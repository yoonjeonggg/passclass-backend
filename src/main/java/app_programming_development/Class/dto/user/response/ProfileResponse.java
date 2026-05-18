package app_programming_development.Class.dto.user.response;

import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.user.entity.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {
    private Long id;
    private String nickname;
    private String profileImage;
    private String userRole;

    public static ProfileResponse from(Users user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileUrl())
                .userRole(user.getRole().toString())
                .build();
    }
}
