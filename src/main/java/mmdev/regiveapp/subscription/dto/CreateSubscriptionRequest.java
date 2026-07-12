package mmdev.regiveapp.subscription.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull Long categoryId,
        String city
) {
}
