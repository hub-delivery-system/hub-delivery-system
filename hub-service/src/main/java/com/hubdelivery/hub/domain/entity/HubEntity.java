package com.hubdelivery.hub.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "p_hub",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_latitude_longitude",
                columnNames = {"latitude", "longitude"}
        )
)
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
public class HubEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;

    @Column(name="hub_name",length = 100, nullable = false)
    private String hubName;


    @Column(name="address",length = 255, nullable = false)
    private String address;

    @Column(name = "latitude",precision = 10, scale = 7,nullable = false)
    private BigDecimal latitude;

    @Column(name="longitude",precision = 10, scale = 7,nullable = false)
    private BigDecimal longitude;


    @Builder
    public HubEntity(
            String hubName,
            String address,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        this.hubName = hubName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
