package com.early_express.product_service.domain.product.presentation.web.dto.request;

import com.early_express.product_service.domain.product.domain.model.vo.Price;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "최소 주문 수량은 필수입니다.")
    @Min(value = 1, message = "최소 주문 수량은 1 이상이어야 합니다.")
    private Integer minOrderQuantity;

    @NotNull(message = "최대 주문 수량은 필수입니다.")
    @Min(value = 1, message = "최대 주문 수량은 1 이상이어야 합니다.")
    private Integer maxOrderQuantity;

    /**
     * Request DTO → Domain Command Object
     */
    public ProductCreateCommand toCommand(String sellerId, String hubId, String companyId) {
        return ProductCreateCommand.builder()
                .sellerId(sellerId)
                .companyId(companyId)
                .hubId(hubId)
                .name(name)
                .description(description)
                .price(Price.of(price))
                .minOrderQuantity(minOrderQuantity)
                .maxOrderQuantity(maxOrderQuantity)
                .build();
    }

    @Getter
    @Builder
    public static class ProductCreateCommand {
        private final String hubId;
        private final String companyId;
        private final String sellerId;
        private final String name;
        private final String description;
        private final Price price;
        private final Integer minOrderQuantity;
        private final Integer maxOrderQuantity;
    }
}