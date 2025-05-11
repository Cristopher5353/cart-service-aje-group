package com.ajegroup.cart_service.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductRequestDto {
    private Long cartId;
    private String productId;
    private int quantity;
}