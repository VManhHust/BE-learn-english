package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.ErrorResponse;
import com.example.belearnenglish.dto.ForgotPasswordOtpRequest;
import com.example.belearnenglish.dto.LoginRequest;
import com.example.belearnenglish.dto.LoginResponse;
import com.example.belearnenglish.dto.RegisterRequest;
import com.example.belearnenglish.dto.ResetPasswordRequest;
import com.example.belearnenglish.dto.SendOtpRequest;
import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.dto.VerifyForgotPasswordOtpRequest;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.service.AuthService;
import com.example.belearnenglish.service.EmailVerificationService;
import com.example.belearnenglish.service.EmailVerificationServiceImpl;
import com.example.belearnenglish.service.GoogleOAuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "linguaflow_refresh_token";
    private static final long OAUTH_SESSION_CODE_TTL_SECONDS = 120;

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationServiceImpl emailVerificationServiceImpl;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OAuthSession> pendingOAuthSessions = new ConcurrentHashMap<>();

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookies;

    public AuthController(AuthService authService,
                          GoogleOAuthService googleOAuthService,
                          EmailVerificationService emailVerificationService,
                          EmailVerificationServiceImpl emailVerificationServiceImpl,
                          UserRepository userRepository) {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationServiceImpl = emailVerificationServiceImpl;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse servletResponse) {
        LoginResponse response = authService.login(request);
        setRefreshCookie(servletResponse, response.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 1 đăng ký: gửi OTP xác thực email.
     * Kiểm tra email chưa tồn tại rồi mới gửi.
     */
    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendRegisterOtp(@Valid @RequestBody SendOtpRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409)
                    .body(new ErrorResponse("Email này đã được đăng ký."));
        }
        try {
            emailVerificationService.sendOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Mã xác thực đã được gửi đến email của bạn."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Bước 2 đăng ký: verify OTP rồi tạo tài khoản.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletResponse servletResponse) {
        try {
            // Verify OTP trước khi tạo account
            emailVerificationService.verifyOtp(request.getEmail(), request.getOtpCode());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }

        LoginResponse response = authService.register(request);
        setRefreshCookie(servletResponse, response.getRefreshToken());
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Forgot password bước 1: gửi OTP về email.
     * Không tiết lộ email có tồn tại hay không (anti-enumeration).
     */
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(@Valid @RequestBody ForgotPasswordOtpRequest request) {
        // Chỉ gửi nếu email tồn tại, nhưng luôn trả về 200 để tránh enumeration
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            try {
                emailVerificationServiceImpl.sendForgotPasswordOtp(request.getEmail());
            } catch (IllegalStateException e) {
                return ResponseEntity.status(429).body(new ErrorResponse(e.getMessage()));
            }
        }
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, mã xác thực đã được gửi."));
    }

    /**
     * Forgot password bước 2: verify OTP (không đặt lại mật khẩu ở đây, chỉ xác nhận OTP hợp lệ).
     * FE sẽ lưu otpCode tạm rồi gửi cùng request reset password.
     */
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyForgotPasswordOtp(@Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        try {
            // Chỉ verify, KHÔNG mark as used — sẽ dùng lại khi reset password
            // Thêm method verifyOtpWithoutConsume vào service để không mark used
            emailVerificationService.checkOtp(request.getEmail(), request.getOtpCode());
            return ResponseEntity.ok(Map.of("valid", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Forgot password bước 3: đặt lại mật khẩu mới.
     */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
        try {
            TokenPair tokenPair = authService.refresh(refreshToken);
            return ResponseEntity.ok(tokenPair);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String[] result = googleOAuthService.buildAuthorizationUrl();
        response.sendRedirect(result[0]);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
        if (!googleOAuthService.isValidState(state)) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }
        if (error != null || code == null) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }
        try {
            GoogleOAuthService.GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(code);
            User user = userRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(userInfo.getEmail())
                                .displayName(userInfo.getName())
                                .googleId(userInfo.getGoogleId())
                                .build();
                        return userRepository.save(newUser);
            });
            TokenPair tokenPair = authService.generateTokenPair(user);
            setRefreshCookie(response, tokenPair.getRefreshToken());
            String sessionCode = createOAuthSessionCode(tokenPair);
            response.sendRedirect(frontendUrl
                    + "/auth/callback?code=" + encode(sessionCode));
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
        }
    }

    @PostMapping("/oauth/session")
    public ResponseEntity<?> exchangeOAuthSession(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.status(400).body(new ErrorResponse("OAuth session code is required"));
        }

        OAuthSession session = pendingOAuthSessions.remove(code);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(new ErrorResponse("OAuth session code invalid or expired"));
        }

        return ResponseEntity.ok(session.tokenPair());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String createOAuthSessionCode(TokenPair tokenPair) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();
        pendingOAuthSessions.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
        pendingOAuthSessions.put(code, new OAuthSession(
                tokenPair,
                now.plusSeconds(OAUTH_SESSION_CODE_TTL_SECONDS)
        ));
        return code;
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        response.addCookie(cookie);
    }

    private record OAuthSession(TokenPair tokenPair, Instant expiresAt) {
    }
}
