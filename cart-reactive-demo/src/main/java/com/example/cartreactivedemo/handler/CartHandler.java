package com.example.cartreactivedemo.handler;

import com.example.cartreactivedemo.dto.DvGroup;
import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.api.ProductListRes;
import com.example.cartreactivedemo.dto.api.ProductReq;
import com.example.cartreactivedemo.dto.api.ProductRes;
import com.example.cartreactivedemo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class CartHandler {

    @Autowired
    CartService cartService;

    public Mono<ServerResponse> getCartList(ServerRequest serverRequest) {

        Flux<OmCart> omCartFlux = cartService.getCartList();

        return ServerResponse.ok()
                .body(omCartFlux, OmCart.class);
    }


    public Mono<ServerResponse> getAllCart(ServerRequest serverRequest) {
        Flux<OmCart> omCartFlux = cartService.getCartListAll();

        return ServerResponse.ok()
                .body(omCartFlux, OmCart.class);
    }

    public Mono<ServerResponse> getCartItem(ServerRequest serverRequest) {
        String cartSn = serverRequest.pathVariable("cartSn");

        return cartService.findByCartSn(cartSn)
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getCartItemWithProd(ServerRequest serverRequest){
        String cartSn = serverRequest.pathVariable("cartSn");
        return cartService.findByCartSnWithProd(cartSn)
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart))
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


    public Mono<ServerResponse> getCartItemToProd(ServerRequest serverRequest) {
        String cartSn = serverRequest.pathVariable("cartSn");
        Mono<Map>  result = cartService.getApiProdByCartSn(cartSn);

        return ServerResponse.ok()
                .body(result, Map.class);
    }

    public Mono<ServerResponse> getPrdtList(ServerRequest serverRequest) {

        Integer pageNo = Integer.parseInt(serverRequest.pathVariable("pageNo"));
        Mono<ProductListRes> omCartFlux = cartService.getGoodsList(pageNo);

        return ServerResponse.ok()
                .body(omCartFlux, ProductListRes.class);
    }


    public Mono<ServerResponse> test(ServerRequest request){
        return request.bodyToFlux(OmCart.class)
                .flatMap(data ->
                        cartService.getProdMapList(data)
                )
                .collectList()
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> getGroupedCart(ServerRequest request){
        Mono<List<DvGroup>> omCartListMono = cartService.getCartGroupedListAll();

        return ServerResponse.ok()
                .body(omCartListMono, DvGroup.class);
    }




}
