package com.ajegroup.cart_service.dtos.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class TotalSummaryDto {
    private double total;
    private double discount;
    private double totalAfterDiscount;
}
