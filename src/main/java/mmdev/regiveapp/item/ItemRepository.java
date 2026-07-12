package mmdev.regiveapp.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long> {

    @Query("""
        select i from Item i
        join fetch i.owner
        join fetch i.category
        where (:city is null or i.city = :city)
          and (:categoryId is null or i.category.id = :categoryId)
          and i.status = mmdev.regiveapp.item.ItemStatus.ACTIVE
        """)
    Page<Item> search(@Param("city") String city,
                      @Param("categoryId") Long categoryId,
                      Pageable pageable);
}
