package mmdev.regiveapp.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateItemRequest(
        @NotBlank String title,
        String description,
        @PositiveOrZero BigDecimal price,
        @NotBlank String city,
        @NotNull Long categoryId
        ) {
}
