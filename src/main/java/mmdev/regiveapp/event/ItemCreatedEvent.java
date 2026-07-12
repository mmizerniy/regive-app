package mmdev.regiveapp.event;

import java.math.BigDecimal;
import java.time.Instant;

public record ItemCreatedEvent(
        Long itemId,
        String title,
        String city,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        Long ownerId,
        Instant occurredAt
) {
}
