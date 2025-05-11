package com.ajegroup.cart_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("carts")
@Data
@Builder
public class Cart {
    @Id
    private Long id;
    private String couponCode;
    private LocalDateTime createdAt;
}