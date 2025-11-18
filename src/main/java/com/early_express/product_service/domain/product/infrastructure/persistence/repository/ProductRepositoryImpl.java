package com.early_express.product_service.domain.product.infrastructure.persistence.repository;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.domain.repository.ProductRepository;
import com.early_express.product_service.domain.product.infrastructure.persistence.entity.ProductEntity;
import com.early_express.product_service.domain.product.infrastructure.persistence.entity.QProductEntity;
import com.early_express.product_service.domain.product.infrastructure.persistence.jpa.ProductJpaRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Product Repository 구현체 (어댑터)
 * - 도메인 Repository 인터페이스 구현
 * - JPA Repository 사용
 * - QueryDSL 활용
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    private static final QProductEntity product = QProductEntity.productEntity;

    /**
     * 상품 저장
     * - ID가 있으면 업데이트 (더티 체킹)
     * - ID가 없으면 신규 저장
     */
    @Override
    @Transactional
    public Product save(Product domain) {
        String productId = domain.getProductId();

        // ID가 있으면 업데이트 (더티 체킹)
        if (productId != null && !productId.isBlank()) {
            Optional<ProductEntity> existingEntity = jpaRepository.findById(productId);
            if (existingEntity.isPresent()) {
                ProductEntity entity = existingEntity.get();
                entity.updateFromDomain(domain);
                return entity.toDomain();
            }
        }

        // ID가 없거나 존재하지 않으면 신규 저장
        ProductEntity entity = ProductEntity.fromDomain(domain);
        ProductEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    /**
     * ID로 상품 조회 (삭제된 상품 제외)
     */
    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(
                        queryFactory
                                .selectFrom(product)
                                .where(
                                        product.productId.eq(productId),
                                        product.isDeleted.eq(false)
                                )
                                .fetchOne()
                )
                .map(ProductEntity::toDomain);
    }

    /**
     * 전체 상품 조회 (삭제된 상품 제외)
     */
    @Override
    public List<Product> findAll() {
        return queryFactory
                .selectFrom(product)
                .where(product.isDeleted.eq(false))
                .fetch()
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 판매자별 상품 조회 (삭제된 상품 제외)
     */
    @Override
    public List<Product> findBySellerId(String sellerId) {
        return queryFactory
                .selectFrom(product)
                .where(
                        product.sellerId.eq(sellerId),
                        product.isDeleted.eq(false)
                )
                .fetch()
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 상태별 상품 조회 (삭제된 상품 제외)
     */
    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return queryFactory
                .selectFrom(product)
                .where(
                        product.status.eq(status),
                        product.isDeleted.eq(false)
                )
                .fetch()
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 상품명으로 검색 (삭제된 상품 제외)
     */
    @Override
    public List<Product> findByNameContaining(String keyword) {
        return queryFactory
                .selectFrom(product)
                .where(
                        product.name.containsIgnoreCase(keyword),
                        product.isDeleted.eq(false)
                )
                .fetch()
                .stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 페이징 조회 (삭제된 상품 제외)
     */
    @Override
    public Page<Product> findAllWithPaging(Pageable pageable) {
        List<ProductEntity> entities = queryFactory
                .selectFrom(product)
                .where(product.isDeleted.eq(false))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(product)
                .where(product.isDeleted.eq(false))
                .fetchCount();

        List<Product> products = entities.stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(products, pageable, total);
    }

    /**
     * 판매자별 페이징 조회 (삭제된 상품 제외)
     */
    @Override
    public Page<Product> findBySellerIdWithPaging(String sellerId, Pageable pageable) {
        List<ProductEntity> entities = queryFactory
                .selectFrom(product)
                .where(
                        product.sellerId.eq(sellerId),
                        product.isDeleted.eq(false)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(product)
                .where(
                        product.sellerId.eq(sellerId),
                        product.isDeleted.eq(false)
                )
                .fetchCount();

        List<Product> products = entities.stream()
                .map(ProductEntity::toDomain)
                .collect(Collectors.toList());

        return new PageImpl<>(products, pageable, total);
    }

    /**
     * 소프트 삭제
     */
    @Override
    @Transactional
    public void delete(String productId) {
        ProductEntity entity = jpaRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        entity.delete(null); // deletedBy는 Service에서 처리 가능
    }

    /**
     * 상품 존재 여부 확인 (삭제된 상품 제외)
     */
    @Override
    public boolean existsById(String productId) {
        return queryFactory
                .selectFrom(product)
                .where(
                        product.productId.eq(productId),
                        product.isDeleted.eq(false)
                )
                .fetchFirst() != null;
    }
}
