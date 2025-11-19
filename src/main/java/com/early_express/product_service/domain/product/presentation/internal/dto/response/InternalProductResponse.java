package com.early_express.product_service.domain.product.presentation.internal.dto.response;

import com.early_express.product_service.domain.product.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 내부 API용 상품 응답 DTO (간소화)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalProductResponse {

    private String productId;
    private String sellerId;
    private String name;
    private BigDecimal price;
    private boolean isSellable;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;

    public static InternalProductResponse from(Product product) {
        return InternalProductResponse.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .name(product.getName())
                .price(product.getPrice().getAmount())
                .isSellable(product.isSellable())
                .minOrderQuantity(product.getMinOrderQuantity())
                .maxOrderQuantity(product.getMaxOrderQuantity())
                .build();
    }
}