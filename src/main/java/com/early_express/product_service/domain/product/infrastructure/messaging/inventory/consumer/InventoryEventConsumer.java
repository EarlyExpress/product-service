package com.early_express.product_service.domain.product.infrastructure.messaging.inventory.consumer;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.infrastructure.messaging.inventory.event.InventoryLowStockEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.inventory.event.InventoryRestockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Inventory 이벤트 Consumer
 * Inventory Service → Product Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final ProductService productService;

    /**
     * 재고 부족 이벤트 처리
     * 상품을 품절 상태로 변경
     * Topic: inventory-low-stock
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.inventory-low-stock:inventory-low-stock}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryLowStock(
            @Payload InventoryLowStockEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[Inventory] LowStock 이벤트 수신 - productId: {}, hubId: {}, currentQuantity: {}, partition: {}, offset: {}",
                event.getProductId(),
                event.getHubId(),
                event.getCurrentQuantity(),
                partition,
                offset);

        try {
            // 품절 처리
            productService.markAsOutOfStock(event.getProductId());

            // 수동 커밋
            ack.acknowledge();

            log.info("[Inventory] LowStock 이벤트 처리 완료 - productId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[Inventory] LowStock 이벤트 처리 실패 - productId: {}, error: {}",
                    event.getProductId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 재입고 이벤트 처리
     * 품절 상태 해제
     * Topic: inventory-restocked
     */
    @KafkaListener(
            topics = "${spring.kafka.topic.inventory-restocked:inventory-restocked}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryRestocked(
            @Payload InventoryRestockedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("[Inventory] Restocked 이벤트 수신 - productId: {}, hubId: {}, restockedQuantity: {}, partition: {}, offset: {}",
                event.getProductId(),
                event.getHubId(),
                event.getRestockedQuantity(),
                partition,
                offset);

        try {
            // 품절 해제
            productService.restoreFromOutOfStock(event.getProductId());

            // 수동 커밋
            ack.acknowledge();

            log.info("[Inventory] Restocked 이벤트 처리 완료 - productId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[Inventory] Restocked 이벤트 처리 실패 - productId: {}, error: {}",
                    event.getProductId(), e.getMessage(), e);
            throw e;
        }
    }
}