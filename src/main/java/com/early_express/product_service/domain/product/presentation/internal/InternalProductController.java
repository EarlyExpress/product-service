package com.early_express.product_service.domain.product.presentation.internal;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.presentation.internal.dto.request.ValidateProductsRequest;
import com.early_express.product_service.domain.product.presentation.internal.dto.response.InternalProductResponse;
import com.early_express.product_service.domain.product.presentation.internal.dto.response.ProductValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 내부 API용 상품 컨트롤러
 * - 서비스 간 통신용
 *
 * TODO: API Gateway에서 내부 트래픽만 허용하도록 설정 필요
 */
@Slf4j
@RestController
@RequestMapping("/v1/product/internal")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    /**
     * 상품 존재 확인
     */
    @GetMapping("/products/{productId}/validate")
    public ResponseEntity<Map<String, Object>> validateProduct(
            @PathVariable String productId
    ) {
        log.info("상품 검증 요청: productId={}", productId);

        boolean exists = productService.existsProduct(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("exists", exists);
        response.put("valid", exists);

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 정보 조회 (내부용)
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<InternalProductResponse> getProduct(
            @PathVariable String productId
    ) {
        log.info("내부 상품 조회: productId={}", productId);

        Product product = productService.getProduct(productId);
        InternalProductResponse response = InternalProductResponse.from(product);

        return ResponseEntity.ok(response);
    }

    /**
     * 대량 상품 검증
     */
    @PostMapping("/products/validate-bulk")
    public ResponseEntity<ProductValidationResponse> validateProducts(
            @RequestBody ValidateProductsRequest request
    ) {
        log.info("대량 상품 검증 요청: count={}", request.getProductIds().size());

        ProductValidationResponse response = productService.validateProducts(request.getProductIds());

        return ResponseEntity.ok(response);
    }

    /**
     * 판매자별 상품 목록
     */
    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<List<InternalProductResponse>> getProductsBySeller(
            @PathVariable String sellerId
    ) {
        log.info("판매자 상품 조회: sellerId={}", sellerId);

        List<Product> products = productService.getProductsBySellerId(sellerId);

        List<InternalProductResponse> response = products.stream()
                .map(InternalProductResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}