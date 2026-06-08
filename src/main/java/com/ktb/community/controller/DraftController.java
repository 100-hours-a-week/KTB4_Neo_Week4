package com.ktb.community.controller;

import com.ktb.community.common.ApiResponse;
import com.ktb.community.dto.draft.DraftRequestDto;
import com.ktb.community.dto.draft.DraftResponseDto;
import com.ktb.community.entity.User;
import com.ktb.community.service.AuthService;
import com.ktb.community.service.DraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<DraftResponseDto>> createDraft(
            @RequestHeader("Authorization") String authorization,
            @RequestBody DraftRequestDto request
    ) {
        User loginUser = authService.getLoginUser(authorization);
        DraftResponseDto response = draftService.createDraft(loginUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>("create_draft_success", response));
    }

    @PutMapping("/{draftId}/autosave")
    public ResponseEntity<ApiResponse<DraftResponseDto>> autosaveDraft(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long draftId,
            @RequestBody DraftRequestDto request
    ) {
        User loginUser = authService.getLoginUser(authorization);
        DraftResponseDto response = draftService.autosaveDraft(loginUser, draftId, request);

        return ResponseEntity.ok(
                new ApiResponse<>("autosave_draft_success", response)
        );
    }
}