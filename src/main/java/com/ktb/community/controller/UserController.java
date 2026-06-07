package com.ktb.community.controller;

import com.ktb.community.common.ApiResponse;
import com.ktb.community.dto.login.LoginRequestDto;
import com.ktb.community.dto.login.LoginResponseDto;
import com.ktb.community.dto.user.SignUpRequestDto;
import com.ktb.community.dto.user.SignUpResponseDto;
import com.ktb.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signup(@Valid @RequestBody SignUpRequestDto request) {

        SignUpResponseDto response = userService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("register_success", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto response = userService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>("login_success", response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId,
            @Valid @RequestBody
    )
}
