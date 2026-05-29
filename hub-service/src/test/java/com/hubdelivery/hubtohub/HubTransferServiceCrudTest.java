package com.hubdelivery.hubtohub;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.application.service.HubRouteService;
import com.hubdelivery.hubtohub.application.service.HubTransferCacheService;
import com.hubdelivery.hubtohub.application.service.HubTransferCreateService;
import com.hubdelivery.hubtohub.application.service.HubTransferService;
import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import com.hubdelivery.hubtohub.domain.exception.HubTransferDuplicateLocationException;
import com.hubdelivery.hubtohub.domain.repository.HubToHubWaypointRepository;
import com.hubdelivery.hubtohub.domain.repository.HubTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("HubTransferService CRUD 단위 테스트")
class HubTransferServiceCrudTest {

    @Mock
    private HubTransferRepository hubTransferRepository;

    @Mock
    private HubToHubWaypointRepository waypointRepository;

    @Mock
    private HubTransferCacheService cacheService;

    @Mock
    private HubRouteService hubRouteService;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubTransferCreateService hubTransferCreateService;

    @InjectMocks
    private HubTransferService hubTransferService;

    private UUID fromHubId;
    private UUID toHubId;
    private UUID transferId;
    private UUID userId;
    private HubEntity fromHub;
    private HubEntity toHub;
    private HubTransferEntity hubTransferEntity;
    private ResGetHubTransferDto responseDto;

    @BeforeEach
    void setUp() {
        fromHubId = UUID.randomUUID();
        toHubId = UUID.randomUUID();
        transferId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // 출발 허브
        fromHub = HubEntity.builder()
                .hubName("서울 허브")
                .address("서울시 강남구")
                .latitude(BigDecimal.valueOf(37.4979))
                .longitude(BigDecimal.valueOf(127.0276))
                .build();
        ReflectionTestUtils.setField(fromHub, "id", fromHubId);

        // 도착 허브
        toHub = HubEntity.builder()
                .hubName("부산 허브")
                .address("부산시 부산진구")
                .latitude(BigDecimal.valueOf(35.1595))
                .longitude(BigDecimal.valueOf(129.1604))
                .build();
        ReflectionTestUtils.setField(toHub, "id", toHubId);

        // 허브 경로 Entity
        hubTransferEntity = HubTransferEntity.builder()
                .startHubId(fromHubId)
                .endHubId(toHubId)
                .distance(BigDecimal.valueOf(330.50))
                .durationSec(12000L)
                .build();
        ReflectionTestUtils.setField(hubTransferEntity, "id", transferId);

        // Response DTO
        responseDto = ResGetHubTransferDto.builder()
                .routeId(transferId)
                .startHubId(fromHubId)
                .endHubId(toHubId)
                .distanceKm(BigDecimal.valueOf(330.50))
                .durationMinutes(200)
                .durationSec(12000L)
                .waypoints(List.of())
                .build();
    }

    @Test
    @DisplayName("CREATE - 경로 생성 성공")
    void testCreateHubTransfer_Success() {
        // Given
        when(hubRepository.findByIdActive(fromHubId)).thenReturn(Optional.of(fromHub));
        when(hubRepository.findByIdActive(toHubId)).thenReturn(Optional.of(toHub));
        when(hubTransferRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .thenReturn(false);
        when(hubTransferCreateService.createAndSaveNewRoute(fromHub, toHub))
                .thenReturn(responseDto);

        // When
        ResGetHubTransferDto result = hubTransferService.createHubTransfer(userId, fromHubId, toHubId);

        // Then
        assertNotNull(result);
        assertEquals(transferId, result.getRouteId());
        assertEquals(fromHubId, result.getStartHubId());
        assertEquals(toHubId, result.getEndHubId());
        verify(hubTransferCreateService, times(1)).createAndSaveNewRoute(fromHub, toHub);
    }

    @Test
    @DisplayName("CREATE - 경로 생성 실패 (이미 존재하는 경로)")
    void testCreateHubTransfer_Duplicate() {
        // Given
        when(hubRepository.findByIdActive(fromHubId)).thenReturn(Optional.of(fromHub));
        when(hubRepository.findByIdActive(toHubId)).thenReturn(Optional.of(toHub));
        when(hubTransferRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(fromHubId, toHubId))
                .thenReturn(true);

        // When & Then
        assertThrows(HubTransferDuplicateLocationException.class, () ->
                hubTransferService.createHubTransfer(userId, fromHubId, toHubId)
        );
        verify(hubTransferCreateService, never()).createAndSaveNewRoute(any(), any());
    }

    @Test
    @DisplayName("READ - 경로 목록 조회 성공 (페이지네이션)")
    void testGetHubTransferList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<HubTransferEntity> entityPage = new PageImpl<>(
                List.of(hubTransferEntity),
                pageable,
                1
        );

        when(hubTransferRepository.findByFilters(
                eq(fromHubId),
                eq(toHubId),
                argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10)
        )).thenReturn(entityPage);
        when(hubTransferCreateService.buildResponseDtoFromDb(hubTransferEntity))
                .thenReturn(responseDto);

