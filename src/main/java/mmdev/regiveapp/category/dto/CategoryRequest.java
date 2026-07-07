package mmdev.regiveapp.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest (
        @NotBlank(message = "Name must not be blank")
        String name
){}
