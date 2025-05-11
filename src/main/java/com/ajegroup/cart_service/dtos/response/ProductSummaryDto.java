package com.ajegroup.cart_service.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSummaryDto {
    private Long itemId;
    private String id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private double amount;
}
