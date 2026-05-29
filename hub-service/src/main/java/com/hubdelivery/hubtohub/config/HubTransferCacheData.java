package com.hubdelivery.hubtohub.config;

import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubTransferCacheData implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID routeId;
    private UUID startHubId;
    private UUID endHubId;
    private BigDecimal distanceKm;
    private Integer durationMinutes;
    private Long durationSec;
    private List<WaypointCacheInfo> waypoints;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WaypointCacheInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer sequence;
    }

    /**
     * ResponseDto → CacheData 변환
     */
    public static HubTransferCacheData fromResponseDto(ResGetHubTransferDto dto) {
        List<WaypointCacheInfo> waypointsCacheInfo = dto.getWaypoints().stream()
                .map(waypoint -> WaypointCacheInfo.builder()
                        .name(waypoint.getName())
                        .latitude(waypoint.getLatitude())
                        .longitude(waypoint.getLongitude())
                        .sequence(waypoint.getSequence())
                        .build())
                .toList();

        return HubTransferCacheData.builder()
                .routeId(dto.getRouteId())
                .startHubId(dto.getStartHubId())
                .endHubId(dto.getEndHubId())
                .distanceKm(dto.getDistanceKm())
                .durationMinutes(dto.getDurationMinutes())
                .durationSec(dto.getDurationSec())
                .waypoints(waypointsCacheInfo)
                .build();
    }

    /**
     * CacheData → ResponseDto 변환
     */
    public ResGetHubTransferDto toResponseDto() {
        List<ResGetHubTransferDto.WaypointInfo> waypointsInfo = waypoints.stream()
                .map(waypoint -> ResGetHubTransferDto.WaypointInfo.builder()
                        .name(waypoint.getName())
                        .latitude(waypoint.getLatitude())
                        .longitude(waypoint.getLongitude())
                        .sequence(waypoint.getSequence())
                        .build())
                .toList();

        return ResGetHubTransferDto.builder()
                .routeId(routeId)
                .startHubId(startHubId)
                .endHubId(endHubId)
                .distanceKm(distanceKm)
                .durationMinutes(durationMinutes)
                .durationSec(durationSec)
                .waypoints(waypointsInfo)
                .build();
    }
}
