package app_programming_development.Class.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerifyRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String code;
}
