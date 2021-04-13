package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.api.ProductListRes;
import com.example.cartreactivedemo.dto.api.ProductReq;
import com.example.cartreactivedemo.dto.api.ProductRes;
import com.example.cartreactivedemo.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
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
    public Mono<Map> getApiProdByCartSn(String cartSn) {

//        Flux<ProductReq> prdReq = cartRepository.findById(cartSn)
//                .flatMapMany( cart -> {
//                    ProductReq req = new ProductReq();
//                    BeanUtils.copyProperties(req, cart);
//                    return Flux.just(req);
//                });
        List<ProductReq> reqList = new ArrayList<>();
        ProductReq req = new ProductReq("LE1206861333","LE1206861333_1237518036");
        reqList.add(req);

        Mono<Map> result = WebClient.create()
                .post()
                .uri("https://pbf.lotteon.com/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.just(reqList), ProductReq.class)
//                .bodyValue(reqList)
//                .body(prdReq.log(), ProductReq.class )
                .retrieve()
                .bodyToMono(Map.class)
                .log();

        return result;
    }

    private Flux<ProductReq> getBodyData(String cartSn){
        Flux<ProductReq> prdReq = cartRepository.findById(cartSn)
                .flatMapMany( cart -> {
                    ProductReq req = new ProductReq();
                    BeanUtils.copyProperties(req, cart);
                    return Flux.just(req);
                }).log();

        return prdReq;
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

    @Override
    public Mono<OmCart> findByCartSnWithProd(String cartSn) {
        return cartRepository.findById(cartSn)
                .flatMap(this::getProductByCartSn);
    }

    private Mono<OmCart> getProductByCartSn(OmCart omCart) {
        return Mono.just(omCart)
                .zipWith(this.getProdInfo(omCart).collectList())
                .map(combine -> combine.getT1().withProduct(combine.getT2().get(0)));

    }

    private Flux<ProductRes> getProdInfo(OmCart omCart){
        return webClient
                .mutate()
                .baseUrl("https://pbf.lotteon.com/product")
                .build()
                .post()
                .uri("/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.just(omCart), OmCart.class)
                .retrieve()
                .bodyToFlux(ProductRes.class)
                .log();
    }
}
