package com.hubdelivery.deliveryservice.deliveryroute.presentation.dto;

import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryRouteUpdateRequest {

    @NotNull(message = "status는 필수입니다.")
    private DeliveryRouteStatus status;

    private BigDecimal realDistance;
    private Integer realDuration;
    private UUID deliveryManagerId;
}
