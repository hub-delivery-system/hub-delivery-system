package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.ProductCompanyNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductNotFoundException;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
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

    /** 상품 생성 로직 */
    @Transactional
    public ProductResponseDto createProduct(UUID userId, UserRole userRole, ProductCreateRequestDto request) {
        // TODO: userId/userRole 기반 scope 권한 검증
        // TODO: HubClient 공통화 후 hubId 존재 여부 검증
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
        // TODO: HubClient 공통화 후 hubId 존재 여부 검증
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        validateCompanyExists(request.companyId());
        product.update(request.productName(), request.hubId(), request.companyId());

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
}
