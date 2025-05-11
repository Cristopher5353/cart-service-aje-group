package com.ajegroup.cart_service.controllers;

import com.ajegroup.cart_service.dtos.request.CartProductRequestDto;
import com.ajegroup.cart_service.dtos.request.CouponRequestDto;
import com.ajegroup.cart_service.dtos.response.CartSummaryDto;
import com.ajegroup.cart_service.dtos.response.TotalSummaryDto;
import com.ajegroup.cart_service.services.ICartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
    @InjectMocks
    private CartController cartController;

    @Mock
    private ICartService iCartService;

    private WebTestClient webTestClient;

    @Test
    void addCartProduct_ShouldReturnCreatedAndProductId() {
        CartProductRequestDto cartProductRequestDto = new CartProductRequestDto();
        when(iCartService.addCartProduct(cartProductRequestDto)).thenReturn(Mono.just(10L));

        webTestClient = WebTestClient.bindToController(cartController).build();
        webTestClient.post()
                .uri("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(cartProductRequestDto))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Long.class).isEqualTo(10L);

        verify(iCartService, times(1)).addCartProduct(cartProductRequestDto);
    }

    @Test
    void removeProduct_ShouldReturnNoContent() {
        Long cartProductId = 10L;
        when(iCartService.removeProduct(cartProductId)).thenReturn(Mono.empty());

        webTestClient = WebTestClient.bindToController(cartController).build();
        webTestClient.delete()
                .uri("/api/v1/cart/remove-product/{cartProductId}", cartProductId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(iCartService, times(1)).removeProduct(cartProductId);
    }

    @Test
    void applyCoupon_ShouldReturnOkAndTotalSummary() {
        CouponRequestDto couponRequestDto = new CouponRequestDto();
        TotalSummaryDto totalSummaryDto = TotalSummaryDto.builder().build();
        when(iCartService.applyCoupon(couponRequestDto)).thenReturn(Mono.just(totalSummaryDto));

        webTestClient = WebTestClient.bindToController(cartController).build();
        webTestClient.post()
                .uri("/api/v1/cart/add-coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(couponRequestDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TotalSummaryDto.class).isEqualTo(totalSummaryDto);

        verify(iCartService, times(1)).applyCoupon(couponRequestDto);
    }

    @Test
    void getCartSummary_ShouldReturnOkAndCartSummary() {
        Long cartId = 10L;
        CartSummaryDto cartSummaryDto = CartSummaryDto.builder().build();
        when(iCartService.getCartSummary(cartId)).thenReturn(Mono.just(cartSummaryDto));

        webTestClient = WebTestClient.bindToController(cartController).build();
        webTestClient.get()
                .uri("/api/v1/cart/summary/{cartId}", cartId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartSummaryDto.class).isEqualTo(cartSummaryDto);

        verify(iCartService, times(1)).getCartSummary(cartId);
    }
}