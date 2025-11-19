package com.early_express.product_service.domain.product.presentation.internal.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 대량 상품 검증 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateProductsRequest {

    @NotEmpty(message = "상품 ID 목록은 비어있을 수 없습니다.")
    private List<String> productIds;
}