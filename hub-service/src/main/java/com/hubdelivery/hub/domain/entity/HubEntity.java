package com.hubdelivery.hub.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="p_hub")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor
public class HubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;

    @Column(name="hub_name",length = 100, nullable = false)
    private String HubName;


    @Column(name="address",length = 255, nullable = false)
    private String Address;

    @Column(name = "latitude",precision = 10, scale = 7,nullable = false)
    private BigDecimal Latitude;

    @Column(name="longitude",precision = 10, scale = 7,nullable = false)
    private BigDecimal Longitude;

}
