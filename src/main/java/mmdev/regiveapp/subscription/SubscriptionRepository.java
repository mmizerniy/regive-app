package mmdev.regiveapp.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    @Query("""
            select s from Subscription s
            join fetch s.user
            where s.category.id = :categoryId
              and (s.city is null or s.city = :city)
            """)
    List<Subscription> findMatching(@Param("categoryId") Long categoryId,
                                    @Param("city") String city);

    List<Subscription> findByUserId(Long userId);

    boolean existsByUserIdAndCategoryIdAndCity(Long userId, Long categoryId, String city);
}
