package com.hubdelivery.company.product.presentation.dto.response;

import com.hubdelivery.company.product.domain.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "상품 응답 DTO")
public record ProductResponseDto(

        @Schema(name = "id", description = "상품 ID", example = "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f")
        UUID id,

        @Schema(name = "product_name", description = "상품명", example = "모니터A")
        String productName,

        @Schema(name = "hub_id", description = "상품 관리 허브 ID", example = "7b8e1a2c-4d5f-6a7b-8c9d-0e1f2a3b4c5d")
        UUID hubId,

        @Schema(name = "company_id", description = "업체 ID", example = "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a")
        UUID companyId,

        @Schema(name = "created_at", description = "생성 일시", example = "2026-05-22T10:15:30")
        LocalDateTime createdAt,

        @Schema(name = "created_by", description = "생성자", example = "SYSTEM")
        String createdBy,

        @Schema(name = "updated_at", description = "수정 일시", example = "2026-05-22T10:15:30")
        LocalDateTime updatedAt,

        @Schema(name = "updated_by", description = "수정자", example = "SYSTEM")
        String updatedBy
) {

    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getProductName(),
                product.getHubId(),
                product.getCompanyId(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy()
        );
    }
}
