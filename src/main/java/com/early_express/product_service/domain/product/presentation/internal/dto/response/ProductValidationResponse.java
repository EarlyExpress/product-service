package com.early_express.product_service.domain.product.presentation.internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 상품 검증 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductValidationResponse {

    private boolean allValid;
    private List<String> validProductIds;
    private List<String> invalidProductIds;
    private Map<String, String> errors;  // productId -> error message

    public static ProductValidationResponse of(
            List<String> validProductIds,
            List<String> invalidProductIds,
            Map<String, String> errors
    ) {
        return ProductValidationResponse.builder()
                .allValid(invalidProductIds.isEmpty())
                .validProductIds(validProductIds)
                .invalidProductIds(invalidProductIds)
                .errors(errors)
                .build();
    }
}