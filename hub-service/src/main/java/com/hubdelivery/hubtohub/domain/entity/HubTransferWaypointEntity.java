package com.hubdelivery.hubtohub.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Hub To Hub 경유지 엔티티
 * - 경로의 경유지 정보만 저장
 * - CentralHubEntity ID 참조
 * - 순서 정보 포함
 */
@Entity
@Table(name = "p_hubtohub_waypoint")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubTransferWaypointEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hubtohub_id", nullable = false)
    private UUID hubToHubId;

    /**
     * 중앙허브 ID (CentralHubEntity ID)
     */
    @Column(name = "central_hub_id", nullable = false)
    private UUID centralHubId;

    /**
     * 경유지 순서 (1번부터 시작)
     */
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Builder
    public HubTransferWaypointEntity(
            UUID hubToHubId,        // ⭐ 추가
            UUID centralHubId,
            Integer sequence) {
        this.hubToHubId = hubToHubId;  // ⭐ 추가
        this.centralHubId = centralHubId;
        this.sequence = sequence;
    }
}
