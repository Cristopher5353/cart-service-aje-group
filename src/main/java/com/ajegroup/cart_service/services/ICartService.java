package com.ajegroup.cart_service.services;

import com.ajegroup.cart_service.dtos.request.CartProductRequestDto;
import com.ajegroup.cart_service.dtos.request.CouponRequestDto;
import com.ajegroup.cart_service.dtos.response.CartSummaryDto;
import com.ajegroup.cart_service.dtos.response.TotalSummaryDto;
import reactor.core.publisher.Mono;

public interface ICartService {
    Mono<Long> addCartProduct(CartProductRequestDto cartProductRequestDto);
    Mono<Void> removeProduct(Long cartProductId);
    Mono<TotalSummaryDto> applyCoupon(CouponRequestDto couponRequestDto);
    Mono<CartSummaryDto> getCartSummary(Long cartId);
}
