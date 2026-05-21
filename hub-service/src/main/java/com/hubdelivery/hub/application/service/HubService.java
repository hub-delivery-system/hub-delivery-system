package com.hubdelivery.hub.application.service;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;

import java.util.List;
import java.util.UUID;

public interface HubService {


    public ResGetHubDto createHub(ReqHubDto reqHubDto);

    public List<ResGetHubDto> getHubs();

    public ResGetHubDto getHub(Long hubId);


    // RequestDTO 생성 후  수정
    public ResGetHubDto updateHub(UUID hubId, ResGetHubDto hub);

    public void deleteHub(Long hubId);
}
