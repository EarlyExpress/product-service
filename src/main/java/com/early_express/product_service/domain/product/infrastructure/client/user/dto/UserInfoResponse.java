package com.early_express.product_service.domain.product.infrastructure.client.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User 서비스 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String username;
    private String hubId;
    private String companyId;
    private String role;
}
