package com.hubdelivery.company.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "상품 재고 증감 요청 DTO")
public record ProductStockUpdateRequestDto(

        @Schema(name = "quantity", description = "증감할 상품 수량", example = "3")
        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
