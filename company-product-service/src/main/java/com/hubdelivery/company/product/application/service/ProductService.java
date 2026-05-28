package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.global.infrastructure.client.HubClient;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.ProductCompanyNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductHubIntegrationException;
import com.hubdelivery.company.product.domain.exception.ProductHubNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductNotFoundException;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.hubdelivery.company.global.util.SearchSortPolicy.PRODUCT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final HubClient hubClient;

    /** 상품 생성 로직 */
    @Transactional
    public ProductResponseDto createProduct(UUID userId, UserRole userRole, ProductCreateRequestDto request) {
        // TODO: userId/userRole 기반 scope 권한 검증
        validateHubExists(userId, userRole, request.hubId());
        validateCompanyExists(request.companyId());

        Product product = productRepository.save(request.toEntity());
        return ProductResponseDto.from(product);
    }

    /** 상품 전체 조회 로직 */
    public PageResponse<ProductResponseDto> getAllProducts(String keyword, Integer page, Integer size, String sort) {
        String normalizedKeyword = SearchPageableUtils.normalizeKeyword(keyword);
        Pageable pageable = SearchPageableUtils.createPageable(page, size, sort, PRODUCT);

        return PageResponse.from(productRepository.searchProducts(normalizedKeyword, pageable)
                .map(ProductResponseDto::from));
    }

    /** 상품 상세 조회 로직 */
    public ProductResponseDto getProduct(UUID productId) {
        return ProductResponseDto.from(productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new));
    }

    /** 상품 수정 로직 */
    @Transactional
    public ProductResponseDto updateProduct(UUID userId, UserRole userRole, UUID productId, ProductUpdateRequestDto request) {
        // TODO: userId/userRole 기반 scope 권한 검증
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        validateHubExists(userId, userRole, request.hubId());
        validateCompanyExists(request.companyId());

        product.update(request.productName(), request.hubId(), request.companyId(), request.stockQuantity());

        return ProductResponseDto.from(product);
    }

    /** 상품 삭제 로직 */
    @Transactional
    public void deleteProduct(UUID userId, UserRole userRole, UUID productId) {
        // TODO: userId/userRole 기반 scope 권한 검증
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        // TODO: user-service 연동 후 userId 대신 username 으로 기록
        product.softDelete(userId.toString());
    }

    private void validateCompanyExists(UUID companyId) {
        if (companyRepository.findByIdAndDeletedAtIsNull(companyId).isEmpty()) {
            throw new ProductCompanyNotFoundException();
        }
    }

    private void validateHubExists(UUID userId, UserRole userRole, UUID hubId) {
        try {
            hubClient.getHub(userId, userRole, hubId);
        } catch (FeignException.NotFound e) {
            throw new ProductHubNotFoundException();
        } catch (FeignException e) {
            throw new ProductHubIntegrationException();
        }
    }
}
