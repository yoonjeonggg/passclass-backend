package app_programming_development.Class.dto.user.request;

import app_programming_development.Class.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeRoleRequest {
    @NotNull
    private UserRole role;
}
