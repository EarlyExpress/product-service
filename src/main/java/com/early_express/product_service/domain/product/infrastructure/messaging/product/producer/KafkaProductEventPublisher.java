package com.early_express.product_service.domain.product.infrastructure.messaging.product.producer;

import com.early_express.product_service.domain.product.domain.messaging.ProductEventPublisher;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductCreatedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductDeletedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductStatusChangedEventData;
import com.early_express.product_service.domain.product.domain.messaging.dto.ProductUpdatedEventData;
import com.early_express.product_service.domain.product.infrastructure.messaging.product.event.ProductCreatedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.product.event.ProductDeletedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.product.event.ProductStatusChangedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.product.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Product 이벤트 발행자 구현체 (Kafka Adapter)
 * 도메인 EventData → Kafka Event 변환 후 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProductEventPublisher implements ProductEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.product-created:product-created}")
    private String productCreatedTopic;

    @Value("${spring.kafka.topic.product-updated:product-updated}")
    private String productUpdatedTopic;

    @Value("${spring.kafka.topic.product-deleted:product-deleted}")
    private String productDeletedTopic;

    @Value("${spring.kafka.topic.product-status-changed:product-status-changed}")
    private String productStatusChangedTopic;

    /**
     * 상품 생성 이벤트 발행
     */
    @Override
    public void publishProductCreated(ProductCreatedEventData eventData) {
        log.info("ProductCreated 이벤트 발행 준비 - productId: {}, hubId: {}",
                eventData.getProductId(), eventData.getHubId());

        // EventData → Event 변환
        ProductCreatedEvent event = ProductCreatedEvent.from(eventData);

        // Kafka 발행
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(productCreatedTopic, eventData.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductCreated 이벤트 발행 성공 - productId: {}, eventId: {}, topic: {}, partition: {}, offset: {}",
                        eventData.getProductId(),
                        event.getEventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductCreated 이벤트 발행 실패 - productId: {}, error: {}",
                        eventData.getProductId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 수정 이벤트 발행
     */
    @Override
    public void publishProductUpdated(ProductUpdatedEventData eventData) {
        log.info("ProductUpdated 이벤트 발행 준비 - productId: {}", eventData.getProductId());

        // EventData → Event 변환
        ProductUpdatedEvent event = ProductUpdatedEvent.from(eventData);

        // Kafka 발행
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(productUpdatedTopic, eventData.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductUpdated 이벤트 발행 성공 - productId: {}, eventId: {}, partition: {}, offset: {}",
                        eventData.getProductId(),
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductUpdated 이벤트 발행 실패 - productId: {}, error: {}",
                        eventData.getProductId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 삭제(단종) 이벤트 발행
     */
    @Override
    public void publishProductDeleted(ProductDeletedEventData eventData) {
        log.info("ProductDeleted 이벤트 발행 준비 - productId: {}, sellerId: {}",
                eventData.getProductId(), eventData.getSellerId());

        // EventData → Event 변환
        ProductDeletedEvent event = ProductDeletedEvent.from(eventData);

        // Kafka 발행
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(productDeletedTopic, eventData.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductDeleted 이벤트 발행 성공 - productId: {}, eventId: {}, partition: {}, offset: {}",
                        eventData.getProductId(),
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductDeleted 이벤트 발행 실패 - productId: {}, error: {}",
                        eventData.getProductId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 상태 변경 이벤트 발행
     */
    @Override
    public void publishProductStatusChanged(ProductStatusChangedEventData eventData) {
        log.info("ProductStatusChanged 이벤트 발행 준비 - productId: {}, {} → {}",
                eventData.getProductId(), eventData.getOldStatus(), eventData.getNewStatus());

        // EventData → Event 변환
        ProductStatusChangedEvent event = ProductStatusChangedEvent.from(eventData);

        // Kafka 발행
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(productStatusChangedTopic, eventData.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductStatusChanged 이벤트 발행 성공 - productId: {}, eventId: {}, partition: {}, offset: {}",
                        eventData.getProductId(),
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductStatusChanged 이벤트 발행 실패 - productId: {}, error: {}",
                        eventData.getProductId(), ex.getMessage(), ex);
            }
        });
    }
}