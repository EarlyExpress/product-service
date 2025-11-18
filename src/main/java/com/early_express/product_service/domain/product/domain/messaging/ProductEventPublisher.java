package com.early_express.product_service.domain.product.domain.messaging;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;

/**
 * Product 이벤트 발행 포트 (도메인 인터페이스)
 * - Infrastructure 계층에서 구현
 */
public interface ProductEventPublisher {

    /**
     * 상품 생성 이벤트 발행
     */
    void publishProductCreated(Product product);

    /**
     * 상품 상태 변경 이벤트 발행
     */
    void publishProductStatusChanged(String productId, ProductStatus oldStatus, ProductStatus newStatus);
}
