package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.DvGroup;
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
import java.util.Comparator;
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

    @Override
    public Flux<OmCart> getCartListAll() {
        return getCartListWithProductList();
    }

    @Override
    public Flux<Map> getProdMapList(OmCart data) {
        return WebClient.create("https://pbf.lotteon.com/product/v1/detail/productDetailList?dataType=LIGHT2")
                        .post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Flux.just(data), OmCart.class)
                        .retrieve()
                        .bodyToFlux(Map.class).log("after map------------>>");
    }


    private Flux<OmCart> getCartListWithProductList() {
        Flux<OmCart> cartList = cartRepository.findAll();
        Flux<List<Map>> cartListWithProduct = cartList
                .flatMap(cart-> this.getProdMapList(cart).collectList());
        return Flux.zip(cartList,cartListWithProduct,(t1,t2)-> t1.withProduct(t2.get(0).get("data")));
    }

    @Override
    public Mono<List<DvGroup>> getCartGroupedListAll() {
        return getDvGroupList();
    }


    private Mono<List<DvGroup>> getDvGroupList() {
        Flux<OmCart> cartList = cartRepository.findAll();
        Flux<List<Map>> cartListWithProduct = cartList
                .flatMap(cart-> this.getProdMapList(cart).collectList());

        return Flux.zip(cartList,cartListWithProduct,(t1,t2)-> t1.withProduct(t2.get(0).get("data")))
                .sort(Comparator.comparing(OmCart::getRegDttm).reversed())
                .groupBy(cart -> cart.getTrNo())
                .concatMap( trNo -> {
                    Mono<DvGroup> mono = trNo.collectList()
                            .map( list -> {
                                DvGroup dvGroup = new DvGroup();
                                dvGroup.setTrNo(trNo.key());
                                dvGroup.setOmCartList(list);
                                dvGroup.setRegDttm(list.stream().sorted(Comparator.comparing(OmCart::getRegDttm).reversed()).findFirst().orElse(new OmCart()).getRegDttm());
                                return dvGroup;
                            });
                    return mono;
                }).collectList();
    }



    private Mono<OmCart> getProductByCartSn(OmCart omCart) {
        return Mono.just(omCart)
                .zipWith(this.getProdInfo(omCart).collectList())
                .map(combine -> combine.getT1().withProduct(combine.getT2().get(0)));

    }

    private Flux<Map> getProdInfo(OmCart omCart){
        return Flux.just(omCart)
                .log("getProdInfo request >>>>>>>>>>>>>>>>>>>>>>>")
                .flatMap(data -> webClient.mutate()
                                .baseUrl("https://pbf.lotteon.com/product")
                                .build()
                                .post()
                                .uri("/v1/detail/productDetailList?dataType=LIGHT2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(data)
//                                .body(data, OmCart.class)
                                .retrieve()
                                .bodyToFlux(Map.class)
                                .log("getProdInfo response >>>>>>>>>>>>>>>>>>>>>>>"));
    }


}
