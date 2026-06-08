package com.ktb.community.dto.report;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ReportRequestDto {

    @NotBlank(message = "신고 사유를 입력해주세요.")
    private String reason;
}
