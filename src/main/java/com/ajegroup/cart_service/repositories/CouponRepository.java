package com.ajegroup.cart_service.repositories;

import com.ajegroup.cart_service.models.Coupon;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface CouponRepository extends R2dbcRepository<Coupon, String> {
    Mono<Coupon> findByCode(String code);
}
