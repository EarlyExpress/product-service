package com.early_express.product_service.domain.product.presentation.web;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.presentation.web.dto.response.ProductResponse;
import com.early_express.product_service.global.presentation.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 모든 사용자용 상품 컨트롤러
 * - 상품 조회 (목록, 상세, 검색)
 */
@Slf4j
@RestController
@RequestMapping("/v1/product/web/all")
@RequiredArgsConstructor
public class AllProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징)
     */
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("상품 목록 조회 요청: page={}, size={}", page, size);

        PageResponse<ProductResponse> response = productService.getProductsWithPaging(page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable String productId
    ) {
        log.info("상품 상세 조회 요청: productId={}", productId);

        Product product = productService.getProduct(productId);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 검색 (키워드)
     */
    @GetMapping("/products/search")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("상품 검색 요청: keyword={}, page={}, size={}", keyword, page, size);

        PageResponse<ProductResponse> response = productService.searchProducts(keyword, page, size);

        return ResponseEntity.ok(response);
    }
}