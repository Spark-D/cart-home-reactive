package com.example.cartreactivedemo.config;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.repository.CartRepository;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@EnableWebFlux
@Configuration
public class CallbackConfig implements BeforeConvertCallback<OmCart>, BeforeSaveCallback<OmCart> {

    @Autowired
    CartRepository repository;

    @Override
    public Publisher<OmCart> onBeforeConvert(OmCart entity, SqlIdentifier table) {
        System.out.println("onBeforeConvert!!!!!!!!!!!!!!!!"+ table);
        // TODO: 2021-04-16  Auditing
        if (entity.getCartSn() == null || entity.getCartSn() == "") {
            return repository.getSeq().map(seq -> {
                entity.setRegDttm(LocalDateTime.now());
                entity.setCartSn(seq);
                return entity;
            }).log("new sequence :::" + entity.toString());
        } else {
            entity.setRegDttm(LocalDateTime.now());
            Mono<OmCart> modiTodo = Mono.just(entity);
            return modiTodo;
        }
    }

    @Override
    public Publisher<OmCart> onBeforeSave(OmCart entity, OutboundRow row, SqlIdentifier table) {
        Mono<OmCart> cart = Mono.just(entity);
        return cart;
    }
}
