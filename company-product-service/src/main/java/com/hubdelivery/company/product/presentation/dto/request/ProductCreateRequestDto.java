package com.hubdelivery.company.product.presentation.dto.request;

import com.hubdelivery.company.product.domain.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "상품 생성 요청 DTO")
public record ProductCreateRequestDto(

        @Schema(name = "product_name", description = "상품명", example = "모니터A")
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
        String productName,

        @Schema(name = "hub_id", description = "상품 관리 허브 ID", example = "7b8e1a2c-4d5f-6a7b-8c9d-0e1f2a3b4c5d")
        @NotNull(message = "상품 관리 허브 ID는 필수입니다.")
        UUID hubId,

        @Schema(name = "company_id", description = "업체 ID", example = "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a")
        @NotNull(message = "업체 ID는 필수입니다.")
        UUID companyId
) {

    public Product toEntity() {
        return Product.builder()
                .productName(productName)
                .hubId(hubId)
                .companyId(companyId)
                .build();
    }
}
