package com.hubdelivery.hubtohub.domain.entity;


import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.hub.domain.entity.HubEntity;
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
     * Hub (기존 Hub 테이블 참조)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", insertable = false, updatable = false)
    private HubEntity hub;

    @Builder
    public CentralHubEntity(HubEntity hub) {
        this.hub = hub;
    }
}
