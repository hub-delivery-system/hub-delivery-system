package com.hubdelivery.company.company.presentation.dto.request;

import com.hubdelivery.company.company.domain.type.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "업체 수정 요청 DTO")
public record CompanyUpdateRequestDto(

        @Schema(name = "company_name", description = "업체명", example = "모니터 업체")
        @NotBlank(message = "업체명은 필수입니다.")
        @Size(max = 100, message = "업체명은 100자를 초과할 수 없습니다.")
        String companyName,

        @Schema(name = "company_type", description = "업체 타입", example = "PRODUCER", allowableValues = {"PRODUCER", "RECEIVER"})
        @NotNull(message = "업체 타입은 필수입니다.")
        CompanyType companyType,

        @Schema(name = "hub_id", description = "업체 관리 허브 ID", example = "7b8e1a2c-4d5f-6a7b-8c9d-0e1f2a3b4c5d")
        @NotNull(message = "관리 허브 ID는 필수입니다.")
        UUID hubId,

        @Schema(name = "address", description = "업체 주소", example = "경기도 고양시 일산동구 정발산로 24")
        @NotBlank(message = "업체 주소는 필수입니다.")
        @Size(max = 255, message = "업체 주소는 255자를 초과할 수 없습니다.")
        String address
) {
}
