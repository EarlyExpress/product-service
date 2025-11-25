package com.early_express.product_service.domain.product.infrastructure.messaging.product.event;

import com.early_express.product_service.domain.product.domain.messaging.dto.ProductUpdatedEventData;
import com.early_express.product_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 수정 이벤트 (Kafka 메시지)
 * Topic: product-updated
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class ProductUpdatedEvent extends BaseEvent {

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 상품명
     */
    private String name;

    /**
     * 가격
     */
    private BigDecimal price;

    /**
     * 수정 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static ProductUpdatedEvent from(ProductUpdatedEventData data) {
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId(data.getProductId())
                .name(data.getName())
                .price(data.getPrice())
                .updatedAt(data.getUpdatedAt())
                .build();

        event.initBaseEvent("PRODUCT_UPDATED", "product-service");

        return event;
    }
}