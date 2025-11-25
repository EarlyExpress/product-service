package com.early_express.product_service.domain.product.infrastructure.messaging.product.event;

import com.early_express.product_service.domain.product.domain.messaging.dto.ProductCreatedEventData;
import com.early_express.product_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 상품 생성 이벤트 (Kafka 메시지)
 * Product Service → Inventory Service
 * Topic: product-created
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class ProductCreatedEvent extends BaseEvent {

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 판매자 ID
     */
    private String sellerId;

    /**
     * 허브 ID
     */
    private String hubId;

    /**
     * 상품명
     */
    private String name;

    /**
     * 생성 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * EventData로부터 Event 생성
     */
    public static ProductCreatedEvent from(ProductCreatedEventData data) {
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(data.getProductId())
                .sellerId(data.getSellerId())
                .hubId(data.getHubId())
                .name(data.getName())
                .createdAt(data.getCreatedAt())
                .build();

        event.initBaseEvent("PRODUCT_CREATED", "product-service");

        return event;
    }
}