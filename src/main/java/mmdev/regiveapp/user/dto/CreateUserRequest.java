package mmdev.regiveapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest (
        @NotBlank(message = "Name must not be blank")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Password must be not blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
)
{}
