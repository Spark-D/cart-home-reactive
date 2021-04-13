package com.example.cartreactivedemo.router;

import com.example.cartreactivedemo.handler.CartHandler;
import com.example.cartreactivedemo.handler.SampleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class CartRouter {

    @Bean
    public RouterFunction<ServerResponse> route(SampleHandler handler){
        return RouterFunctions
                .route( GET("/getTest").and(accept(APPLICATION_JSON)), handler::test);
    }

    @Bean
    public RouterFunction<ServerResponse> root(CartHandler handler){
        //RouterFunctions.route()가 제공하는 빌더를 사용.
        return RouterFunctions.route()
                .GET("/productList/{pageNo}", accept(APPLICATION_JSON), handler::getPrdtList)
                .POST("/cart/test", accept(APPLICATION_JSON), handler::test)
                .GET("/cart/{cartSn}", accept(APPLICATION_JSON), handler::getCartItem)
                .GET("/cart", accept(APPLICATION_JSON), handler::getCartList)
                .GET("/cart/with/{cartSn}", accept(APPLICATION_JSON), handler::getCartItemWithProd)
                .GET("/cart/prod/{cartSn}", accept(APPLICATION_JSON), handler::getCartItemToProd)
                .POST("/cart", accept(APPLICATION_JSON), handler::insertCart)
                .PUT("/cart/{cartSn}", accept(APPLICATION_JSON), handler::updateCart)
                .DELETE("/cart/{cartSn}", accept(APPLICATION_JSON), handler::deleteCart)
                .build();
    }
}
