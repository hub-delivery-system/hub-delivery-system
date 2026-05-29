package com.hubdelivery.orderservice.order.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "수령업체 ID는 필수입니다.")
    private UUID receiverId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 1 이상이어야 합니다.")
    private Integer amount;

    private String requestMessage; // 납품 기한 등 요청사항 (선택)

    // 배송 생성에 필요한 정보
    @NotNull(message = "출발 허브 ID는 필수입니다.")
    private UUID startHubId;

    @NotNull(message = "도착 허브 ID는 필수입니다.")
    private UUID endHubId;

    @NotBlank(message = "배송 주소는 필수입니다.")
    private String address;

    @NotNull(message = "배송 수령인 userId는 필수입니다.")
    private UUID deliveryUserId;

    @NotBlank(message = "슬랙 ID는 필수입니다.")
    private String slackId;

    @Valid
    @NotEmpty(message = "경로 정보는 필수입니다.")
    private List<RouteRequest> routes;

    @Getter
    @NoArgsConstructor
    public static class RouteRequest {

        @NotNull(message = "경로 순번은 필수입니다.")
        private Integer sequence;

        @NotNull(message = "경로 출발 허브 ID는 필수입니다.")
        private UUID startHubId;

        @NotNull(message = "경로 도착 허브 ID는 필수입니다.")
        private UUID endHubId;

        private BigDecimal estimatedDistance;  // 예상 거리 (선택)
        private Integer estimatedDuration;     // 예상 소요 시간(분) (선택)
        private UUID deliveryManagerId;        // 수동 지정 시 사용 (선택)
    }
}