        // When
        PageResponse<ResGetHubTransferDto> result = hubTransferService.getHubTransferList(
                fromHubId, toHubId, 0, 10, "createdAt,desc", userId
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        verify(hubTransferRepository, times(1)).findByFilters(any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("READ - 경로 상세 조회 성공 (transferId로 조회)")
    void testGetHubRouteByTransferId_Success() {
        // Given
        when(hubTransferRepository.findActiveRouteById(transferId))
                .thenReturn(Optional.of(hubTransferEntity));
        when(hubRepository.findByIdActive(fromHubId)).thenReturn(Optional.of(fromHub));
        when(hubRepository.findByIdActive(toHubId)).thenReturn(Optional.of(toHub));
        when(cacheService.getRouteByTransferId(transferId)).thenReturn(null);
        when(hubTransferCreateService.buildResponseDtoFromDb(hubTransferEntity))
                .thenReturn(responseDto);

        // When
        ResGetHubTransferDto result = hubTransferService.getHubRouteByTransferId(transferId, userId);

        // Then
        assertNotNull(result);
        assertEquals(transferId, result.getRouteId());
        verify(hubTransferRepository, times(1)).findActiveRouteById(transferId);
        verify(hubTransferCreateService, times(1)).buildResponseDtoFromDb(hubTransferEntity);
    }

    @Test
    @DisplayName("UPDATE - 경로 업데이트 성공")
    void testUpdateHubRouteByTransferId_Success() {
        // Given
        when(hubTransferRepository.findById(transferId))
                .thenReturn(Optional.of(hubTransferEntity));
        when(hubRepository.findByIdActive(fromHubId)).thenReturn(Optional.of(fromHub));
        when(hubRepository.findByIdActive(toHubId)).thenReturn(Optional.of(toHub));
        when(hubRouteService.calculateRoute(fromHub, toHub))
                .thenReturn(createMockDirectionsResponse());
        when(hubTransferRepository.save(any(HubTransferEntity.class)))
                .thenReturn(hubTransferEntity);



        when(hubTransferCreateService.buildResponseDtoFromDb(hubTransferEntity))
                .thenReturn(responseDto);

        // When
        ResGetHubTransferDto result = hubTransferService.updateHubRouteByTransferId(transferId, userId);

        // Then
        assertNotNull(result);
        assertEquals(transferId, result.getRouteId());
        assertEquals(fromHubId, result.getStartHubId());
        assertEquals(toHubId, result.getEndHubId());
        verify(hubTransferRepository, times(1)).findById(transferId);
        verify(hubTransferRepository, times(1)).save(any(HubTransferEntity.class));
        verify(waypointRepository, times(1)).deleteByHubToHubId(transferId);
        verify(hubTransferCreateService, times(1)).buildResponseDtoFromDb(hubTransferEntity);
    }

    @Test
    @DisplayName("DELETE - 경로 삭제 성공 (Soft Delete)")
    void testDeleteHubRouteByTransferId_Success() {
        // Given
        when(hubTransferRepository.findById(transferId))
                .thenReturn(Optional.of(hubTransferEntity));
        when(waypointRepository.findByHubToHubIdOrderBySequence(transferId))
                .thenReturn(List.of());

        // When
        hubTransferService.deleteHubRouteByTransferId(userId, transferId);

        // Then
        verify(hubTransferRepository, times(1)).findById(transferId);
        verify(hubTransferRepository, times(1)).save(any(HubTransferEntity.class));
        verify(waypointRepository, times(1)).findByHubToHubIdOrderBySequence(transferId);
        verify(waypointRepository, times(1)).saveAll(any());
        verify(cacheService, times(1)).evictRoute(fromHubId, toHubId);
        verify(cacheService, times(1)).evictRouteByTransferId(transferId);
    }

    // Helper method
    private com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse createMockDirectionsResponse() {
        var response = new com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse();
        var route = new com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse.Route();
        var summary = new com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse.Summary();

        // Summary 설정
        summary.setDistance(330500);  // 330.5km
        summary.setDuration(12000);   // 12000초

        // Route 설정
        route.setResultCode(0);
        route.setSummary(summary);
        route.setSections(List.of());

        // Response 설정
        response.setRoutes(List.of(route));

        return response;
    }

}
