package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.OmCart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface CartService {
    Flux<OmCart> getCartList();

    Mono<OmCart> insertCart(OmCart omCart);

    Mono<OmCart> updateCart(OmCart omCart);

    Mono<OmCart> findByCartSn(String cartSn);

    Mono<Void> deleteCart(String cartSn);
}
