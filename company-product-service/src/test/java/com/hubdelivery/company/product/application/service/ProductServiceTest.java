package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.domain.type.CompanyType;
import com.hubdelivery.company.global.infrastructure.client.HubClient;
import com.hubdelivery.company.global.infrastructure.client.dto.HubResponse;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.ProductCompanyNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductHubIntegrationException;
import com.hubdelivery.company.product.domain.exception.ProductHubNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductStockNotEnoughException;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductStockUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HubClient hubClient;

    private final UUID userId = UUID.randomUUID();
    private final UserRole userRole = UserRole.MASTER;

    @Nested
    @DisplayName("상품 생성")
    class CreateProduct {

        @Test
        @DisplayName("상품 생성 성공")
        void createProduct_success() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            ProductCreateRequestDto request = createRequest("모니터A", hubId, companyId, 100);

            givenHubExists(hubId);
            givenCompanyExists(companyId);
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> {
                        Product product = invocation.getArgument(0);
                        setId(product, UUID.randomUUID());
                        return product;
                    });

            // when
            ProductResponseDto response = productService.createProduct(userId, userRole, request);

            // then
            assertThat(response.productName()).isEqualTo("모니터A");
            assertThat(response.hubId()).isEqualTo(hubId);
            assertThat(response.companyId()).isEqualTo(companyId);
            assertThat(response.stockQuantity()).isEqualTo(100);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().getProductName()).isEqualTo("모니터A");
            assertThat(captor.getValue().getStockQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("상품 생성 실패 - 존재하지 않는 허브")
        void createProduct_hubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            ProductCreateRequestDto request = createRequest("모니터A", hubId, companyId, 100);

            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(404));

            // when & then
            assertThatThrownBy(() -> productService.createProduct(userId, userRole, request))
                    .isInstanceOf(ProductHubNotFoundException.class);

            verifyNoInteractions(companyRepository);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("상품 생성 실패 - 허브 서버 연동 실패")
        void createProduct_hubIntegrationFailed() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            ProductCreateRequestDto request = createRequest("모니터A", hubId, companyId, 100);

            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(500));

            // when & then
            assertThatThrownBy(() -> productService.createProduct(userId, userRole, request))
                    .isInstanceOf(ProductHubIntegrationException.class);

            verifyNoInteractions(companyRepository);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("상품 생성 실패 - 존재하지 않는 업체")
        void createProduct_companyNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            ProductCreateRequestDto request = createRequest("모니터A", hubId, companyId, 100);

            givenHubExists(hubId);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.createProduct(userId, userRole, request))
                    .isInstanceOf(ProductCompanyNotFoundException.class);

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("상품 단건 조회")
    class GetProduct {

        @Test
        @DisplayName("상품 단건 조회 성공")
        void getProduct_success() {
            // given
            UUID productId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            Product product = product(productId, "모니터A", hubId, companyId, 100);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));

            // when
            ProductResponseDto response = productService.getProduct(productId);

            // then
            assertThat(response.id()).isEqualTo(productId);
            assertThat(response.productName()).isEqualTo("모니터A");
            assertThat(response.stockQuantity()).isEqualTo(100);
        }

        @Test
        @DisplayName("상품 단건 조회 실패 - 존재하지 않는 상품")
        void getProduct_notFound() {
            // given
            UUID productId = UUID.randomUUID();
            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(productId))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 전체 조회")
    class GetAllProducts {

        @Test
        @DisplayName("검색어를 정규화하고 허용되지 않은 페이지 크기와 정렬 필드는 기본값으로 보정")
        void getAllProducts_normalizeSearchOptions() {
            // given
            Product product = product(UUID.randomUUID(), "모니터A", UUID.randomUUID(), UUID.randomUUID(), 100);

            given(productRepository.searchProducts(eq("모니터"), any(Pageable.class)))
                    .willAnswer(invocation -> {
                        Pageable pageable = invocation.getArgument(1);
                        return new PageImpl<>(List.of(product), pageable, 1);
                    });

            // when
            PageResponse<ProductResponseDto> response = productService.getAllProducts("  모니터  ", -1, 99, "id,asc");

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.sort()).isEqualTo("createdAt, ASC");

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(productRepository).searchProducts(eq("모니터"), captor.capture());
            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class UpdateProduct {

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_success() {
            // given
            UUID productId = UUID.randomUUID();
            UUID oldHubId = UUID.randomUUID();
            UUID oldCompanyId = UUID.randomUUID();
            UUID newHubId = UUID.randomUUID();
            UUID newCompanyId = UUID.randomUUID();
            Product product = product(productId, "기존 상품", oldHubId, oldCompanyId, 100);
            ProductUpdateRequestDto request = updateRequest("수정 상품", newHubId, newCompanyId, 50);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));
            givenHubExists(newHubId);
            givenCompanyExists(newCompanyId);

            // when
            ProductResponseDto response = productService.updateProduct(userId, userRole, productId, request);

            // then
            assertThat(response.id()).isEqualTo(productId);
            assertThat(response.productName()).isEqualTo("수정 상품");
            assertThat(response.hubId()).isEqualTo(newHubId);
            assertThat(response.companyId()).isEqualTo(newCompanyId);
            assertThat(response.stockQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
        void updateProduct_productNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            ProductUpdateRequestDto request = updateRequest("수정 상품", UUID.randomUUID(), UUID.randomUUID(), 50);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(userId, userRole, productId, request))
                    .isInstanceOf(ProductNotFoundException.class);

            verifyNoInteractions(hubClient);
            verifyNoInteractions(companyRepository);
        }

        @Test
        @DisplayName("상품 수정 실패 - 변경할 허브가 존재하지 않음")
        void updateProduct_hubNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            Product product = product(productId, "기존 상품", UUID.randomUUID(), UUID.randomUUID(), 100);
            ProductUpdateRequestDto request = updateRequest("수정 상품", hubId, UUID.randomUUID(), 50);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));
            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(404));

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(userId, userRole, productId, request))
                    .isInstanceOf(ProductHubNotFoundException.class);

            verifyNoInteractions(companyRepository);
        }

        @Test
        @DisplayName("상품 수정 실패 - 변경할 업체가 존재하지 않음")
        void updateProduct_companyNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            Product product = product(productId, "기존 상품", UUID.randomUUID(), UUID.randomUUID(), 100);
            ProductUpdateRequestDto request = updateRequest("수정 상품", hubId, companyId, 50);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));
            givenHubExists(hubId);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(userId, userRole, productId, request))
                    .isInstanceOf(ProductCompanyNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class DeleteProduct {

        @Test
        @DisplayName("상품 삭제 성공")
        void deleteProduct_success() {
            // given
            UUID productId = UUID.randomUUID();
            Product product = product(productId, "모니터A", UUID.randomUUID(), UUID.randomUUID(), 100);

            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));

            // when
            productService.deleteProduct(userId, userRole, productId);

            // then
            assertThat(product.isDeleted()).isTrue();
            assertThat(product.getDeletedBy()).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
        void deleteProduct_productNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            given(productRepository.findByIdAndDeletedAtIsNull(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(userId, userRole, productId))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("상품 재고 감소")
    class DecreaseStock {

        @Test
        @DisplayName("상품 재고 감소 성공")
        void decreaseStock_success() {
            // given
            UUID productId = UUID.randomUUID();
            Product product = product(productId, "모니터A", UUID.randomUUID(), UUID.randomUUID(), 100);
            ProductStockUpdateRequestDto request = stockRequest(30);

            given(productRepository.findLockedByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));

            // when
            ProductResponseDto response = productService.decreaseStock(userId, userRole, productId, request);

            // then
            assertThat(response.stockQuantity()).isEqualTo(70);
            assertThat(product.getStockQuantity()).isEqualTo(70);
        }

        @Test
        @DisplayName("상품 재고 감소 실패 - 존재하지 않는 상품")
        void decreaseStock_productNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            ProductStockUpdateRequestDto request = stockRequest(30);

            given(productRepository.findLockedByIdAndDeletedAtIsNull(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.decreaseStock(userId, userRole, productId, request))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("상품 재고 감소 실패 - 재고 부족")
        void decreaseStock_notEnoughStock() {
            // given
            UUID productId = UUID.randomUUID();
            Product product = product(productId, "모니터A", UUID.randomUUID(), UUID.randomUUID(), 10);
            ProductStockUpdateRequestDto request = stockRequest(30);

            given(productRepository.findLockedByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));

            // when & then
            assertThatThrownBy(() -> productService.decreaseStock(userId, userRole, productId, request))
                    .isInstanceOf(ProductStockNotEnoughException.class);

            assertThat(product.getStockQuantity()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("상품 재고 증가")
    class IncreaseStock {

        @Test
        @DisplayName("상품 재고 증가 성공")
        void increaseStock_success() {
            // given
            UUID productId = UUID.randomUUID();
            Product product = product(productId, "모니터A", UUID.randomUUID(), UUID.randomUUID(), 100);
            ProductStockUpdateRequestDto request = stockRequest(30);

            given(productRepository.findLockedByIdAndDeletedAtIsNull(productId)).willReturn(Optional.of(product));

            // when
            ProductResponseDto response = productService.increaseStock(userId, userRole, productId, request);

            // then
            assertThat(response.stockQuantity()).isEqualTo(130);
            assertThat(product.getStockQuantity()).isEqualTo(130);
        }

        @Test
        @DisplayName("상품 재고 증가 실패 - 존재하지 않는 상품")
        void increaseStock_productNotFound() {
            // given
            UUID productId = UUID.randomUUID();
            ProductStockUpdateRequestDto request = stockRequest(30);

            given(productRepository.findLockedByIdAndDeletedAtIsNull(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.increaseStock(userId, userRole, productId, request))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    private ProductCreateRequestDto createRequest(String name, UUID hubId, UUID companyId, int stockQuantity) {
        return new ProductCreateRequestDto(name, hubId, companyId, stockQuantity);
    }

    private ProductUpdateRequestDto updateRequest(String name, UUID hubId, UUID companyId, int stockQuantity) {
        return new ProductUpdateRequestDto(name, hubId, companyId, stockQuantity);
    }

    private ProductStockUpdateRequestDto stockRequest(int quantity) {
        return new ProductStockUpdateRequestDto(quantity);
    }

    private Product product(UUID id, String name, UUID hubId, UUID companyId, int stockQuantity) {
        Product product = Product.builder()
                .productName(name)
                .hubId(hubId)
                .companyId(companyId)
                .stockQuantity(stockQuantity)
                .build();
        setId(product, id);
        return product;
    }

    private Company company(UUID hubId) {
        return Company.builder()
                .companyName("모니터 업체")
                .companyType(CompanyType.PRODUCER)
                .hubId(hubId)
                .address("서울시 중구")
                .build();
    }

    private void givenHubExists(UUID hubId) {
        HubResponse response = new HubResponse(
                hubId,
                "서울특별시 센터",
                "서울시 중구",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );
        given(hubClient.getHub(userId, userRole, hubId)).willReturn(ApiResponse.ok(response));
    }

    private void givenCompanyExists(UUID companyId) {
        given(companyRepository.findByIdAndDeletedAtIsNull(companyId))
                .willReturn(Optional.of(company(UUID.randomUUID())));
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/v1/hubs/" + UUID.randomUUID(),
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("error")
                .request(request)
                .headers(Map.of())
                .build();

        return FeignException.errorStatus("HubClient#getHub", response);
    }

    private void setId(Product product, UUID id) {
        try {
            Field field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(product, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
