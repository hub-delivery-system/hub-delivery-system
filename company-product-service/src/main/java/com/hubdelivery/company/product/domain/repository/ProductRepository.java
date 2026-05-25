package com.hubdelivery.company.product.domain.repository;

import com.hubdelivery.company.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
