package com.hubdelivery.deliveryservice.delivery.presentation.dto;

import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryUpdateRequest {

    @NotNull(message = "status는 필수입니다.")
    private DeliveryStatus status;

    @NotBlank(message = "address는 필수입니다.")
    private String address;

    @NotNull(message = "userId는 필수입니다.")
    private UUID userId;

    @NotBlank(message = "slackId는 필수입니다.")
    private String slackId;

    private UUID deliveryManagerId;
}
