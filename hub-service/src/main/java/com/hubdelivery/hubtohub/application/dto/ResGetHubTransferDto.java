package com.hubdelivery.hubtohub.application.dto;


import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResGetHubTransferDto {

    /**
     * 경로 ID
     */
    private UUID routeId;

    /**
     * 출발 허브 ID
     */
    private UUID startHubId;

    /**
     * 도착 허브 ID
     */
    private UUID endHubId;

    /**
     * 거리 (km)
     */
    private BigDecimal distanceKm;

    /**
     * 소요시간 (분)
     */
    private Integer durationMinutes;

    /**
     * 소요시간 (초) - DB 저장용
     */
    private Long durationSec;

    /**
     * 경유지 목록
     */
    private List<WaypointInfo> waypoints;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WaypointInfo {
        /**
         * 경유지명 (예: 경기남부 중앙허브, 대전 중앙허브)
         */
        private String name;

        /**
         * 위도
         */
        private BigDecimal latitude;

        /**
         * 경도
         */
        private BigDecimal longitude;

        /**
         * 순서 (1번부터 시작)
         */
        private Integer sequence;
    }

    /**
     * Entity → DTO 변환
     */
    public static ResGetHubTransferDto fromEntity(
            HubTransferEntity entity,
            List<WaypointInfo> waypoints) {
        return ResGetHubTransferDto.builder()
                .routeId(entity.getId())
                .startHubId(entity.getStartHubId())
                .endHubId(entity.getEndHubId())
                .distanceKm(entity.getDistance())
                .durationMinutes((int) (entity.getDurationSec() / 60))
                .durationSec(entity.getDurationSec())
                .waypoints(waypoints)
                .build();
    }
}
