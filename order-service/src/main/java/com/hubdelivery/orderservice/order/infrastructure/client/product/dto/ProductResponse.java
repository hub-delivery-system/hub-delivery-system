package com.hubdelivery.orderservice.order.infrastructure.client.product.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductResponse {

    private UUID id;
    private String productName;
    private UUID hubId;
    private UUID companyId;
    private int stockQuantity;
}
