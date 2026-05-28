package com.hubdelivery.hubtohub.domain.entity;


import com.hubdelivery.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;



@Entity
@Table(name = "p_central_hub")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CentralHubEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Hub ID (기존 Hub 테이블 참조)
     */
    @Column(name = "hub_id", nullable = false, unique = true)
    private UUID hubId;

    @Builder
    public CentralHubEntity(UUID hubId) {
        this.hubId = hubId;
    }
}
