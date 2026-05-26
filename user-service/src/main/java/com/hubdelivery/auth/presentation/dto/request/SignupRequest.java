package com.hubdelivery.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.user.domain.type.AffiliationType;

public record SignupRequest (

    @NotBlank(message = "슬랙 아이디는 필수입니다.")
    @Size(max = 20)
    @JsonProperty("slack_id")
    String slackId,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,15}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    String password,

    @NotBlank(message = "유저 이름은 필수입니다.")
    @Size(min = 4, max = 10, message = "유저 이름은 4자 이상 10자 이하여야 합니다.")
    @Pattern(
            regexp = "^[a-z0-9]{4,10}$",
            message = "유저 이름은 소문자(a-z)와 숫자(0-9)만 사용할 수 있습니다."
    )
    String username,

    @NotNull(message = "타입은 필수입니다.")
    @JsonProperty("affiliation_type")
    AffiliationType affiliationType,

    @NotBlank(message = "허브 이름 또는 업체 이름은 필수입니다.")
    @Size(max = 100)
    @JsonProperty("affiliation_name")
    String affiliationName

) {}
