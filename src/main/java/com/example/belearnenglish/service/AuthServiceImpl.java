package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.entity.RefreshToken;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.entity.enums.UserStatus;
import com.example.belearnenglish.repository.RefreshTokenRepository;
import com.example.belearnenglish.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(RefreshTokenRepository refreshTokenRepository,
                           JwtProvider jwtProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Transactional
    public TokenPair generateTokenPair(User user) {
        ensureCanIssueToken(user);
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getDisplayName(),
                user.getStatus().name()
        );
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
        ensureCanIssueToken(user);
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

    private static final int MAX_ACTIVE_SESSIONS = 5;

    private void storeRefreshToken(User user, String rawRefreshToken) {
        Instant now = Instant.now();

        // Xóa token đã expired hoặc revoked để giữ DB gọn
        refreshTokenRepository.deleteExpiredOrRevokedByUserId(user.getId(), now);

        // Nếu vẫn vượt giới hạn session, revoke session cũ nhất
        long activeCount = refreshTokenRepository.countActiveByUserId(user.getId(), now);
        if (activeCount >= MAX_ACTIVE_SESSIONS) {
            List<RefreshToken> oldest = refreshTokenRepository
                    .findActiveByUserIdOrderByCreatedAtAsc(user.getId(), now);
            long toRevoke = activeCount - MAX_ACTIVE_SESSIONS + 1;
            oldest.stream()
                    .limit(toRevoke)
                    .forEach(t -> {
                        t.setRevoked(true);
                        refreshTokenRepository.save(t);
                    });
        }

        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
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

    private void ensureCanIssueToken(User user) {
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.LOCK) {
            throw new BadCredentialsException("Tài khoản này đã bị xóa");
        }
    }

}
