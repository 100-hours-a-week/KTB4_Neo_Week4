package com.ktb.community.dto.draft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class DraftRequestDto {

    private String title;
    private String postBody;
    private String postImage;
}