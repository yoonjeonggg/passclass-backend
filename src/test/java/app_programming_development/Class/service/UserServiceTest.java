package app_programming_development.Class.service;

import app_programming_development.Class.dto.user.request.PatchMyProfileRequest;
import app_programming_development.Class.dto.user.response.MyProfileResponse;
import app_programming_development.Class.dto.user.response.ProfileResponse;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.user.repository.UserRepository;
import app_programming_development.Class.user.service.UserService;
import app_programming_development.Class.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private UserService userService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .email("user@test.com")
                .password("password")
                .nickname("테스트유저")
                .profileUrl("http://profile.com/img.jpg")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getProfile_성공() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ProfileResponse result = userService.getProfile(1L);

        assertThat(result.getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("프로필 조회 - 존재하지 않는 유저 예외")
    void getProfile_유저없음_예외() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("내 프로필 조회 - 성공")
    void getMyProfile_성공() {
        given(securityUtils.getCurrentUser()).willReturn(user);

        MyProfileResponse result = userService.getMyProfile();

        assertThat(result.getNickname()).isEqualTo("테스트유저");
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("내 프로필 수정 - 닉네임과 프로필 사진 변경")
    void patchMyProfile_성공() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(userRepository.save(any())).willReturn(user);

        PatchMyProfileRequest request = PatchMyProfileRequest.builder()
                .nickname("새닉네임")
                .profileImage("http://new-profile.com/img.jpg")
                .build();
        MyProfileResponse result = userService.patchMyProfile(request);

        assertThat(result.getNickname()).isEqualTo("새닉네임");
    }
}
