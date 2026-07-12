create index idx_items_status_city_created_at
    on items (status, city, created_at desc);

create index idx_items_status_category_created_at
    on items (status, category_id, created_at desc);