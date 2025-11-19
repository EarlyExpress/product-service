package com.early_express.product_service.domain.product.presentation.web.dto.response;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String productId;
    private String sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private boolean isSellable;
    private boolean hasEvent;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().getAmount())
                .status(product.getStatus())
                .isSellable(product.isSellable())
                .hasEvent(product.isHasEvent())
                .minOrderQuantity(product.getMinOrderQuantity())
                .maxOrderQuantity(product.getMaxOrderQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}