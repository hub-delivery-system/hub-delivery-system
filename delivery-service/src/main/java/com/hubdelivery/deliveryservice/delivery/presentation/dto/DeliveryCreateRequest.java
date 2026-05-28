package com.hubdelivery.deliveryservice.delivery.presentation.dto;

import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCreateRequest {

    @NotNull(message = "orderId는 필수입니다.")
    private UUID orderId;

    @NotNull(message = "startHubId는 필수입니다.")
    private UUID startHubId;

    @NotNull(message = "endHubId는 필수입니다.")
    private UUID endHubId;

    @NotBlank(message = "address는 필수입니다.")
    private String address;

    @NotNull(message = "userId는 필수입니다.")
    private UUID userId;

    @NotBlank(message = "slackId는 필수입니다.")
    private String slackId;

    private UUID deliveryManagerId;

    @Valid
    @NotEmpty(message = "routes는 비어 있을 수 없습니다.")
    private List<RouteRequest> routes;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteRequest {

        @NotNull(message = "sequence는 필수입니다.")
        private Integer sequence;

        @NotNull(message = "startHubId는 필수입니다.")
        private UUID startHubId;

        @NotNull(message = "endHubId는 필수입니다.")
        private UUID endHubId;

        private BigDecimal estimatedDistance;
        private Integer estimatedDuration;
        private UUID deliveryManagerId;
    }
}
