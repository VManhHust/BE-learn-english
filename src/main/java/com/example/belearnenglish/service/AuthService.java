package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TokenPair;
import com.example.belearnenglish.entity.User;

public interface AuthService {
    TokenPair refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
    TokenPair generateTokenPair(User user);
}
