package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.api.ProductListRes;
import com.example.cartreactivedemo.dto.api.ProductReq;
import com.example.cartreactivedemo.dto.api.ProductRes;
import com.example.cartreactivedemo.repository.CartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    WebClient webClient ;

    @Override
    public Flux<OmCart> getCartList() {
        return cartRepository.findAll();
    }

    @Override
    public Mono<OmCart> insertCart(OmCart omCart) {
        return cartRepository.save(omCart);
    }

    @Override
    public Mono<OmCart> updateCart(OmCart omCart) {
        return cartRepository.save(omCart);
    }

    @Override
    public Mono<OmCart> findByCartSn(String cartSn) {
        return cartRepository.findById(cartSn);
    }

    @Override
    public Mono<Void> deleteCart(String cartSn) {
        return cartRepository.deleteById(cartSn);
    }

    @Override
    public Mono<ProductRes> getApiProdByCartSn(String cartSn) {

        List<ProductReq> productReq= new ArrayList<>();
        ProductReq req = new ProductReq();
        req.setSitmNo("LE1206861333");
        req.setSpdNo("LE1206861333_1237518036");
        productReq.add(req);

        log.info("productReq : {}", productReq);

//        Mono<List<ProductReq>> m = Mono.just(productReq);
        Mono<ProductRes> result = webClient
                .post()
                .uri("https://pbf.lotteon.com/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productReq).flatMapMany(Flux::fromIterable), ProductReq.class )
                .retrieve()
                .onStatus(status -> status.is5xxServerError() || status.is4xxClientError()
                    , clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new RuntimeException(body))
                            )
                .bodyToMono(ProductRes.class)
                .log();

        return result;
    }

    @Override
    public Mono<ProductListRes> getGoodsList(Integer pageNo) {

        return webClient
                .mutate()
                .baseUrl("https://www.lotteon.com/p/display")
                .build()
                .get()
                .uri("/category/seltCatePdWishListAjax?pdSortCd=01&pageNo={pageNo}&rowsPerPage=60&dshopNo=22276", pageNo)
                .accept(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION)
                .retrieve()
                .bodyToMono(ProductListRes.class)
                .log();
    }


}
