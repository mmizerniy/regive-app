package mmdev.regiveapp.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Name must not be blank")
        String name
) {
}
