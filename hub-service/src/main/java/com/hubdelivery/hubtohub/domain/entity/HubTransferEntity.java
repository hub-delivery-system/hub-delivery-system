package com.hubdelivery.hubtohub.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

/**
 * Hub To Hub 경로 정보 엔티티
 * - 두 허브 간 경로 기본 정보
 * - HubToHubWaypointEntity와 1:N 관계
 */
@Entity
@Table(
        name = "p_hub_to_hub"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubTransferEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 출발 허브 ID
     */
    @Column(name = "start_hub", nullable = false)
    private UUID startHubId;

    /**
     * 도착 허브 ID
     */
    @Column(name = "end_hub", nullable = false)
    private UUID endHubId;

    /**
     * 소요시간 (초)
     */
    @Column(name = "duration_sec", nullable = false)
    private Long durationSec;

    /**
     * 거리 (km)
     */
    @Column(name = "distance", nullable = false, precision = 8, scale = 2)
    private BigDecimal distance;

    /**
     * 경유지 정보 (1:N 관계)
     * cascade: 부모 삭제 시 자식도 삭제
     * orphanRemoval: 리스트에서 제거되면 자식 삭제
     */
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "hubtohub_id")
    private List<HubTransferWaypointEntity> waypoints = new ArrayList<>();

    @Builder
    public HubTransferEntity(
            UUID startHubId,
            UUID endHubId,
            Long durationSec,
            BigDecimal distance) {
        this.startHubId = startHubId;
        this.endHubId = endHubId;
        this.durationSec = durationSec;
        this.distance = distance;
        this.waypoints = new ArrayList<>();
    }

    /**
     * 경유지 추가
     */
    public void addWaypoint(HubTransferWaypointEntity waypoint) {
        waypoints.add(waypoint);
    }

    /**
     * 경유지 일괄 추가
     */
    public void addAllWaypoints(List<HubTransferWaypointEntity> newWaypoints) {
        waypoints.addAll(newWaypoints);
    }

    /**
     * 경유지 초기화
     */
    public void clearWaypoints() {
        waypoints.clear();
    }

    public void updateRoute(BigDecimal distance, long durationSec) {
        this.distance = distance;
        this.durationSec = durationSec;
    }
}
