package app_programming_development.Class.user.service;

import app_programming_development.Class.auth.repository.RefreshTokenRepository;
import app_programming_development.Class.dto.user.request.ChangePasswordRequest;
import app_programming_development.Class.dto.user.request.ChangeRoleRequest;
import app_programming_development.Class.dto.user.request.PatchMyProfileRequest;
import app_programming_development.Class.dto.user.response.MyProfileResponse;
import app_programming_development.Class.dto.user.response.ProfileResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.PasswordMismatchException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

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
        log.info("Profile updated: userId={}", user.getId());
        return MyProfileResponse.from(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed: userId={}", user.getId());
    }

    public void deleteAccount() {
        Users user = securityUtils.getCurrentUser();
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
        log.info("Account deleted: userId={}", user.getId());
    }

    public void changeRole(Long userId, ChangeRoleRequest request) {
        Users admin = securityUtils.getCurrentUser();
        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }
        Users target = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        target.setRole(request.getRole());
        userRepository.save(target);
        log.info("[ADMIN] Role changed: targetUserId={}, newRole={}, adminId={}", userId, request.getRole(), admin.getId());
    }
}
