package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LoginRequest;
import com.example.belearnenglish.dto.LoginResponse;
import com.example.belearnenglish.dto.RegisterRequest;
import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.dto.UserDto;
import com.example.belearnenglish.entity.RefreshToken;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.RefreshTokenRepository;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           JwtProvider jwtProvider,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getEmail())
                .build();
        user = userRepository.save(user);
        TokenPair tokenPair = generateTokenPair(user);
        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole().name());
        return new LoginResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(), userDto);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email hoặc mật khẩu không đúng");
        }

        TokenPair tokenPair = generateTokenPair(user);

        UserDto userDto = new UserDto(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole().name());
        return new LoginResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken(), userDto);
    }

    @Override
    @Transactional
    public TokenPair generateTokenPair(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String rawRefreshToken = jwtProvider.generateRefreshToken(user.getId());
        storeRefreshToken(user, rawRefreshToken);
        return new TokenPair(accessToken, rawRefreshToken);
    }

    @Override
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new JwtException("Token invalid or expired"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new JwtException("Token invalid or expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        return generateTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private void storeRefreshToken(User user, String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
