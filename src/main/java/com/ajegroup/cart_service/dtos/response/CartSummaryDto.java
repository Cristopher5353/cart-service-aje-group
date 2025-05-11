package com.ajegroup.cart_service.dtos.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class CartSummaryDto {
    private double total;
    private double discount;
    private double totalAfterDiscount;
    private double totalInUsd;
    private List<ProductSummaryDto> products;
}