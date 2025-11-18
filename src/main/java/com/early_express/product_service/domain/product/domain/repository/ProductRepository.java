package com.early_express.product_service.domain.product.domain.repository;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository 인터페이스 (포트)
 * - 도메인 계층에서 정의
 * - Infrastructure 계층에서 구현
 */
public interface ProductRepository {

    /**
     * 상품 저장
     * - ID가 있으면 업데이트 (더티 체킹)
     * - ID가 없으면 신규 저장
     */
    Product save(Product product);

    /**
     * ID로 상품 조회 (삭제된 상품 제외)
     */
    Optional<Product> findById(String productId);

    /**
     * 전체 상품 조회 (삭제된 상품 제외)
     */
    List<Product> findAll();

    /**
     * 판매자별 상품 조회 (삭제된 상품 제외)
     */
    List<Product> findBySellerId(String sellerId);

    /**
     * 상태별 상품 조회 (삭제된 상품 제외)
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * 상품명으로 검색 (삭제된 상품 제외)
     */
    List<Product> findByNameContaining(String keyword);

    /**
     * 페이징 조회 (삭제된 상품 제외)
     */
    Page<Product> findAllWithPaging(Pageable pageable);

    /**
     * 판매자별 페이징 조회 (삭제된 상품 제외)
     */
    Page<Product> findBySellerIdWithPaging(String sellerId, Pageable pageable);

    /**
     * 소프트 삭제
     */
    void delete(String productId);

    /**
     * 상품 존재 여부 확인 (삭제된 상품 제외)
     */
    boolean existsById(String productId);
}
