package com.hubdelivery.hub.application.dto;

import com.hubdelivery.hub.domain.entity.HubEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResGetHubDto {

    private UUID hubId;

    private String hubName;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    public static ResGetHubDto from(HubEntity hubEntity) {
        return ResGetHubDto.builder()
                .hubId(hubEntity.getId())
                .hubName(hubEntity.getHubName())
                .latitude(hubEntity.getLatitude())
                .longitude(hubEntity.getLongitude())
                .build();
    }
}
