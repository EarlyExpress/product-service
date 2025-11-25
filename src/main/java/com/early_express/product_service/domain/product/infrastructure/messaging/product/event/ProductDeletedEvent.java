package com.early_express.product_service.domain.product.infrastructure.messaging.product.event;

import com.early_express.product_service.domain.product.domain.messaging.dto.ProductDeletedEventData;
import com.early_express.product_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 상품 삭제(단종) 이벤트 (Kafka 메시지)
 * Product Service → Inventory Service
 * Topic: product-deleted
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class ProductDeletedEvent extends BaseEvent {

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 판매자 ID
     */
    private String sellerId;

    /**
     * 삭제 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static ProductDeletedEvent from(ProductDeletedEventData data) {
        ProductDeletedEvent event = ProductDeletedEvent.builder()
                .productId(data.getProductId())
                .sellerId(data.getSellerId())
                .deletedAt(data.getDeletedAt())
                .build();

        event.initBaseEvent("PRODUCT_DELETED", "product-service");

        return event;
    }
}