package com.example.cartreactivedemo.handler;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.service.CartService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CartHandler {

    @Autowired
    CartService cartService;

    public Mono<ServerResponse> getCartList(ServerRequest serverRequest) {

        Flux<OmCart> omCartFlux = cartService.getCartList();

        return ServerResponse.ok()
                .body(omCartFlux, OmCart.class);
    }

    public Mono<ServerResponse> getCartItem(ServerRequest serverRequest) {
        String cartSn = serverRequest.pathVariable("cartSn");

        return cartService.findByCartSn(cartSn)
                .flatMap(customer -> ServerResponse.ok().bodyValue(customer))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> insertCart(ServerRequest serverRequest) {

        Mono<OmCart> omCartMono = serverRequest.bodyToMono(OmCart.class)
                .flatMap(cartService::insertCart);

        return ServerResponse.ok()
                .body(omCartMono, OmCart.class);
    }

    public Mono<ServerResponse> updateCart(ServerRequest serverRequest) {

        Mono<OmCart> omCartMono = serverRequest.bodyToMono(OmCart.class)
                .flatMap(cartService::updateCart);

        return ServerResponse.ok()
                .body(omCartMono, OmCart.class);
    }

    public Mono<ServerResponse> deleteCart(ServerRequest serverRequest) {

        String cartSn = serverRequest.pathVariable("cartSn");

        return cartService.findByCartSn(cartSn)
                .flatMap(cart -> {
                    Mono<Void> delete = cartService.deleteCart(cart.getCartSn());
                    return ServerResponse.ok().body(delete, Void.class);
                })
                .switchIfEmpty(ServerResponse.notFound().build());

    }


}
