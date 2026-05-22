package com.hubdelivery.hub.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReqHubDto {

    @NotBlank(message = "허브 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "허브 이름은 1~100자여야 합니다")
    private String hub_name;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(min = 1, max = 255, message = "허브 이름은 1~255자여야 합니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0000000", inclusive = true, message = "위도는 -90 이상이어야 합니다")
    @DecimalMax(value = "90.0000000", inclusive = true, message = "위도는 90 이하여야 합니다")
    @Digits(integer = 2, fraction = 7, message = "위도는 정수 2자리, 소수 7자리여야 합니다")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0000000", inclusive = true, message = "경도는 -180 이상이어야 합니다")
    @DecimalMax(value = "180.0000000", inclusive = true, message = "경도는 180 이하여야 합니다")
    @Digits(integer = 2, fraction = 7, message = "위도는 정수 2자리, 소수 7자리여야 합니다")
    private BigDecimal longitude;


}
