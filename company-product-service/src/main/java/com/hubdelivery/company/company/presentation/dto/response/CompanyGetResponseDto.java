package com.hubdelivery.company.company.presentation.dto.response;

import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.type.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "업체 응답 DTO")
public record CompanyGetResponseDto(

        @Schema(name = "id", description = "업체 ID", example = "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a")
        UUID id,

        @Schema(name = "company_name", description = "업체명", example = "모니터 업체")
        String companyName,

        @Schema(name = "company_type", description = "업체 타입", example = "PRODUCER")
        CompanyType companyType,

        @Schema(name = "hub_id", description = "업체 관리 허브 ID", example = "7b8e1a2c-4d5f-6a7b-8c9d-0e1f2a3b4c5d")
        UUID hubId,

        @Schema(name = "address", description = "업체 주소", example = "경기도 고양시 일산동구 정발산로 24")
        String address,

        @Schema(name = "created_at", description = "생성 일시", example = "2026-05-22T10:15:30")
        LocalDateTime createdAt,

        @Schema(name = "created_by", description = "생성자", example = "SYSTEM")
        String createdBy,

        @Schema(name = "updated_at", description = "수정 일시", example = "2026-05-22T10:15:30")
        LocalDateTime updatedAt,

        @Schema(name = "updated_by", description = "수정자", example = "SYSTEM")
        String updatedBy
) {

    public static CompanyGetResponseDto from(Company company) {
        return new CompanyGetResponseDto(
                company.getId(),
                company.getCompanyName(),
                company.getCompanyType(),
                company.getHubId(),
                company.getAddress(),
                company.getCreatedAt(),
                company.getCreatedBy(),
                company.getUpdatedAt(),
                company.getUpdatedBy()
        );
    }
}
