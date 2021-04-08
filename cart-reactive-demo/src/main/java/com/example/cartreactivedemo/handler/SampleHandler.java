package com.example.cartreactivedemo.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class SampleHandler {
    public Mono<ServerResponse> test(ServerRequest serverRequest) {

        return ServerResponse.ok()
                .body("hello test", String.class);
    }
}
