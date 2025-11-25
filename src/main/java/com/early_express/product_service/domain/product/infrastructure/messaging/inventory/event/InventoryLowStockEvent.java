package com.early_express.product_service.domain.product.infrastructure.messaging.inventory.event;

import com.early_express.product_service.global.infrastructure.event.base.BaseEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 재고 부족 이벤트 (수신용)
 * Inventory Service → Product Service
 * Topic: inventory-low-stock
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryLowStockEvent extends BaseEvent {

    /**
     * 재고 ID
     */
    private String inventoryId;

    /**
     * 상품 ID
     */
    private String productId;

    /**
     * 허브 ID
     */
    private String hubId;

    /**
     * 현재 수량
     */
    private Integer currentQuantity;

    /**
     * 안전 재고
     */
    private Integer safetyStock;

    /**
     * 감지 시간
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime detectedAt;
}