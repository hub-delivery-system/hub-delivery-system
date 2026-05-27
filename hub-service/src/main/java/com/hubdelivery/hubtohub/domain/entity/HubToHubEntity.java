package com.hubdelivery.hubtohub.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "p_hubTohub",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_start_end_hub",
                        columnNames = {"start_hub_id", "end_hub_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubToHubEntity extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name="start_hub",nullable = false)
    private UUID startHubId;


    @Column(name="end_hub",nullable = false)
    private UUID endHubId;

    @Column(name="duration_sec",nullable = false)
    private Long durationSec;

    @Column(name="distance", nullable = false,precision = 8,scale=2)
    private BigDecimal distance;

    @Builder
    public HubToHubEntity(UUID startHubId,UUID endHubId, Long durationSec,BigDecimal distance){
        this.startHubId=startHubId;
        this.endHubId=endHubId;
        this.durationSec=durationSec;
        this.distance=distance;
    }

}
