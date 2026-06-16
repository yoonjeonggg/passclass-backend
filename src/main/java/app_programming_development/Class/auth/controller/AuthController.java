package app_programming_development.Class.auth.controller;

import app_programming_development.Class.auth.service.AuthService;
import app_programming_development.Class.dto.auth.request.AutoLoginRequest;
import app_programming_development.Class.dto.auth.request.EmailSendRequest;
import app_programming_development.Class.dto.auth.request.EmailVerifyRequest;
import app_programming_development.Class.dto.auth.request.LoginRequest;
import app_programming_development.Class.dto.auth.request.PasswordResetConfirmRequest;
import app_programming_development.Class.dto.auth.request.SignupRequest;
import app_programming_development.Class.dto.auth.response.SignupResponse;
import app_programming_development.Class.dto.auth.response.TokenResponse;
import app_programming_development.Class.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "비밀번호가 일치하지 않습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "로그인 되었습니다."));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입 시 사용하는 API 입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 계정 입니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse result = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "회원가입 되었습니다."));
    }

    @PostMapping("/auto-login")
    @Operation(summary = "자동 로그인", description = "리프레시 토큰을 넣으면 엑세스 토큰을 반환 해줍니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "유효하지 않은 리프레시 토큰 입니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<TokenResponse>> autoLogin(@Valid @RequestBody AutoLoginRequest request) {
        TokenResponse result = authService.autoLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "로그인 되었습니다."));
    }

    @PostMapping("/log-out")
    @Operation(summary = "로그아웃", description = "엑세스 토큰을 넣으면 로그아웃이 됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> logOut() {
        authService.logOut();
        return ResponseEntity.ok(ApiResponse.ok("로그아웃 되었습니다."));
    }

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증 코드 발송", description = "이메일 인증 코드를 발송합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    })
    public ResponseEntity<ApiResponse<Void>> sendVerificationEmail(@Valid @RequestBody EmailSendRequest request) {
        authService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("인증 코드가 발송되었습니다."));
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증 확인", description = "발송된 인증 코드로 이메일을 인증합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 인증 코드", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정 코드 발송", description = "가입된 이메일로 비밀번호 재설정 코드를 발송합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetEmail(@Valid @RequestBody EmailSendRequest request) {
        authService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정 코드가 발송되었습니다."));
    }

    @PostMapping("/password/confirm")
    @Operation(summary = "비밀번호 재설정", description = "인증 코드와 새 비밀번호로 비밀번호를 재설정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 인증 코드", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계정이 존재하지 않습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 재설정되었습니다."));
    }
}
