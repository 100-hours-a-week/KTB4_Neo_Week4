package com.ktb.community.service;

import com.ktb.community.entity.User;
import com.ktb.community.exception.ApiException;
import com.ktb.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User getLoginUser(String authorization) {

        if(authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String token = authorization.substring(7);

        if(!token.startsWith("token-user-")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Long userId;

        try {
            userId = Long.parseLong(token.replace("token-user-", ""));
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        if(user.isDeleted()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return user;
    }
}
