package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.LoginRequest;
import com.example.belearnenglish.dto.LoginResponse;
import com.example.belearnenglish.dto.RegisterRequest;
import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse register(RegisterRequest request);
    TokenPair refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
    TokenPair generateTokenPair(User user);

    /**
     * Đặt lại mật khẩu mới (sau khi OTP đã được verify).
     * @param email    email tài khoản
     * @param otpCode  mã OTP 6 số (sẽ verify lại ở đây)
     * @param newPassword mật khẩu mới
     */
    void resetPassword(String email, String otpCode, String newPassword);
}
