package com.hubdelivery.hub.application.dto;

import com.hubdelivery.hub.domain.entity.HubEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResGetHubDto implements Serializable {

    private UUID hubId;

    private String hubName;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    public static ResGetHubDto from(HubEntity hubEntity) {
        return ResGetHubDto.builder()
                .hubId(hubEntity.getId())
                .hubName(hubEntity.getHubName())
                .address(hubEntity.getAddress())
                .latitude(hubEntity.getLatitude())
                .longitude(hubEntity.getLongitude())
                .build();
    }
}
