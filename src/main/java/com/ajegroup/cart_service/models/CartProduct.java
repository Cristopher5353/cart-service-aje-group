package com.ajegroup.cart_service.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_products")
@Data
@Builder
public class CartProduct {
    @Id
    private Long id;
    private String productId;
    private Long cartId;
    private int quantity;
    private double amount;
}