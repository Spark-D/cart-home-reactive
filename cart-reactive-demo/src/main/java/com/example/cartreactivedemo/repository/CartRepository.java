package com.example.cartreactivedemo.repository;

import com.example.cartreactivedemo.dto.OmCart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CartRepository extends ReactiveCrudRepository<OmCart, String> {

    @Query("SELECT 'T'|| to_char(now(), '24MISSMS') || trunc(random() * 100 + 1)")
    Mono<String> getSeq();
}
