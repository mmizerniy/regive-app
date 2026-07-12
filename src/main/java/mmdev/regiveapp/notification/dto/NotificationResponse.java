package mmdev.regiveapp.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long itemId,
        String itemTitle,
        String message,
        boolean read,
        Instant createdAt
) {
}
