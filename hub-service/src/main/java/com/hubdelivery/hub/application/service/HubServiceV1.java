package com.hubdelivery.hub.application.service;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubDuplicateLocationException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import com.thoughtworks.xstream.core.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional( readOnly = true)
public class HubServiceV1 implements HubService {

    private HubRepository hubRepository;


    @Override
    @Transactional
    public ResGetHubDto createHub(ReqHubDto reqHubDto) {

        if (hubRepository.existsByLatitudeAndLongitude(
                reqHubDto.getLatitude(),
                reqHubDto.getLongitude()
        )) {
            throw new HubDuplicateLocationException();
        }


        HubEntity hubEntity = HubEntity.builder()
                .hubName(reqHubDto.getName())
                .address(reqHubDto.getAddress())
                .latitude(reqHubDto.getLatitude())
                .longitude(reqHubDto.getLongitude())
                .build();
        hubRepository.save(hubEntity);
        return ResGetHubDto.from(hubEntity);
    }

    @Override
    public List<ResGetHubDto> getHubs() {
        List<HubEntity> hubEntities = hubRepository.findAll();
        return List.of();
    }

    @Override
    public ResGetHubDto getHub(Long hubId) {
        return null;
    }

    @Override
    public ResGetHubDto updateHub(UUID hubId, ResGetHubDto hub) {
        return null;
    }

    @Override
    public void deleteHub(Long hubId) {

    }
}
