package com.ktb.community.service;

import com.ktb.community.dto.login.LoginRequestDto;
import com.ktb.community.dto.login.LoginResponseDto;
import com.ktb.community.dto.password.PasswordUpdateRequestDto;
import com.ktb.community.dto.user.SignUpRequestDto;
import com.ktb.community.dto.user.SignUpResponseDto;
import com.ktb.community.dto.user.UserResponseDto;
import com.ktb.community.dto.user.UserUpdateRequestDto;
import com.ktb.community.entity.User;
import com.ktb.community.exception.*;
import com.ktb.community.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public SignUpResponseDto signup(SignUpRequestDto request) {

        if(userRepository.existsByEmail(request.getEmail())
                || userRepository.existsByNickname(request.getNickname())) {
            throw new AlreadyExistsException();
        }

        if(!request.getPassword().equals(request.getPasswordCheck())) {
            throw new InvalidInputException();
        }

        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getProfileImage()
        );

        User savedUser = userRepository.save(user);

        return new SignUpResponseDto(savedUser.getUserId());

    }


    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotRegisteredException());

        if(!user.getPassword().equals(request.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = "token-user-" + user.getUserId();

        return new LoginResponseDto(user.getUserId(), accessToken);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getMyPage(String authorization, Long userId) {

        User loginUser = authService.getLoginUser(authorization);

        if(!loginUser.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        return new UserResponseDto(loginUser.getUserId(), loginUser.getNickname(), loginUser.getEmail(), loginUser.getPassword(), loginUser.getProfileImage());
    }

    public void updateUser(String authorization, Long userId, @Valid UserUpdateRequestDto request) {

        User loginUser = authService.getLoginUser(authorization);

        if(!loginUser.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        loginUser.update(request.getNickname(), request.getProfileImage());
    }

    public void updatePassword(String authorization, Long userId, PasswordUpdateRequestDto request) {

        User loginUser = authService.getLoginUser(authorization);

        if(!loginUser.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        loginUser.updatePassword(request.getPassword());
    }

    public void deleteUser(String authorization, Long userId) {

        User loginUser = authService.getLoginUser(authorization);

        if(!loginUser.getUserId().equals(userId)) {
            throw new UnauthorizedException();
        }

        if(loginUser.isDeleted()) {
            throw new ConflictException();
        }

        loginUser.delete();
    }
}
