package com.hubdelivery.orderservice.order.infrastructure.client.product.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductResponse {

    private UUID id;
    private String productName;
    private UUID hubId;      // HUB_MANAGER 권한 체크 시 사용
    private UUID companyId;
}
