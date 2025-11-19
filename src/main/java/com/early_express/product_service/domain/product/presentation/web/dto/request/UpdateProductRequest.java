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
 * 상품 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotBlank(message = "상품 설명은 필수입니다.")
    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    /**
     * Request DTO → Domain Command Object
     */
    public ProductUpdateCommand toCommand() {
        return ProductUpdateCommand.builder()
                .name(name)
                .description(description)
                .price(Price.of(price))
                .build();
    }

    @Getter
    @Builder
    public static class ProductUpdateCommand {
        private final String name;
        private final String description;
        private final Price price;
    }
}