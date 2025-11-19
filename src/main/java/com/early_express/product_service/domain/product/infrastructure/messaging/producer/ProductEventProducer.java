package com.early_express.product_service.domain.product.infrastructure.messaging.producer;

import com.early_express.product_service.domain.product.domain.messaging.ProductEventPublisher;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductCreatedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductDeletedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductStatusChangedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Product 이벤트 발행자 구현체 (어댑터)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer implements ProductEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.application.name:product-service}")
    private String applicationName;

    private static final String EVENTS_TOPIC_SUFFIX = "-events";

    /**
     * 상품 생성 이벤트 발행
     */
    @Override
    public void publishProductCreated(Product product, String hubId) {
        String topic = applicationName + EVENTS_TOPIC_SUFFIX;

        ProductCreatedEvent event = ProductCreatedEvent.of(
                product.getProductId(),
                product.getSellerId(),
                hubId,
                product.getName()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, product.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductCreatedEvent 발행 성공: productId={}, hubId={}, topic={}, partition={}, offset={}",
                        product.getProductId(),
                        hubId,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductCreatedEvent 발행 실패: productId={}, error={}",
                        product.getProductId(), ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 상태 변경 이벤트 발행
     */
    @Override
    public void publishProductStatusChanged(String productId, ProductStatus oldStatus, ProductStatus newStatus) {
        String topic = applicationName + EVENTS_TOPIC_SUFFIX;

        ProductStatusChangedEvent event = ProductStatusChangedEvent.of(
                productId,
                oldStatus,
                newStatus
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, productId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductStatusChangedEvent 발행 성공: productId={}, oldStatus={}, newStatus={}, partition={}, offset={}",
                        productId,
                        oldStatus,
                        newStatus,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductStatusChangedEvent 발행 실패: productId={}, error={}",
                        productId, ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 삭제(단종) 이벤트 발행
     */
    @Override
    public void publishProductDeleted(String productId, String sellerId) {
        String topic = applicationName + EVENTS_TOPIC_SUFFIX;

        ProductDeletedEvent event = ProductDeletedEvent.of(productId, sellerId);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, productId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductDeletedEvent 발행 성공: productId={}, sellerId={}, partition={}, offset={}",
                        productId,
                        sellerId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductDeletedEvent 발행 실패: productId={}, error={}",
                        productId, ex.getMessage(), ex);
            }
        });
    }

    /**
     * 상품 정보 수정 이벤트 발행
     */
    @Override
    public void publishProductUpdated(Product product) {
        String topic = applicationName + EVENTS_TOPIC_SUFFIX;

        ProductUpdatedEvent event = ProductUpdatedEvent.of(
                product.getProductId(),
                product.getName(),
                product.getPrice().getAmount()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, product.getProductId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("ProductUpdatedEvent 발행 성공: productId={}, name={}, partition={}, offset={}",
                        product.getProductId(),
                        product.getName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("ProductUpdatedEvent 발행 실패: productId={}, error={}",
                        product.getProductId(), ex.getMessage(), ex);
            }
        });
    }
}