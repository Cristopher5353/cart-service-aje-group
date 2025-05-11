package com.ajegroup.cart_service.controllers;

import com.ajegroup.cart_service.dtos.request.CartProductRequestDto;
import com.ajegroup.cart_service.dtos.request.CouponRequestDto;
import com.ajegroup.cart_service.dtos.response.CartSummaryDto;
import com.ajegroup.cart_service.dtos.response.TotalSummaryDto;
import com.ajegroup.cart_service.services.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final ICartService iCartService;

    @PostMapping("/add-product")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Long> addCartProduct(@RequestBody CartProductRequestDto cartProductRequestDto) {
        return iCartService.addCartProduct(cartProductRequestDto);
    }

    @DeleteMapping("/remove-product/{cartProductId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeProduct(@PathVariable Long cartProductId) {
        return iCartService.removeProduct(cartProductId);
    }

    @PostMapping("/add-coupon")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TotalSummaryDto> applyCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        return iCartService.applyCoupon(couponRequestDto);
    }

    @GetMapping("/summary/{cartId}")
    public Mono<CartSummaryDto> getCartSummary(@PathVariable Long cartId) {
        return iCartService.getCartSummary(cartId);
    }
}