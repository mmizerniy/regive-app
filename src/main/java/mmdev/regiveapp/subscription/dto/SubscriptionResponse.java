package mmdev.regiveapp.subscription.dto;

import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String city,
        Instant createdAt
) {
}
