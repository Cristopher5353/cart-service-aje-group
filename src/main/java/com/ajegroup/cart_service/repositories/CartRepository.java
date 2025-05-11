package com.ajegroup.cart_service.repositories;

import com.ajegroup.cart_service.models.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CartRepository extends R2dbcRepository<Cart, Long> {
}