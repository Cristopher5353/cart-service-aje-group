package com.ajegroup.cart_service.services;

import com.ajegroup.cart_service.dtos.records.ProductWithCartRecord;
import com.ajegroup.cart_service.dtos.request.CartProductRequestDto;
import com.ajegroup.cart_service.dtos.request.CouponRequestDto;
import com.ajegroup.cart_service.dtos.response.CartSummaryDto;
import com.ajegroup.cart_service.dtos.response.ProductSummaryDto;
import com.ajegroup.cart_service.dtos.response.TotalSummaryDto;
import com.ajegroup.cart_service.exceptions.ElementNotFoundException;
import com.ajegroup.cart_service.models.Cart;
import com.ajegroup.cart_service.models.CartProduct;
import com.ajegroup.cart_service.models.Coupon;
import com.ajegroup.cart_service.models.Product;
import com.ajegroup.cart_service.repositories.CartProductRepository;
import com.ajegroup.cart_service.repositories.CartRepository;
import com.ajegroup.cart_service.repositories.CouponRepository;
import com.ajegroup.cart_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final CouponRepository couponRepository;

    @Override
    public Mono<Long> addCartProduct(CartProductRequestDto cartProductRequestDto) {
        return cartRepository.findById(cartProductRequestDto.getCartId())
                .switchIfEmpty(cartRepository.save(Cart.builder().createdAt(LocalDateTime.now()).build()))
                .flatMap(cart -> productRepository.findById(cartProductRequestDto.getProductId())
                        .flatMap(product -> cartProductRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                                .flatMap(existingCartProduct -> updateExistingCartProduct(
                                        existingCartProduct,
                                        cartProductRequestDto,
                                        product))
                                .switchIfEmpty(createNewCartProduct(cartProductRequestDto, cart, product))
                        ).thenReturn(cart.getId()));
    }

    @Override
    public Mono<Void> removeProduct(Long cartProductId) {
        return cartProductRepository.findById(cartProductId)
                .flatMap(cartProductRepository::delete)
                .then();
    }

    @Override
    public Mono<TotalSummaryDto> applyCoupon(CouponRequestDto couponRequestDto) {
        return Mono.zip(
                couponRepository.findByCode(couponRequestDto.getCouponCode())
                        .switchIfEmpty(Mono.error(new ElementNotFoundException("Coupon not exists"))),
                cartRepository.findById(couponRequestDto.getCartId())
                        .switchIfEmpty(Mono.error(new ElementNotFoundException("Cart not found"))),
                cartProductRepository.findByCartId(couponRequestDto.getCartId())
                        .collectList())
                .flatMap(response -> {
                    Coupon coupon = response.getT1();
                    Cart cart = response.getT2();
                    List<CartProduct> cartProducts = response.getT3();

                    if(coupon.getValidDate().isBefore(LocalDateTime.now())) {
                        return Mono.error(new RuntimeException("Coupon expired"));
                    }

                    return Flux.fromIterable(cartProducts)
                            .flatMap(cartProduct -> productRepository.findById(cartProduct.getProductId())
                                    .map(product -> new ProductWithCartRecord(cartProduct, product)))
                            .collectList()
                            .flatMap(productWithCart -> applyCouponProcess(productWithCart, coupon, cart));
                });
    }

    @Override
    public Mono<CartSummaryDto> getCartSummary(Long cartId) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(new ElementNotFoundException("Cart not found")))
                .flatMap(cart -> cartProductRepository.findByCartId(cartId).collectList()
                        .flatMapMany(Flux::fromIterable)
                        .flatMap(cartProduct -> productRepository.findById(cartProduct.getProductId())
                                .map(product -> new ProductWithCartRecord(cartProduct, product)))
                        .collectList()
                        .flatMap(productWithCart -> getCartSummaryProcess(cart, productWithCart)));
    }

    private Mono<CartProduct> updateExistingCartProduct(CartProduct existingCartProduct,
                                                        CartProductRequestDto cartProductRequestDto,
                                                        Product product) {
        int newQuantity = existingCartProduct.getQuantity() + cartProductRequestDto.getQuantity();
        double newAmount = product.getPrice() * newQuantity;

        existingCartProduct.setQuantity(newQuantity);
        existingCartProduct.setAmount(newAmount);

        return cartProductRepository.save(existingCartProduct);
    }

    private Mono<CartProduct> createNewCartProduct(CartProductRequestDto cartProductRequestDto,
                                                   Cart cart,
                                                   Product product) {
        CartProduct newCartProduct = CartProduct.builder()
                .quantity(cartProductRequestDto.getQuantity())
                .productId(product.getId())
                .cartId(cart.getId())
                .amount(product.getPrice() * cartProductRequestDto.getQuantity())
                .build();

        return cartProductRepository.save(newCartProduct);
    }

    private double getTotalCart(List<ProductWithCartRecord> productWithCart) {
        return productWithCart.stream()
                .mapToDouble(item -> item.cartProduct().getAmount())
                .sum();
    }

    private double getTotalDiscountCart(List<ProductWithCartRecord> productWithCart, Coupon coupon) {
        return productWithCart.stream()
                .filter(item -> Arrays.asList(coupon.getCategories().split(","))
                        .contains(item.product().getCategory()))
                .mapToDouble(item -> item.cartProduct().getAmount())
                .sum();
    }

    private Mono<TotalSummaryDto> applyCouponProcess(List<ProductWithCartRecord> productWithCart,
                                                     Coupon coupon,
                                                     Cart cart) {
        double total = getTotalCart(productWithCart);
        double discountTotal = getTotalDiscountCart(productWithCart, coupon);

        double discount = discountTotal * (coupon.getDiscountPercent() / 100.0);
        double totalAfterDiscount = total - discount;

        cart.setCouponCode(coupon.getCode());

        return cartRepository.save(cart).thenReturn(
                TotalSummaryDto.builder()
                        .total(total)
                        .discount(discount)
                        .totalAfterDiscount(totalAfterDiscount).build());
    }

    private List<ProductSummaryDto> toListProductSummaryDto(List<ProductWithCartRecord> productWithCart) {
        return productWithCart.stream()
                .map(item -> ProductSummaryDto.builder()
                        .itemId(item.cartProduct().getId())
                        .id(item.cartProduct().getProductId())
                        .name(item.product().getName())
                        .category(item.product().getCategory())
                        .quantity(item.cartProduct().getQuantity())
                        .price(item.product().getPrice())
                        .amount(Math.round(roundNumber(item.cartProduct().getAmount())))
                        .build())
                .toList();
    }

    private Mono<CartSummaryDto> getCartSummaryProcess(Cart cart,
                                                       List<ProductWithCartRecord> productWithCart) {
        return (cart.getCouponCode() == null
                ? Mono.just(0.0)
                : getTotalDiscountCartExistsCoupon(cart, productWithCart).defaultIfEmpty(0.0))
                .map(discount -> createSummaryDto(productWithCart, discount));
    }

    private Mono<Double> getTotalDiscountCartExistsCoupon(Cart cart, List<ProductWithCartRecord> productWithCart) {
        return couponRepository.findByCode(cart.getCouponCode())
                .filter(coupon -> coupon.getValidDate().isAfter(LocalDateTime.now()))
                .map(coupon -> getTotalDiscountCart(productWithCart, coupon) * (coupon.getDiscountPercent() / 100.0));
    }

    private CartSummaryDto createSummaryDto(List<ProductWithCartRecord> productWithCart,
                                                  double discount) {
        List<ProductSummaryDto> productSummaryDtos = toListProductSummaryDto(productWithCart);
        double total = getTotalCart(productWithCart);

        double totalAfterDiscount = total - discount;
        double totalInUSD = totalAfterDiscount / 3.63;

        return CartSummaryDto.builder()
                .products(productSummaryDtos)
                .total(total)
                .discount(discount)
                .totalAfterDiscount(roundNumber(totalAfterDiscount))
                .totalInUsd(roundNumber(totalInUSD))
                .build();
    }

    private double roundNumber(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
