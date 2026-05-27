package com.hubdelivery.hub;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.application.service.HubServiceV1;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubDuplicateLocationException;
import com.hubdelivery.hub.domain.exception.HubNotFoundException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("HubServiceV1 단위 테스트")
class HubServiceV1Test {

    @InjectMocks
    private HubServiceV1 hubService;

    @Mock
    private HubRepository hubRepository;

    @Spy
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private UUID userId;
    private UUID hubId;
    private UserRole role;
    private ReqHubDto reqHubDto;
    private HubEntity hubEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        hubId = UUID.randomUUID();
        role = UserRole.MASTER;

        reqHubDto = ReqHubDto.builder()
                .hub_name("서울허브")
                .address("서울시 강남구")
                .latitude(new BigDecimal("37.4979000"))
                .longitude(new BigDecimal("127.0276000"))
                .build();

        hubEntity = HubEntity.builder()
                .hubName("서울허브")
                .address("서울시 강남구")
                .latitude(new BigDecimal("37.4979000"))
                .longitude(new BigDecimal("127.0276000"))
                .build();

        // ID 강제 주입 (테스트용)
        ReflectionTestUtils.setField(hubEntity, "id", hubId);
    }

    @Nested
    @DisplayName("허브 생성 테스트")
    class CreateHubTest {

        @Test
        @DisplayName("정상적으로 허브를 생성한다")
        void createHub_Success() {
            // given
            given(hubRepository.existsByLatitudeAndLongitudeIsActive(
                    reqHubDto.getLatitude(),
                    reqHubDto.getLongitude()
            )).willReturn(false);
            given(hubRepository.save(any(HubEntity.class))).willReturn(hubEntity);

            // when
            ResGetHubDto result = hubService.createHub(reqHubDto, userId, role);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getHubName()).isEqualTo("서울허브");
            assertThat(result.getAddress()).isEqualTo("서울시 강남구");
            verify(hubRepository, times(1)).save(any(HubEntity.class));
        }

        @Test
        @DisplayName("중복된 위치로 허브 생성 시 예외가 발생한다")
        void createHub_DuplicateLocation_ThrowsException() {
            // given
            given(hubRepository.existsByLatitudeAndLongitudeIsActive(
                    reqHubDto.getLatitude(),
                    reqHubDto.getLongitude()
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() -> hubService.createHub(reqHubDto, userId, role))
                    .isInstanceOf(HubDuplicateLocationException.class)
                    .hasMessageContaining("이미 존재하는 위치입니다");

            verify(hubRepository, times(0)).save(any(HubEntity.class));
        }
    }

    @Nested
    @DisplayName("허브 조회 테스트")
    class GetHubTest {

        @Test
        @DisplayName("ID로 허브를 조회한다")
        void getHub_Success() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.of(hubEntity));

            // when
            ResGetHubDto result = hubService.getHub(hubId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getHubName()).isEqualTo("서울허브");
            verify(hubRepository, times(1)).findByIdActive(hubId);
        }

        @Test
        @DisplayName("존재하지 않는 허브 조회 시 예외가 발생한다")
        void getHub_NotFound_ThrowsException() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.getHub(hubId))
                    .isInstanceOf(HubNotFoundException.class)
                    .hasMessageContaining("허브를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("페이지네이션으로 전체 허브를 조회한다")
        void getHubs_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<HubEntity> hubPage = new PageImpl<>(List.of(hubEntity));
            given(hubRepository.searchHubs(null,pageable)).willReturn(hubPage);

            // when
            Page<ResGetHubDto> result = hubService.getHubs(null, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getHubName()).isEqualTo("서울허브");
            verify(hubRepository, times(1)).searchHubs(null,pageable);
        }

        @Test
        @DisplayName("빈 페이지를 반환한다")
        void getHubs_EmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<HubEntity> emptyPage = new PageImpl<>(List.of());
            String keyword="hub01";
            given(hubRepository.searchHubs(eq(keyword), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<ResGetHubDto> result = hubService.getHubs(keyword, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("허브 수정 테스트")
    class UpdateHubTest {

        @Test
        @DisplayName("정상적으로 허브를 수정한다")
        void updateHub_Success() {
            // given

            ReqHubDto updateDto = ReqHubDto.builder()
                    .hub_name("수정된허브")
                    .address("서울시 서초구")
                    .latitude(new BigDecimal("37.5000000"))
                    .longitude(new BigDecimal("127.0300000"))
                    .build();


            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.of(hubEntity));
            given(hubRepository.existsByLatitudeAndLongitudeExcludingId(
                    updateDto.getLatitude(),
                    updateDto.getLongitude(),
                    hubId
            )).willReturn(false);

            // when
            ResGetHubDto result = hubService.updateHub(hubId, updateDto, userId, role);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getHubName()).isEqualTo("수정된허브");
            assertThat(result.getAddress()).isEqualTo("서울시 서초구");
        }

        @Test
        @DisplayName("존재하지 않는 허브 수정 시 예외가 발생한다")
        void updateHub_NotFound_ThrowsException() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.updateHub(hubId, reqHubDto, userId, role))
                    .isInstanceOf(HubNotFoundException.class);
        }

        @Test
        @DisplayName("다른 허브와 위치가 중복되면 예외가 발생한다")
        void updateHub_DuplicateLocation_ThrowsException() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.of(hubEntity));
            given(hubRepository.existsByLatitudeAndLongitudeExcludingId(
                    reqHubDto.getLatitude(),
                    reqHubDto.getLongitude(),
                    hubId
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() -> hubService.updateHub(hubId, reqHubDto, userId, role))
                    .isInstanceOf(HubDuplicateLocationException.class);
        }
    }

    @Nested
    @DisplayName("허브 삭제 테스트")
    class DeleteHubTest {

        @Test
        @DisplayName("정상적으로 허브를 삭제한다 (Soft Delete)")
        void deleteHub_Success() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.of(hubEntity));

            // when
            hubService.deleteHub(hubId, userId, role);

            // then
            assertThat(hubEntity.isDeleted()).isTrue();
            assertThat(hubEntity.getDeletedBy()).isEqualTo(userId.toString());
            verify(hubRepository, times(1)).findByIdActive(hubId);
        }

        @Test
        @DisplayName("존재하지 않는 허브 삭제 시 예외가 발생한다")
        void deleteHub_NotFound_ThrowsException() {
            // given
            given(hubRepository.findByIdActive(hubId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.deleteHub(hubId, userId, role))
                    .isInstanceOf(HubNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("위치 중복 검증 테스트")
    class ValidateLocationTest {

        @Test
        @DisplayName("중복되지 않은 위치는 검증을 통과한다")
        void validateDuplicateLocation_NotDuplicate_Success() {
            // given
            BigDecimal latitude = new BigDecimal("37.4979000");
            BigDecimal longitude = new BigDecimal("127.0276000");
            given(hubRepository.existsByLatitudeAndLongitudeIsActive(latitude, longitude))
                    .willReturn(false);

            // when & then (예외가 발생하지 않음)
            hubService.validateDuplicateLocation(latitude, longitude);
        }

        @Test
        @DisplayName("중복된 위치는 예외가 발생한다")
        void validateDuplicateLocation_Duplicate_ThrowsException() {
            // given
            BigDecimal latitude = new BigDecimal("37.4979000");
            BigDecimal longitude = new BigDecimal("127.0276000");
            given(hubRepository.existsByLatitudeAndLongitudeIsActive(latitude, longitude))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> hubService.validateDuplicateLocation(latitude, longitude))
                    .isInstanceOf(HubDuplicateLocationException.class);
        }
    }
}
