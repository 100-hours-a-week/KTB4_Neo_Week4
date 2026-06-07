package com.ktb.community.exception;

import com.ktb.community.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 : 잘못된 입력값
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationInputException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("invalid_input", null));
    }

    // 401 : 비로그인 혹은 토큰 만료
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("unauthorized_user", null));
    }

    // 401 : 로그인 시, 등록되지 않은 이메일 -> 등록되지 않은 유저
    @ExceptionHandler(NotRegisteredException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotRegisteredException(NotRegisteredException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("not_found_user", null));
    }

    // 401 : 로그인 시, 올바르지 않은 비밀번호
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("invalid_password", null));
    }

    // 403 : 자격 권한이 없는 경우
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("denied_access", null));
    }

    // 404 : 올바르지 않은 페이지 (올바르지 않은 파라미터)
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleContentNotFoundException(ContentNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("content_not_found", null));
    }

    // 409 : 이미 신고한 상태의 게시글
    @ExceptionHandler(AlreadyReportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyReportException(AlreadyReportedException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("already_reported", null));
    }

    // 409 : 회원 가입 시, 이미 존재하는 이메일 혹은 닉네임
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyExistsException(AlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("already_exists", null));
    }


    // 409 : 중복 요청 (좋아요, 탈퇴한 유저의 탈퇴 요청 등)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("conflicted state", null));
    }

    // 429 : 짧은 시간 내 너무 많은 요청 (게시글 도배 방지)
    @ExceptionHandler(TooManyRequests.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(TooManyRequests e) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiResponse<>("too_many_requests", null));
    }

    // 500 : 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("internal_server_error", null));
    }

}
