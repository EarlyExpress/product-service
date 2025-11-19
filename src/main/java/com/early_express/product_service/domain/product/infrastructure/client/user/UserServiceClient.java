package com.early_express.product_service.domain.product.infrastructure.client.user;

import com.early_express.product_service.domain.product.infrastructure.client.user.dto.UserInfoResponse;
import com.early_express.product_service.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * User Service Feign Client
 * - 사용자 정보 조회
 */
@FeignClient(
        name = "user-service",
        url = "${user-service.url:http://localhost:8081}"
)
public interface UserServiceClient {

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 (hubId 포함)
     */
    @GetMapping("/api/v1/users/{userId}")
    UserInfoResponse getUserInfo(@PathVariable("userId") String userId);
}