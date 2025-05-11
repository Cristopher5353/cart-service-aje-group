package com.ajegroup.cart_service.dtos.records;

import com.ajegroup.cart_service.models.CartProduct;
import com.ajegroup.cart_service.models.Product;

public record ProductWithCartRecord (CartProduct cartProduct, Product product){}