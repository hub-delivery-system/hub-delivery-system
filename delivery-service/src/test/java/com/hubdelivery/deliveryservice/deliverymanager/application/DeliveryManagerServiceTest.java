package com.hubdelivery.deliveryservice.deliverymanager.application;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerErrorCode;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerException;
import com.hubdelivery.deliveryservice.deliverymanager.domain.repository.DeliveryManagerRepository;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.HubServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto.UserResponse;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerCreateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DeliveryManagerServiceTest {

    @InjectMocks
    private DeliveryManagerService deliveryManagerService;

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @Mock
    private HubServiceClient hubServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    // -----------------------------------------------------------------------
    // 테스트 헬퍼
    // -----------------------------------------------------------------------

    /** 테스트용 DeliveryManager 빌드 (리플렉션으로 id 설정) */
    private DeliveryManager buildManager(UUID id, DeliveryManagerType type, UUID hubId, int sequence) {
        DeliveryManager manager = DeliveryManager.builder()
                .userId(UUID.randomUUID())
                .hubId(hubId)
                .type(type)
                .sequence(sequence)
                .build();
        try {
            var field = DeliveryManager.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(manager, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return manager;
    }

    /** 테스트용 DeliveryManagerCreateRequest 빌드 (리플렉션으로 필드 설정) */
    private DeliveryManagerCreateRequest buildCreateRequest(DeliveryManagerType type, UUID hubId) {
        try {
            var request = new DeliveryManagerCreateRequest();
            setField(request, "userId", UUID.randomUUID());
            setField(request, "type", type);
            setField(request, "hubId", hubId);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -----------------------------------------------------------------------
    // 순번 자동 배정
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("순번 자동 배정")
    class SequenceAssignment {

        @Test
        @DisplayName("HUB_DELIVERY 생성 시 전체 최대 순번 + 1 로 배정된다")
        void create_hubDelivery_sequenceIsMaxByTypePlusOne() {
            // given
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.HUB_DELIVERY, null);
            String userId = UUID.randomUUID().toString();

            given(deliveryManagerRepository.findAllByTypeForUpdate(DeliveryManagerType.HUB_DELIVERY))
                    .willReturn(Collections.emptyList());
            given(deliveryManagerRepository.findMaxSequenceByType(DeliveryManagerType.HUB_DELIVERY))
                    .willReturn(5);

            DeliveryManager saved = buildManager(UUID.randomUUID(), DeliveryManagerType.HUB_DELIVERY, null, 6);
            given(deliveryManagerRepository.save(any())).willReturn(saved);

            // when
            DeliveryManagerResponse response = deliveryManagerService.create(request, userId, UserRole.MASTER);

            // then
            assertThat(response.getSequence()).isEqualTo(6);
        }

        @Test
        @DisplayName("COMPANY_DELIVERY 생성 시 허브+타입 기준 최대 순번 + 1 로 배정된다")
        void create_companyDelivery_sequenceIsMaxByHubAndTypePlusOne() {
            // given
            UUID hubId = UUID.randomUUID();
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.COMPANY_DELIVERY, hubId);
            String userId = UUID.randomUUID().toString();

            given(hubServiceClient.getHub(hubId)).willReturn(null); // 허브 존재 검증 통과
            given(deliveryManagerRepository.findAllByHubIdAndTypeForUpdate(hubId, DeliveryManagerType.COMPANY_DELIVERY))
                    .willReturn(Collections.emptyList());
            given(deliveryManagerRepository.findMaxSequence(hubId, DeliveryManagerType.COMPANY_DELIVERY))
                    .willReturn(3);

            DeliveryManager saved = buildManager(UUID.randomUUID(), DeliveryManagerType.COMPANY_DELIVERY, hubId, 4);
            given(deliveryManagerRepository.save(any())).willReturn(saved);

            // when
            DeliveryManagerResponse response = deliveryManagerService.create(request, userId, UserRole.MASTER);

            // then
            assertThat(response.getSequence()).isEqualTo(4);
        }

        @Test
        @DisplayName("첫 담당자 등록 시 순번은 1이 된다 (DB에 담당자 없으면 findMax = 0)")
        void create_firstManager_sequenceIsOne() {
            // given
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.HUB_DELIVERY, null);
            String userId = UUID.randomUUID().toString();

            given(deliveryManagerRepository.findAllByTypeForUpdate(DeliveryManagerType.HUB_DELIVERY))
                    .willReturn(Collections.emptyList());
            given(deliveryManagerRepository.findMaxSequenceByType(DeliveryManagerType.HUB_DELIVERY))
                    .willReturn(0); // 담당자 없으면 COALESCE 로 0 반환

            DeliveryManager saved = buildManager(UUID.randomUUID(), DeliveryManagerType.HUB_DELIVERY, null, 1);
            given(deliveryManagerRepository.save(any())).willReturn(saved);

            // when
            DeliveryManagerResponse response = deliveryManagerService.create(request, userId, UserRole.MASTER);

            // then
            assertThat(response.getSequence()).isEqualTo(1);
        }

        @Test
        @DisplayName("정원(10명) 초과 시 CAPACITY_EXCEEDED 예외가 발생한다")
        void create_capacityExceeded_throwsException() {
            // given
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.HUB_DELIVERY, null);
            String userId = UUID.randomUUID().toString();

            List<DeliveryManager> fullList = Collections.nCopies(10,
                    buildManager(UUID.randomUUID(), DeliveryManagerType.HUB_DELIVERY, null, 1));
            given(deliveryManagerRepository.findAllByTypeForUpdate(DeliveryManagerType.HUB_DELIVERY))
                    .willReturn(fullList);

            // when & then
            assertThatThrownBy(() ->
                    deliveryManagerService.create(request, userId, UserRole.MASTER))
                    .isInstanceOf(DeliveryManagerException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryManagerErrorCode.CAPACITY_EXCEEDED);
        }
    }

    // -----------------------------------------------------------------------
    // 순환 배정
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("순환 배정")
    class RoundRobinAssignment {

        @Test
        @DisplayName("현재 순번 이후 담당자가 있으면 해당 담당자를 반환한다")
        void assignNext_returnsNextManager() {
            // given
            UUID hubId = UUID.randomUUID();
            int currentSeq = 3;
            DeliveryManager nextManager = buildManager(UUID.randomUUID(),
                    DeliveryManagerType.COMPANY_DELIVERY, hubId, 5);

            given(deliveryManagerRepository.findNextAfter(
                    eq(hubId), eq(DeliveryManagerType.COMPANY_DELIVERY), eq(currentSeq), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(nextManager)));

            // when
            DeliveryManagerResponse response = deliveryManagerService.assignNext(
                    hubId, DeliveryManagerType.COMPANY_DELIVERY, currentSeq);

            // then
            assertThat(response.getSequence()).isEqualTo(5);
        }

        @Test
        @DisplayName("현재 순번 이후 담당자가 없으면 처음 담당자로 wrap-around 한다")
        void assignNext_wrapsAroundToFirst() {
            // given
            UUID hubId = UUID.randomUUID();
            int currentSeq = 10;
            DeliveryManager firstManager = buildManager(UUID.randomUUID(),
                    DeliveryManagerType.COMPANY_DELIVERY, hubId, 1);

            given(deliveryManagerRepository.findNextAfter(
                    eq(hubId), eq(DeliveryManagerType.COMPANY_DELIVERY), eq(currentSeq), any(Pageable.class)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerRepository.findFirstByHubIdAndType(
                    eq(hubId), eq(DeliveryManagerType.COMPANY_DELIVERY), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(firstManager)));

            // when
            DeliveryManagerResponse response = deliveryManagerService.assignNext(
                    hubId, DeliveryManagerType.COMPANY_DELIVERY, currentSeq);

            // then
            assertThat(response.getSequence()).isEqualTo(1);
        }

        @Test
        @DisplayName("배정 가능한 담당자가 없으면 NO_AVAILABLE_MANAGER 예외가 발생한다")
        void assignNext_noManager_throwsNoAvailableManager() {
            // given
            UUID hubId = UUID.randomUUID();

            given(deliveryManagerRepository.findNextAfter(any(), any(), anyInt(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerRepository.findFirstByHubIdAndType(any(), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));

            // when & then
            assertThatThrownBy(() ->
                    deliveryManagerService.assignNext(hubId, DeliveryManagerType.COMPANY_DELIVERY, 0))
                    .isInstanceOf(DeliveryManagerException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryManagerErrorCode.NO_AVAILABLE_MANAGER);
        }

        @Test
        @DisplayName("HUB_DELIVERY 전용 순환 배정 - 현재 순번 이후 담당자 반환")
        void assignNextByType_returnsNextHubDeliveryManager() {
            // given
            int currentSeq = 2;
            DeliveryManager nextManager = buildManager(UUID.randomUUID(),
                    DeliveryManagerType.HUB_DELIVERY, null, 4);

            given(deliveryManagerRepository.findNextByTypeAfter(
                    eq(DeliveryManagerType.HUB_DELIVERY), eq(currentSeq), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(nextManager)));

            // when
            DeliveryManagerResponse response = deliveryManagerService.assignNextByType(
                    DeliveryManagerType.HUB_DELIVERY, currentSeq);

            // then
            assertThat(response.getSequence()).isEqualTo(4);
        }

        @Test
        @DisplayName("HUB_DELIVERY 전용 순환 배정 - wrap-around")
        void assignNextByType_wrapsAround() {
            // given
            int currentSeq = 99;
            DeliveryManager firstManager = buildManager(UUID.randomUUID(),
                    DeliveryManagerType.HUB_DELIVERY, null, 1);

            given(deliveryManagerRepository.findNextByTypeAfter(
                    eq(DeliveryManagerType.HUB_DELIVERY), eq(currentSeq), any(Pageable.class)))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerRepository.findFirstByType(
                    eq(DeliveryManagerType.HUB_DELIVERY), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(firstManager)));

            // when
            DeliveryManagerResponse response = deliveryManagerService.assignNextByType(
                    DeliveryManagerType.HUB_DELIVERY, currentSeq);

            // then
            assertThat(response.getSequence()).isEqualTo(1);
        }
    }

    // -----------------------------------------------------------------------
    // 권한 제어
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("권한 제어")
    class AuthorizationTest {

        @Test
        @DisplayName("DELIVERY_MANAGER 역할로 담당자 생성 시 MANAGER_FORBIDDEN 예외 발생")
        void create_deliveryManagerRole_throwsForbidden() {
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.HUB_DELIVERY, null);

            assertThatThrownBy(() ->
                    deliveryManagerService.create(request, UUID.randomUUID().toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(DeliveryManagerException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
        }

        @Test
        @DisplayName("COMPANY_MANAGER 역할로 담당자 생성 시 MANAGER_FORBIDDEN 예외 발생")
        void create_companyManagerRole_throwsForbidden() {
            DeliveryManagerCreateRequest request = buildCreateRequest(DeliveryManagerType.HUB_DELIVERY, null);

            assertThatThrownBy(() ->
                    deliveryManagerService.create(request, UUID.randomUUID().toString(), UserRole.COMPANY_MANAGER))
                    .isInstanceOf(DeliveryManagerException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
        }

        @Test
        @DisplayName("HUB_MANAGER가 다른 허브의 담당자 삭제 시도 시 MANAGER_FORBIDDEN 예외 발생")
        void delete_hubManagerOtherHub_throwsForbidden() {
            // given
            UUID managerId = UUID.randomUUID();
            UUID managerHubId = UUID.randomUUID();
            UUID otherHubId = UUID.randomUUID();   // 담당자 소속 허브 ≠ 요청자 허브
            String requestUserId = UUID.randomUUID().toString();

            DeliveryManager manager = buildManager(managerId, DeliveryManagerType.COMPANY_DELIVERY, otherHubId, 1);
            given(deliveryManagerRepository.findByIdAndDeletedAtIsNull(managerId))
                    .willReturn(java.util.Optional.of(manager));

            UserResponse userResponse = mock(UserResponse.class);
            given(userResponse.getHubId()).willReturn(managerHubId); // 요청자 허브
            given(userServiceClient.getUser(any()))
                    .willReturn(ApiResponse.ok(userResponse));

            // when & then
            assertThatThrownBy(() ->
                    deliveryManagerService.delete(managerId, requestUserId, UserRole.HUB_MANAGER))
                    .isInstanceOf(DeliveryManagerException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
        }
    }
}
