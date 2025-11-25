package com.early_express.product_service.domain.product.application.service;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import com.early_express.product_service.domain.product.domain.messaging.ProductEventPublisher;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductCreatedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductDeletedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductStatusChangedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductUpdatedEventData;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.domain.repository.ProductRepository;
import com.early_express.product_service.domain.product.presentation.internal.dto.response.ProductValidationResponse;
import com.early_express.product_service.domain.product.presentation.web.dto.response.ProductResponse;
import com.early_express.product_service.global.common.utils.PageUtils;
import com.early_express.product_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    // ==================== 명령(Command) 메서드 ====================

    /**
     * 상품 생성
     */
    @Transactional
    public Product createProduct(
            String hubId,
            String sellerId,
            String companyId,
            String name,
            String description,
            Price price,
            Integer minOrderQuantity,
            Integer maxOrderQuantity
    ) {
        log.info("상품 생성 시작: sellerId={}, name={}", sellerId, name);

        Product product = Product.create(
                null,
                sellerId,
                companyId,
                name,
                description,
                price,
                minOrderQuantity,
                maxOrderQuantity
        );

        Product savedProduct = productRepository.save(product);

        // 이벤트 발행 (EventData 사용)
        ProductCreatedEventData eventData = ProductCreatedEventData.of(
                savedProduct.getProductId(),
                savedProduct.getSellerId(),
                hubId,
                savedProduct.getName()
        );
        eventPublisher.publishProductCreated(eventData);

        log.info("상품 생성 완료: productId={}", savedProduct.getProductId());

        return savedProduct;
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

        // 이벤트 발행 (EventData 사용)
        ProductUpdatedEventData eventData = ProductUpdatedEventData.of(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getPrice().getAmount()
        );
        eventPublisher.publishProductUpdated(eventData);

        log.info("상품 수정 완료: productId={}", productId);

        return savedProduct;
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteProduct(String productId) {
        log.info("상품 삭제 시작: productId={}", productId);

        Product product = findById(productId);
        String sellerId = product.getSellerId();

        productRepository.delete(productId);

        // 이벤트 발행 (EventData 사용)
        ProductDeletedEventData eventData = ProductDeletedEventData.of(productId, sellerId);
        eventPublisher.publishProductDeleted(eventData);

        log.info("상품 삭제 완료: productId={}", productId);
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

        if (oldStatus != product.getStatus()) {
            publishStatusChangedEvent(productId, oldStatus, product.getStatus());
        }

        log.info("상품 활성화 완료: productId={}, status={}", productId, product.getStatus());

        return savedProduct;
    }

    /**
     * 상품 일시중지
     */
    @Transactional
    public Product suspendProduct(String productId) {
        log.info("상품 일시중지 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        product.suspend();
        Product savedProduct = productRepository.save(product);

        if (oldStatus != product.getStatus()) {
            publishStatusChangedEvent(productId, oldStatus, product.getStatus());
        }

        log.info("상품 일시중지 완료: productId={}", productId);

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

        publishStatusChangedEvent(productId, oldStatus, product.getStatus());

        log.info("상품 단종 완료: productId={}", productId);
    }

    /**
     * 품절 처리 (Inventory 이벤트 수신 시 호출)
     */
    @Transactional
    public void markAsOutOfStock(String productId) {
        log.info("품절 처리 시작: productId={}", productId);

        Product product = findById(productId);
        ProductStatus oldStatus = product.getStatus();

        if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            log.info("이미 품절 상태: productId={}", productId);
            return;
        }

        product.markOutOfStock();
        productRepository.save(product);

        publishStatusChangedEvent(productId, oldStatus, product.getStatus());

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

        if (product.getStatus() != ProductStatus.OUT_OF_STOCK) {
            log.info("품절 상태가 아님: productId={}, status={}", productId, product.getStatus());
            return;
        }

        product.activate();
        productRepository.save(product);

        publishStatusChangedEvent(productId, oldStatus, product.getStatus());

        log.info("품절 해제 완료: productId={}, status={}", productId, product.getStatus());
    }

    // ==================== 조회(Query) 메서드 - Controller용 ====================

    /**
     * 상품 단건 조회
     */
    public Product getProduct(String productId) {
        log.info("상품 조회: productId={}", productId);
        return findById(productId);
    }

    /**
     * 상품 목록 조회 (페이징)
     */
    public PageResponse<ProductResponse> getProductsWithPaging(int page, int size) {
        log.info("상품 목록 조회: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllWithPaging(pageable);

        return PageUtils.toPageResponse(productPage, ProductResponse::from);
    }

    /**
     * 상품 검색 (키워드)
     */
    public PageResponse<ProductResponse> searchProducts(String keyword, int page, int size) {
        log.info("상품 검색: keyword={}, page={}, size={}", keyword, page, size);

        List<Product> allProducts = productRepository.findByNameContaining(keyword);

        // 수동 페이징
        int start = page * size;
        int end = Math.min(start + size, allProducts.size());
        List<Product> pagedProducts = start < allProducts.size()
                ? allProducts.subList(start, end)
                : List.of();

        // PageInfo 생성
        int totalPages = (int) Math.ceil((double) allProducts.size() / size);
        com.early_express.product_service.global.common.dto.PageInfo pageInfo =
                com.early_express.product_service.global.common.dto.PageInfo.of(
                        page,
                        size,
                        allProducts.size(),
                        totalPages,
                        pagedProducts.size()
                );

        List<ProductResponse> productResponses = pagedProducts.stream()
                .map(ProductResponse::from)
                .toList();

        return PageResponse.of(productResponses, pageInfo);
    }

    /**
     * 판매자별 상품 목록 조회 (페이징)
     */
    public PageResponse<ProductResponse> getProductsBySeller(String sellerId, int page, int size) {
        log.info("판매자 상품 조회: sellerId={}, page={}, size={}", sellerId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findBySellerIdWithPaging(sellerId, pageable);

        return PageUtils.toPageResponse(productPage, ProductResponse::from);
    }

    /**
     * 상태별 상품 목록 조회
     */
    public List<Product> getProductsByStatus(ProductStatus status) {
        log.info("상태별 상품 조회: status={}", status);
        return productRepository.findByStatus(status);
    }

    /**
     * 전체 상품 목록 조회 (페이징 없음)
     */
    public List<Product> getAllProducts() {
        log.info("전체 상품 조회");
        return productRepository.findAll();
    }

    // ==================== 조회(Query) 메서드 - Internal API용 ====================

    /**
     * 판매자별 상품 목록 (전체) - Internal API용
     */
    public List<Product> getProductsBySellerId(String sellerId) {
        log.info("판매자 전체 상품 조회: sellerId={}", sellerId);
        return productRepository.findBySellerId(sellerId);
    }

    /**
     * 상품 존재 확인
     */
    public boolean existsProduct(String productId) {
        return productRepository.existsById(productId);
    }

    /**
     * 대량 상품 검증
     */
    public ProductValidationResponse validateProducts(List<String> productIds) {
        log.info("대량 상품 검증: count={}", productIds.size());

        List<String> validProductIds = new ArrayList<>();
        List<String> invalidProductIds = new ArrayList<>();
        Map<String, String> errors = new HashMap<>();

        for (String productId : productIds) {
            try {
                boolean exists = productRepository.existsById(productId);
                if (exists) {
                    validProductIds.add(productId);
                } else {
                    invalidProductIds.add(productId);
                    errors.put(productId, "상품을 찾을 수 없습니다.");
                }
            } catch (Exception e) {
                invalidProductIds.add(productId);
                errors.put(productId, e.getMessage());
            }
        }

        return ProductValidationResponse.of(
                validProductIds,
                invalidProductIds,
                errors
        );
    }

    // ==================== 내부 헬퍼 메서드 ====================

    /**
     * 상품 조회 (예외 발생)
     */
    private Product findById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 상태 변경 이벤트 발행 헬퍼
     */
    private void publishStatusChangedEvent(String productId, ProductStatus oldStatus, ProductStatus newStatus) {
        ProductStatusChangedEventData eventData = ProductStatusChangedEventData.of(
                productId,
                oldStatus.name(),
                newStatus.name()
        );
        eventPublisher.publishProductStatusChanged(eventData);
    }
}