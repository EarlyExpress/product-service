package com.early_express.product_service.domain.product.infrastructure.persistence.jpa;

import com.early_express.product_service.domain.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Product JPA Repository
 * - Spring Data JPA 기본 인터페이스
 * - QueryDSL은 구현체에서 처리
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
}
