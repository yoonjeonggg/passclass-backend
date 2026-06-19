package app_programming_development.Class.auth.service;

import app_programming_development.Class.auth.entity.EmailVerification;
import app_programming_development.Class.auth.entity.RefreshTokens;
import app_programming_development.Class.auth.repository.EmailVerificationRepository;
import app_programming_development.Class.auth.repository.RefreshTokenRepository;
import app_programming_development.Class.dto.auth.request.AutoLoginRequest;
import app_programming_development.Class.dto.auth.request.EmailVerifyRequest;
import app_programming_development.Class.dto.auth.request.LoginRequest;
import app_programming_development.Class.dto.auth.request.PasswordResetConfirmRequest;
import app_programming_development.Class.dto.auth.request.SignupRequest;
import app_programming_development.Class.dto.auth.response.SignupResponse;
import app_programming_development.Class.dto.auth.response.TokenResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.badRequest.InvalidVerificationCodeException;
import app_programming_development.Class.exceptions.conflict.UserAlreadyExistsException;
import app_programming_development.Class.exceptions.notFound.RefreshTokenNotFoundException;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.PasswordMismatchException;
import app_programming_development.Class.global.TokenProvider;
import app_programming_development.Class.discord.DiscordWebhookService;
import app_programming_development.Class.logging.AuditLog;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final DiscordWebhookService discordWebhookService;

    @AuditLog(action = "LOGIN")
    public TokenResponse login(LoginRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed - password mismatch: userId={}", user.getId());
            throw new PasswordMismatchException();
        }

        String accessToken = tokenProvider.createToken(authentication.getName());
        String refreshToken = tokenProvider.createRefreshToken(authentication.getName());

        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        log.info("User login: userId={}", user.getId());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @AuditLog(action = "SIGNUP")
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileUrl(request.getProfileImage())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("New user registered: userId={}, role={}", user.getId(), user.getRole());
        discordWebhookService.sendNewUser(user.getNickname(), user.getRole().name());
        return SignupResponse.from(user);
    }

    public TokenResponse autoLogin(AutoLoginRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RefreshTokenNotFoundException();
        }

        RefreshTokens existingToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(RefreshTokenNotFoundException::new);

        String email = tokenProvider.getEmail(refreshToken);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = tokenProvider.createToken(user.getEmail());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        log.debug("Auto-login: userId={}", user.getId());
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logOut() {
        Users user = securityUtils.getCurrentUser();
        refreshTokenRepository.deleteByUser(user);
        log.info("User logout: userId={}", user.getId());
    }

    @Transactional
    public void sendVerificationEmail(String email) {
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        emailVerificationRepository.deleteByEmail(email);
        
        emailVerificationRepository.save(EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build());
        emailService.sendVerificationCode(email, code);
        log.info("Verification email requested: email={}", email);
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        sendVerificationEmail(email);
        log.info("Password reset email requested: email={}", email);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(InvalidVerificationCodeException::new);

        if (verification.isExpired() || !verification.getCode().equals(request.getCode())) {
            throw new InvalidVerificationCodeException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        emailVerificationRepository.deleteByEmail(request.getEmail());
        log.info("Password reset completed: userId={}", user.getId());
    }

    @Transactional
    public void verifyEmail(EmailVerifyRequest request) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(InvalidVerificationCodeException::new);

        if (verification.isExpired() || !verification.getCode().equals(request.getCode())) {
            throw new InvalidVerificationCodeException();
        }

        verification.verify();

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setEmailVerified(true);
            userRepository.save(user);
        });

        log.info("Email verified: email={}", request.getEmail());
    }
}
