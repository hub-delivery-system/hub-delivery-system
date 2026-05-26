package com.hubdelivery.hub.application.service;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HubService {


    public ResGetHubDto createHub(ReqHubDto reqHubDto,UUID userId, UserRole role);

    public Page<ResGetHubDto> getHubs(String keyword, Pageable pageable);

    public ResGetHubDto getHub(UUID hubId);


    // RequestDTO 생성 후  수정
    public ResGetHubDto updateHub(UUID hubId, ReqHubDto reqHubDto,UUID userId, UserRole role);

    public void deleteHub(UUID hubId,UUID userId, UserRole role);
}
