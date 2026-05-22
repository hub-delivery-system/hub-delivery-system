package com.hubdelivery.hub.application.service;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubDuplicateLocationException;
import com.hubdelivery.hub.domain.exception.HubNotFoundException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional( readOnly = true)
@RequiredArgsConstructor
public class HubServiceV1 implements HubService {

    private HubRepository hubRepository;


    @Override
    @Transactional
    public ResGetHubDto createHub(ReqHubDto reqHubDto) {

        validateDuplicateLocation(reqHubDto.getLatitude(), reqHubDto.getLongitude());


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
    public Page<ResGetHubDto> getHubs(Pageable pageable) {
        return hubRepository.findAllActive(pageable)
                .map(ResGetHubDto::from);
    }

    @Override
    public ResGetHubDto getHub(UUID hubId) {
        HubEntity hubEntity = getHubEntityByHubId(hubId);
        return ResGetHubDto.from(hubEntity);
    }



    @Override
    @Transactional
    public ResGetHubDto updateHub(UUID hubId, ReqHubDto reqHubDto) {
        HubEntity hubEntity = getHubEntityByHubId(hubId);

        validateDuplicateLocationExcludingSelf(
                reqHubDto.getLatitude(),
                reqHubDto.getLongitude(),
                hubId
        );

        hubEntity.update(
                reqHubDto.getName(),
                reqHubDto.getAddress(),
                reqHubDto.getLatitude(),
                reqHubDto.getLongitude());
        return ResGetHubDto.from(hubEntity);
    }

    @Override
    public void deleteHub(UUID hubId) {
        HubEntity hubEntity=getHubEntityByHubId(hubId);
        hubEntity.softDelete("user");

    }

    public HubEntity getHubEntityByHubId(UUID hubId) {
        return hubRepository.findById(hubId).orElseThrow(
                HubNotFoundException::new
                );
    }

    public void validateDuplicateLocation(BigDecimal latitude, BigDecimal longitude) {
        if (hubRepository.existsByLatitudeAndLongitude(
                latitude,
                longitude
        )) {
            throw new HubDuplicateLocationException();
        }
    }

    private void validateDuplicateLocationExcludingSelf(
            BigDecimal latitude,
            BigDecimal longitude,
            UUID excludeId
    ) {
        if (hubRepository.existsByLatitudeAndLongitudeExcludingId(
                latitude, longitude, excludeId
        )) {
            throw new HubDuplicateLocationException();
        }
    }


}
