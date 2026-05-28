package com.hubdelivery.user.presentation.dto.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserApproveRequest (

        @JsonProperty("hub_id")
        UUID hubId,

        @JsonProperty("company_id")
        UUID companyId

) {}
