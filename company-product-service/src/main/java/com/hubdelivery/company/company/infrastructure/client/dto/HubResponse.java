package com.hubdelivery.company.company.infrastructure.client.dto;

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
