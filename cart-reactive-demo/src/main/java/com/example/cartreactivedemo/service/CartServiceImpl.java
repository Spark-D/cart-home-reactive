package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.ProductReq;
import com.example.cartreactivedemo.dto.ProductRes;
import com.example.cartreactivedemo.repository.CartRepository;
import com.fasterxml.jackson.core.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    WebClient.Builder builder ;

    @Override
    public Flux<OmCart> getCartList() {
        return cartRepository.findAll();
    }

    @Override
    public Mono<OmCart> insertCart(OmCart omCart) {
        return cartRepository.save(omCart);
    }

    @Override
    public Mono<OmCart> updateCart(OmCart omCart) {
        return cartRepository.save(omCart);
    }

    @Override
    public Mono<OmCart> findByCartSn(String cartSn) {


        Mono<ProductReq> productReqMono = cartRepository.findById(cartSn)
                .map(x -> {
                    ProductReq req = new ProductReq();
                    req.setSitmNo(x.getSitmNo());
                    req.setSpdNo(x.getSpdNo());
                    return req;
                });

        productReqMono.map(pro -> builder.build()
                .post()
                .uri("https://pbf.lotteon.com/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(pro)
                .retrieve()
                .bodyToMono(ProductRes.class)
        );



        return cartRepository.findById(cartSn);
    }

    @Override
    public Mono<Void> deleteCart(String cartSn) {
        return cartRepository.deleteById(cartSn);
    }
}
