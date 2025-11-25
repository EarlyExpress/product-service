package com.early_express.product_service.domain.product.infrastructure.messaging.product.event;

import com.early_express.product_service.domain.product.domain.messaging.dto.ProductStatusChangedEventData;
import com.early_express.product_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 상품 상태 변경 이벤트 (Kafka 메시지)
 * Topic: product-status-changed
 */
@Getter
@SuperBuilder
@NoArgsConstructor
public class ProductStatusChangedEvent extends BaseEvent {

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 이전 상태
     */
    private String oldStatus;

    /**
     * 새 상태
     */
    private String newStatus;

    /**
     * 변경 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;

    /**
     * EventData로부터 Event 생성
     */
    public static ProductStatusChangedEvent from(ProductStatusChangedEventData data) {
        ProductStatusChangedEvent event = ProductStatusChangedEvent.builder()
                .productId(data.getProductId())
                .oldStatus(data.getOldStatus())
                .newStatus(data.getNewStatus())
                .changedAt(data.getChangedAt())
                .build();

        event.initBaseEvent("PRODUCT_STATUS_CHANGED", "product-service");

        return event;
    }
}