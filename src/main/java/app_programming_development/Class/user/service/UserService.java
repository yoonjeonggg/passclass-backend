package app_programming_development.Class.user.service;

import app_programming_development.Class.dto.user.request.PatchMyProfileRequest;
import app_programming_development.Class.dto.user.response.MyProfileResponse;
import app_programming_development.Class.dto.user.response.ProfileResponse;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public ProfileResponse getProfile(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return ProfileResponse.from(user);
    }

    public MyProfileResponse getMyProfile() {
        Users user = securityUtils.getCurrentUser();
        return MyProfileResponse.from(user);
    }

    public MyProfileResponse patchMyProfile(PatchMyProfileRequest request) {
        Users user = securityUtils.getCurrentUser();
        user.setNickname(request.getNickname());
        user.setProfileUrl(request.getProfileImage());
        userRepository.save(user);
        return MyProfileResponse.from(user);
    }
}
