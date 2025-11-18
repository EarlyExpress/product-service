package com.early_express.product_service.domain.product.application.service;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import com.early_express.product_service.domain.product.domain.messaging.ProductEventPublisher;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product Application Service
 * - 유스케이스 처리
 * - 트랜잭션 관리
 * - 이벤트 발행 (도메인 인터페이스 의존)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;

    /**
     * 상품 생성
     */
    @Transactional
    public Product createProduct(
            String sellerId,
            String name,
            String description,
            Price price,
            Integer minOrderQuantity,
            Integer maxOrderQuantity
    ) {
        log.info("상품 생성 시작: sellerId={}, name={}", sellerId, name);

        // 도메인 모델 생성
        Product product = Product.create(
                null, // ID는 Repository에서 자동 생성
                sellerId,
                name,
                description,
                price,
                minOrderQuantity,
                maxOrderQuantity
        );

        // 저장
        Product savedProduct = productRepository.save(product);

        // 이벤트 발행 (인터페이스 호출)
        eventPublisher.publishProductCreated(savedProduct);

        log.info("상품 생성 완료: productId={}", savedProduct.getProductId());

        return savedProduct;
    }

    /**
     * 상품 활성화
     */
    @Transactional
    public Product activateProduct(String productId) {
        log.info("상품 활성화 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        product.activate();
        Product savedProduct = productRepository.save(product);

        // 상태 변경 이벤트 발행
        if (oldStatus != product.getStatus()) {
            eventPublisher.publishProductStatusChanged(
                    productId,
                    oldStatus,
                    product.getStatus()
            );
        }

        log.info("상품 활성화 완료: productId={}, status={}", productId, product.getStatus());

        return savedProduct;
    }

    /**
     * 품절 처리 (Inventory 이벤트 수신 시 호출)
     */
    @Transactional
    public void markAsOutOfStock(String productId) {
        log.info("품절 처리 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        // 이미 품절 상태면 스킵
        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            log.info("이미 품절 상태: productId={}", productId);
            return;
        }

        product.markOutOfStock();
        productRepository.save(product);

        // 상태 변경 이벤트 발행
        eventPublisher.publishProductStatusChanged(
                productId,
                oldStatus,
                product.getStatus()
        );

        log.info("품절 처리 완료: productId={}", productId);
    }

    /**
     * 품절 해제 (Inventory 이벤트 수신 시 호출)
     */
    @Transactional
    public void restoreFromOutOfStock(String productId) {
        log.info("품절 해제 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        // 품절 상태가 아니면 스킵
        if (product.getStatus() != ProductStatus.OUT_OF_STOCK) {
            log.info("품절 상태가 아님: productId={}, status={}", productId, product.getStatus());
            return;
        }

        // ACTIVE 상태로 복구
        product.activate();
        productRepository.save(product);

        // 상태 변경 이벤트 발행
        eventPublisher.publishProductStatusChanged(
                productId,
                oldStatus,
                product.getStatus()
        );

        log.info("품절 해제 완료: productId={}, status={}", productId, product.getStatus());
    }

    /**
     * 상품 조회
     */
    public Product getProduct(String productId) {
        return findById(productId);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public Product updateProduct(
            String productId,
            String name,
            String description,
            Price price
    ) {
        log.info("상품 수정 시작: productId={}", productId);

        Product product = findById(productId);
        product.update(name, description, price);

        Product savedProduct = productRepository.save(product);

        log.info("상품 수정 완료: productId={}", productId);

        return savedProduct;
    }

    /**
     * 상품 단종
     */
    @Transactional
    public void discontinueProduct(String productId) {
        log.info("상품 단종 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        product.discontinue();
        productRepository.save(product);

        // 상태 변경 이벤트 발행
        eventPublisher.publishProductStatusChanged(
                productId,
                oldStatus,
                product.getStatus()
        );

        log.info("상품 단종 완료: productId={}", productId);
    }

    /**
     * 내부 헬퍼 메서드 - 상품 조회
     */
    private Product findById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}