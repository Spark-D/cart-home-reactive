package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.api.ProductListRes;
import com.example.cartreactivedemo.dto.api.ProductRes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


public interface CartService {
    Flux<OmCart> getCartList();

    Mono<OmCart> insertCart(OmCart omCart);

    Mono<OmCart> updateCart(OmCart omCart);

    Mono<OmCart> findByCartSn(String cartSn);

    Mono<Void> deleteCart(String cartSn);

    Mono<Map>  getApiProdByCartSn(String cartSn);

    Mono<ProductListRes> getGoodsList(Integer pageNo);

    Mono<OmCart> findByCartSnWithProd(String cartSn);

}
