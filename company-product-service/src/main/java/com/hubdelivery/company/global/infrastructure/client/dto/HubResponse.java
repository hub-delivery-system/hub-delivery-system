package com.hubdelivery.company.global.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record HubResponse(
        UUID hubId,
        String hubName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
