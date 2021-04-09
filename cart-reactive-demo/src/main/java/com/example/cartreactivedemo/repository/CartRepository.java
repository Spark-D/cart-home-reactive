package com.example.cartreactivedemo.repository;

import com.example.cartreactivedemo.dto.OmCart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<OmCart, String> {

}
