package com.ktb.community.service;

import com.ktb.community.dto.draft.DraftRequestDto;
import com.ktb.community.dto.draft.DraftResponseDto;
import com.ktb.community.entity.Draft;
import com.ktb.community.entity.User;
import com.ktb.community.exception.ApiException;
import com.ktb.community.exception.ContentNotFoundException;
import com.ktb.community.repository.DraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DraftService {

    private final DraftRepository draftRepository;

    public DraftResponseDto createDraft(User user, DraftRequestDto request) {
        Draft draft = new Draft(
                user,
                request.getTitle(),
                request.getPostBody(),
                request.getPostImage()
        );

        Draft savedDraft = draftRepository.save(draft);
        return toResponse(savedDraft);
    }

    public DraftResponseDto autosaveDraft(User user, Long draftId, DraftRequestDto request) {
        Draft draft = draftRepository.findByDraftIdAndUser(draftId, user)
                .orElseThrow(() -> new ContentNotFoundException());

        if (draft.isPublished()) {
            throw new ApiException(HttpStatus.CONFLICT, "draft_already_published");
        }

        draft.autosave(
                request.getTitle(),
                request.getPostBody(),
                request.getPostImage()
        );

        return toResponse(draft);
    }

    private DraftResponseDto toResponse(Draft draft) {
        return new DraftResponseDto(
                draft.getDraftId(),
                draft.getTitle(),
                draft.getPostBody(),
                draft.getPostImage(),
                draft.getUpdateAt()
        );
    }
}