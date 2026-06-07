package com.ktb.community.dto.report;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ReportResponseDto {

    private Long postId;
    private int reportCount;
    private boolean isBlinded;
}
