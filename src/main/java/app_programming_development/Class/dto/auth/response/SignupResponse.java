package app_programming_development.Class.dto.auth.response;

import app_programming_development.Class.user.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SignupResponse {
    private Long userId;
    private String email;
    private String nickname;

    public static SignupResponse from(Users user) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
