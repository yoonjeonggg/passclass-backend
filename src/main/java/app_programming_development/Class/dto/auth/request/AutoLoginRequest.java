package app_programming_development.Class.dto.auth.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AutoLoginRequest {
    private String refreshToken;
}
