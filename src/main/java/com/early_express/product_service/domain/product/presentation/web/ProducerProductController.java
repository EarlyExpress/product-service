package com.early_express.product_service.domain.product.presentation.web;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.presentation.web.dto.request.CreateProductRequest;
import com.early_express.product_service.domain.product.presentation.web.dto.request.UpdateProductRequest;
import com.early_express.product_service.domain.product.presentation.web.dto.response.ProductResponse;
import com.early_express.product_service.global.presentation.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 생산업체용 상품 컨트롤러
 * - 상품 등록, 수정, 삭제, 활성화/중지
 */
@Slf4j
@RestController
@RequestMapping("/v1/product/web/producer")
@RequiredArgsConstructor
public class ProducerProductController {

    private final ProductService productService;

    /**
     * 상품 등록
     */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("X-User-Id") String sellerId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        log.info("상품 등록 요청: sellerId={}, hubId={}, companyId={}, name={}",
                sellerId, request.getHubId(), request.getCompanyId(), request.getName());

        CreateProductRequest.ProductCreateCommand command = request.toCommand(sellerId);

        Product product = productService.createProduct(
                command.getHubId(),
                command.getSellerId(),
                command.getCompanyId(),
                command.getName(),
                command.getDescription(),
                command.getPrice(),
                command.getMinOrderQuantity(),
                command.getMaxOrderQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(product));
    }

    /**
     * 상품 수정
     */
    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("상품 수정 요청: sellerId={}, productId={}", sellerId, productId);

        UpdateProductRequest.ProductUpdateCommand command = request.toCommand();

        Product product = productService.updateProduct(
                productId,
                command.getName(),
                command.getDescription(),
                command.getPrice()
        );

        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 상품 삭제 (단종)
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable String productId
    ) {
        log.info("상품 삭제 요청: sellerId={}, productId={}", sellerId, productId);

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 내 상품 목록 조회
     */
    @GetMapping("/products")
    public ResponseEntity<PageResponse<ProductResponse>> getMyProducts(
            @RequestHeader("X-User-Id") String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("내 상품 목록 조회: sellerId={}, page={}, size={}", sellerId, page, size);

        PageResponse<ProductResponse> response = productService.getProductsBySeller(sellerId, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 활성화
     */
    @PutMapping("/products/{productId}/activate")
    public ResponseEntity<ProductResponse> activateProduct(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable String productId
    ) {
        log.info("상품 활성화 요청: sellerId={}, productId={}", sellerId, productId);

        Product product = productService.activateProduct(productId);

        return ResponseEntity.ok(ProductResponse.from(product));
    }

    /**
     * 상품 일시중지
     */
    @PutMapping("/products/{productId}/suspend")
    public ResponseEntity<ProductResponse> suspendProduct(
            @RequestHeader("X-User-Id") String sellerId,
            @PathVariable String productId
    ) {
        log.info("상품 일시중지 요청: sellerId={}, productId={}", sellerId, productId);

        Product product = productService.suspendProduct(productId);

        return ResponseEntity.ok(ProductResponse.from(product));
    }
}