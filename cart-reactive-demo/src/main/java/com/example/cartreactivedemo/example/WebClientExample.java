package com.example.cartreactivedemo.example;

import com.example.cartreactivedemo.dto.OmCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class WebClientExample {


    public static void main(String[] args) {

        WebClient client = WebClient.create("https://localhost:8080");

        Mono<ResponseEntity<OmCart>> result = client.get()
                .uri("/cart/{cartSn}", "T01").accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(OmCart.class);
    }

}
