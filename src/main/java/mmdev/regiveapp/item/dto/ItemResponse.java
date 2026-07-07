package mmdev.regiveapp.item.dto;

import mmdev.regiveapp.item.ItemStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String city,
        ItemStatus status,
        Long ownerId,
        String ownerName,
        Long categoryId,
        String categoryName,
        Instant createdAt
) {
}
