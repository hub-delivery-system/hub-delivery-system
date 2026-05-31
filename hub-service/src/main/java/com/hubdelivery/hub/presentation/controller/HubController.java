package com.hubdelivery.hub.presentation.controller;


import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.application.service.HubService;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubController implements HubControllerDocs{

    private final HubService hubService;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<ResGetHubDto> createHub(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @Valid @RequestBody ReqHubDto request
    ) {
        return ApiResponse.created(hubService.createHub(request, userId,userRole));
    }

    @GetMapping("/{hub_id}")
    public ApiResponse<ResGetHubDto> getHub(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable("hub_id") UUID hubId
    ){
        return ApiResponse.ok(hubService.getHub(hubId));
    }

    @GetMapping
    public ApiResponse<PageResponse<ResGetHubDto>> getHubs(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @RequestParam(required = false) String keyword,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ){
        Pageable validatedPageable = PageableUtils.createPageable(pageable.getPageNumber(), pageable.getPageSize());

        return ApiResponse.ok(PageResponse.from(hubService.getHubs(keyword,validatedPageable)));
    }

    @PutMapping("/{hub_id}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<ResGetHubDto> updateHub(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable("hub_id") UUID hubId,
            @Valid @RequestBody ReqHubDto request
    ){
        return ApiResponse.ok(hubService.updateHub(hubId,request,userId,userRole));
    }

    @DeleteMapping("/{hub_id}")
    @PreAuthorize("hasRole('MASTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<ResGetHubDto> deleteHub(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable("hub_id") UUID hubId
    ){
        hubService.deleteHub(hubId,userId,userRole);
        return new ApiResponse<>(204, null, "NO_CONTENT", null, null);
    }



}
