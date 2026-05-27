package com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HubResponse {

    private UUID hubId;
    private String hubName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
