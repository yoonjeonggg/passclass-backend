package app_programming_development.Class.user.controller;

import app_programming_development.Class.dto.user.request.ChangePasswordRequest;
import app_programming_development.Class.dto.user.request.ChangeRoleRequest;
import app_programming_development.Class.dto.user.request.PatchMyProfileRequest;
import app_programming_development.Class.dto.user.response.MyProfileResponse;
import app_programming_development.Class.dto.user.response.ProfileResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/{userId}")
    @Operation(summary = "프로필 조회", description = "프로필 조회 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable Long userId) {
        ProfileResponse result = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/profile/me")
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile() {
        MyProfileResponse result = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/profile/me")
    @Operation(summary = "내 프로필 수정", description = "내 프로필 수정 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<MyProfileResponse>> patchMyProfile(@RequestBody PatchMyProfileRequest request) {
        MyProfileResponse result = userService.patchMyProfile(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "현재 비밀번호가 일치하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 변경되었습니다."));
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴가 완료되었습니다."));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "사용자 역할 변경 (관리자 전용)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> changeRole(@PathVariable Long userId, @Valid @RequestBody ChangeRoleRequest request) {
        userService.changeRole(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("역할이 변경되었습니다."));
    }
}
