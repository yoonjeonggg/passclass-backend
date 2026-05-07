package app_programming_development.Class.dto.user.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatchMyProfileRequest {
    private String nickname;
    private String profileImage;
}
