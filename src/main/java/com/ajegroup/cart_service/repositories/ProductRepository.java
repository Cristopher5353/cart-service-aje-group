package com.ajegroup.cart_service.repositories;

import com.ajegroup.cart_service.models.Product;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ProductRepository extends R2dbcRepository<Product, String> {
}
