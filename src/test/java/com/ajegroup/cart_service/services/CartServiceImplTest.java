package com.ajegroup.cart_service.services;

import com.ajegroup.cart_service.dtos.request.CartProductRequestDto;
import com.ajegroup.cart_service.dtos.request.CouponRequestDto;
import com.ajegroup.cart_service.models.Cart;
import com.ajegroup.cart_service.models.CartProduct;
import com.ajegroup.cart_service.models.Coupon;
import com.ajegroup.cart_service.models.Product;
import com.ajegroup.cart_service.repositories.CartProductRepository;
import com.ajegroup.cart_service.repositories.CartRepository;
import com.ajegroup.cart_service.repositories.CouponRepository;
import com.ajegroup.cart_service.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartProductRepository cartProductRepository;
    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addCartProduct_shouldAddNewProductToCart() {
        Product product = new Product("P002", "Amayu", 1.5, "juice");
        Cart cart = Cart.builder().id(1L).build();
        CartProductRequestDto request = new CartProductRequestDto(1L, "P002", 1);

        when(cartRepository.findById(anyLong())).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(cart));
        when(productRepository.findById("P002")).thenReturn(Mono.just(product));
        when(cartProductRepository.findByCartIdAndProductId(1L, "P002")).thenReturn(Mono.empty());
        when(cartProductRepository.save(any())).thenReturn(Mono.just(CartProduct.builder().build()));

        StepVerifier.create(cartService.addCartProduct(request))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void addCartProduct_shouldUpdateExistingCartProduct() {
        Product product = new Product("P002", "Amayu", 1.5, "juice");
        Cart cart = Cart.builder().id(1L).build();
        CartProduct existing = CartProduct.builder().cartId(1L).productId("P002").quantity(2).amount(6.0).build();
        CartProductRequestDto request = new CartProductRequestDto(1L, "P002", 2);

        when(cartRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any())).thenReturn(Mono.just(cart));
        when(productRepository.findById("P002")).thenReturn(Mono.just(product));
        when(cartProductRepository.findByCartIdAndProductId(1L, "P002")).thenReturn(Mono.just(existing));
        when(cartProductRepository.save(any())).thenReturn(Mono.just(existing));

        StepVerifier.create(cartService.addCartProduct(request))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void removeProduct_shouldDeleteProduct() {
        CartProduct cartProduct = CartProduct.builder().id(1L).build();

        when(cartProductRepository.findById(1L)).thenReturn(Mono.just(cartProduct));
        when(cartProductRepository.delete(cartProduct)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProduct(1L))
                .verifyComplete();
    }

    @Test
    void applyCoupon_shouldReturnSummaryWithDiscount() {
        Coupon coupon = new Coupon("C002", 5, LocalDateTime.now().plusDays(1),"juice");
        Cart cart = Cart.builder().id(1L).build();
        CartProduct cartProduct = CartProduct.builder().productId("P002").amount(20).build();
        Product product = new Product("P002", "Amayu", 1.5, "juice");

        when(couponRepository.findByCode("C002")).thenReturn(Mono.just(coupon));
        when(cartRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartProductRepository.findByCartId(1L)).thenReturn(Flux.just(cartProduct));
        when(productRepository.findById("P002")).thenReturn(Mono.just(product));
        when(cartRepository.save(any())).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.applyCoupon(new CouponRequestDto(1L, "C002")))
                .expectNextMatches(summary -> summary.getDiscount() == 1.0)
                .verifyComplete();
    }

    @Test
    void applyCoupon_shouldFailWhenCouponExpired() {
        Coupon coupon = new Coupon("C002", 5, LocalDateTime.now().minusDays(1), "juice");
        Cart cart = Cart.builder().id(1L).build();

        when(couponRepository.findByCode("OLD10")).thenReturn(Mono.just(coupon));
        when(cartRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartProductRepository.findByCartId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(cartService.applyCoupon(new CouponRequestDto(1L, "OLD10")))
                .expectErrorMatches(e -> e.getMessage().equals("Coupon expired"))
                .verify();
    }

    @Test
    void getCartSummary_shouldReturnSummaryWithNoCoupon() {
        Cart cart = Cart.builder().id(1L).build();
        CartProduct cartProduct = CartProduct.builder().productId("P002").amount(20).quantity(2).build();
        Product product = new Product("P002", "Amayu", 1.5, "juice");

        when(cartRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartProductRepository.findByCartId(1L)).thenReturn(Flux.just(cartProduct));
        when(productRepository.findById("P002")).thenReturn(Mono.just(product));

        StepVerifier.create(cartService.getCartSummary(1L))
                .expectNextMatches(summary -> summary.getTotal() == 20.0 && summary.getDiscount() == 0.0)
                .verifyComplete();
    }
}