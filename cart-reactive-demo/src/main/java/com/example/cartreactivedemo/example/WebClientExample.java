package com.example.cartreactivedemo.example;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.api.ProductReq;
import com.example.cartreactivedemo.dto.api.ProductRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class WebClientExample {


    public static void main(String[] args) {

        ProductReq req = new ProductReq();
        req.setSpdNo("LE1206861333");
        req.setSitmNo("LE1206861333_1237518036");
        List<ProductReq> prdList = new ArrayList<>();
        prdList.add(req);

        WebClient client = WebClient.create();

        Flux<ProductRes> res = client.post()
                .uri("https://pbf.lotteon.com/product/v1/detail/productDetailList?dataType=LIGHT2")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(prdList)
                .retrieve()
                .bodyToFlux(ProductRes.class);

        res.subscribe(System.out::println);
    }

}
