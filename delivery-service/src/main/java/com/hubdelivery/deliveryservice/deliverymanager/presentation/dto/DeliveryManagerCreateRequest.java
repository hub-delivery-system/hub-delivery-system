package com.hubdelivery.deliveryservice.deliverymanager.presentation.dto;

import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryManagerCreateRequest {

    @NotNull(message = "userId는 필수입니다.")
    private UUID userId;

    @NotNull(message = "hubId는 필수입니다.")
    private UUID hubId;

    // COMPANY_DELIVERY 타입일 때만 사용
    private UUID companyId;

    @NotNull(message = "type은 필수입니다.")
    private DeliveryManagerType type;
}
