package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.ProductCompanyNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductNotFoundException;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final String DEFAULT_SORT_PROPERTY = "createdAt";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "updatedAt", "productName");

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    /** 상품 생성 로직 */
    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto request) {
        // TODO: HubClient 확정 후 hubId 존재 여부와 HUB_MANAGER 담당 허브 여부를 검증한다.
        validateCompanyExists(request);

        Product product = productRepository.save(request.toEntity());
        return ProductResponseDto.from(product);
    }

    /** 상품 전체 조회 로직 */
    public PageResponse<ProductResponseDto> getAllProducts(String keyword, Integer page, Integer size, String sort) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Pageable pageable = createPageable(page, size, sort);

        return PageResponse.from(productRepository.searchProducts(normalizedKeyword, pageable)
                .map(ProductResponseDto::from));
    }

    /** 상품 상세 조회 로직 */
    public ProductResponseDto getProduct(UUID productId) {
        return ProductResponseDto.from(productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new));
    }

    private void validateCompanyExists(ProductCreateRequestDto request) {
        if (companyRepository.findByIdAndDeletedAtIsNull(request.companyId()).isEmpty()) {
            throw new ProductCompanyNotFoundException();
        }
    }

    private Pageable createPageable(Integer page, Integer size, String sort) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? PageableUtils.DEFAULT_SIZE : size;
        Pageable pageable = PageableUtils.createPageable(pageNumber, pageSize);

        if (!PageableUtils.hasKeyword(sort)) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolveSort(sort));
    }

    private Sort resolveSort(String sort) {
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            property = DEFAULT_SORT_PROPERTY;
        }

        Sort.Direction direction = parts.length < 2
                ? DEFAULT_SORT_DIRECTION
                : Sort.Direction.fromOptionalString(parts[1].trim()).orElse(DEFAULT_SORT_DIRECTION);

        return Sort.by(direction, property);
    }

    private String normalizeKeyword(String keyword) {
        if (!PageableUtils.hasKeyword(keyword)) {
            return null;
        }

        return keyword.trim();
    }
}
