package com.hubdelivery.orderservice.order.presentation.dto;

import com.hubdelivery.orderservice.order.domain.type.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderUpdateRequest {

    @NotNull(message = "주문 상태는 필수입니다.")
    private OrderStatus status;

    private String requestMessage; // 요청사항 수정 (선택)
}
