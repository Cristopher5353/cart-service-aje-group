package com.ajegroup.cart_service.repositories;

import com.ajegroup.cart_service.models.CartProduct;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartProductRepository extends R2dbcRepository<CartProduct, Long> {
    Mono<CartProduct> findByCartIdAndProductId(Long cartId, String productId);
    Flux<CartProduct> findByCartId(Long cartId);
}
