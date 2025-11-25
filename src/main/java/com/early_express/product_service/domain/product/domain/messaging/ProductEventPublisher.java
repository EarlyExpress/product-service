package com.early_express.product_service.domain.product.domain.messaging;

import com.early_express.product_service.domain.product.domain.messaging.dto.ProductCreatedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductDeletedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductStatusChangedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductUpdatedEventData;

/**
 * Product 이벤트 발행 포트 (도메인 인터페이스)
 * Infrastructure 계층에서 구현
 */
public interface ProductEventPublisher {

    /**
     * 상품 생성 이벤트 발행
     * Product Service → Inventory Service
     *
     * @param eventData 상품 생성 이벤트 데이터
     */
    void publishProductCreated(ProductCreatedEventData eventData);

    /**
     * 상품 수정 이벤트 발행
     *
     * @param eventData 상품 수정 이벤트 데이터
     */
    void publishProductUpdated(ProductUpdatedEventData eventData);

    /**
     * 상품 삭제(단종) 이벤트 발행
     * Product Service → Inventory Service
     *
     * @param eventData 상품 삭제 이벤트 데이터
     */
    void publishProductDeleted(ProductDeletedEventData eventData);

    /**
     * 상품 상태 변경 이벤트 발행
     *
     * @param eventData 상품 상태 변경 이벤트 데이터
     */
    void publishProductStatusChanged(ProductStatusChangedEventData eventData);
}