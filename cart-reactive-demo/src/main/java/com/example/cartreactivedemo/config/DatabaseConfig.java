package com.example.cartreactivedemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import reactor.core.publisher.Mono;

@Configuration
@EnableR2dbcAuditing
public class DatabaseConfig implements ReactiveAuditorAware<String> {


    @Override
    public Mono<String> getCurrentAuditor() {
        return Mono.just("SPARK");
    }

    // TODO: 2021-04-22 jsonproperty, dateformatter, enableTransactionManagement,parmeterizedTypeReference로 api 공통스펙 정의, monard
}
