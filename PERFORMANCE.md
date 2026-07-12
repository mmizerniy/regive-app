# Performance Optimization

A case study of finding, fixing, and measuring a performance problem in the item search
endpoint (`GET /api/items`).

## The problem

The listing feed had three compounding issues, all of which only became visible under
realistic data volume (50,000 items, 1,000 users, 20 categories):

1. **N+1 queries.** `ItemResponse` flattens the owner and category into the response
   (`ownerName`, `categoryName`). Since both associations are `LAZY`, Hibernate issued a
   separate `SELECT` for every single item — roughly 7,400 extra queries for one HTTP call.
2. **No pagination.** The endpoint returned every matching row in a single response.
3. **No index.** PostgreSQL fell back to a sequential scan, reading all 50,000 rows and
   discarding 92% of them, then sorting the survivors in memory.

## Measuring it

Before touching anything, the baseline was captured with `EXPLAIN (ANALYZE, BUFFERS)` and
by observing the SQL Hibernate actually emitted.

```
Sort  (cost=1785.37..1794.57 rows=3683 width=97)
  Sort Key: created_at DESC
  Sort Method: quicksort  Memory: 520kB
  Buffers: shared hit=817
  ->  Seq Scan on items i  (cost=0.00..1567.21 rows=3683 width=97)
        Filter: ((city = 'Lviv') AND (status = 'ACTIVE'))
        Rows Removed by Filter: 46348
Execution Time: 9.344 ms
```

The key signals: `Seq Scan`, `Rows Removed by Filter: 46348`, and an in-memory `Sort`.

## The fix

**1. Eliminate N+1 with a fetch join.** Load the associations in the same query instead of
letting Hibernate lazily fetch them one row at a time:

```java
@Query("""
        select i from Item i
        join fetch i.owner
        join fetch i.category
        where (:city is null or i.city = :city)
          and (:categoryId is null or i.category.id = :categoryId)
          and i.status = ItemStatus.ACTIVE
        """)
Page<Item> search(@Param("city") String city,
                  @Param("categoryId") Long categoryId,
                  Pageable pageable);
```

**2. Add pagination.** The endpoint now accepts `Pageable` and returns a page of 20 items by
default, sorted by `createdAt` descending.

**3. Add a covering index.** Column order matters: equality predicates first, then the sort
column. This lets PostgreSQL both filter *and* return rows already ordered — eliminating the
sort step entirely.

```sql
create index idx_items_status_city_created_at
    on items (status, city, created_at desc);
```

## Results

The `Sort` node disappeared completely — the index already provides the required ordering, so
`LIMIT 20` simply reads the first 20 entries and stops.

```
Limit  (cost=0.41..17.13 rows=20 width=97)
  Buffers: shared hit=21
  ->  Index Scan using idx_items_status_city_created_at on items i
        Index Cond: ((status = 'ACTIVE') AND (city = 'Lviv'))
Execution Time: 0.175 ms
```

| Metric | Before | After | Improvement |
|--------------------------|---------------|--------------|-------------|
| API response time        | 217 ms        | **18 ms**    | 12× faster  |
| Response size            | 883 KB        | **5.2 KB**   | 169× smaller |
| SQL queries per request  | ~7,400        | **2**        | ~3,700× fewer |
| Query plan               | `Seq Scan`    | **`Index Scan`** | — |
| Rows discarded by filter | 46,348        | **0**        | — |
| Shared buffers read      | 817           | **21**       | 39× fewer   |
| SQL execution time       | 9.344 ms      | **0.175 ms** | 53× faster  |

## Takeaway

The interesting part is that the raw SQL was never the bottleneck — it ran in 9 ms even with a
sequential scan, because the table fit in memory. The real cost was the **7,400 round trips**
Hibernate made behind the scenes, which no `EXPLAIN` would ever show. Profiling the ORM's
actual query count mattered more than profiling the query itself.