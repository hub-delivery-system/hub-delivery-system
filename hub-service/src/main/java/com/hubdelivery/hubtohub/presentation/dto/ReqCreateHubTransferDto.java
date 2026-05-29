package com.hubdelivery.hubtohub.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Hub 이동 경로 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqCreateHubTransferDto {

    /**
     * 출발 허브 ID
     */
    @NotNull(message="출발 허브는 필수입니다.")
    private UUID fromHubId;

    /**
     * 도착 허브 ID
     */
    @NotNull(message="도착 허브는 필수입니다.")
    private UUID toHubId;
}
