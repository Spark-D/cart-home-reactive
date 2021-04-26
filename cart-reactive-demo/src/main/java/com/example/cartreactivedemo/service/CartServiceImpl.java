package com.example.cartreactivedemo.service;

import com.example.cartreactivedemo.dto.DvGroup;
import com.example.cartreactivedemo.dto.OmCart;
import com.example.cartreactivedemo.dto.ReturnCode;
import com.example.cartreactivedemo.dto.api.ProductListRes;
import com.example.cartreactivedemo.dto.api.ProductReq;
import com.example.cartreactivedemo.dto.api.ReturnDto;
import com.example.cartreactivedemo.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Transactional(propagation = Propagation.REQUIRED , rollbackFor = Exception.class)
    public Mono<OmCart> insertCart(OmCart omCart) {


        return Mono.just(omCart)
                .flatMap(cart-> this.getProdDtoList(omCart))
                .map(x -> {
                    if(!x.getReturnCode().equals(ReturnCode.OK.getCode())){
                        log.info("상품정보 오류 : {}", x.getReturnCode());
                        return omCart;
                    }
                    OmCart c = x.getData().get(0);
                    BeanUtils.copyProperties(omCart, c, "trNo","lrtrNo"); // 원본, 복사대상
                    return c;
                })
                .flatMap(cartRepository::save);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED , rollbackFor = Exception.class)
    public Mono<OmCart> updateCart(OmCart omCart) {
        return cartRepository.save(omCart);
    }

    @Override
    public Mono<OmCart> findByCartSn(String cartSn) {
        return cartRepository.findById(cartSn);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED , rollbackFor = Exception.class)
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

        Mono<Map> result = webClient
                .post()
                .uri("/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Flux.just(reqList), ProductReq.class)
                .retrieve()
                .bodyToMono(Map.class);

        return result;
    }

    private Flux<ProductReq> getBodyData(String cartSn){
        Flux<ProductReq> prdReq = cartRepository.findById(cartSn)
                .flatMapMany( cart -> {
                    ProductReq req = new ProductReq();
                    BeanUtils.copyProperties(req, cart);
                    return Flux.just(req);
                });

        return prdReq;
    }

    @Override
    public Mono<ProductListRes> getGoodsList(Integer pageNo) {

        //기존 설정값을 상속해서 사용할 수 있는 mutate() 함수를 제공
        return webClient
                .mutate()
                .baseUrl("https://www.lotteon.com")
                .defaultHeader("user-agent", "WebClient")
                .build()
                .get()
                .uri("/p/display/category/seltCatePdWishListAjax?pdSortCd=01&pageNo={pageNo}&rowsPerPage=60&dshopNo=22276", pageNo)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer ***Token")
                //retrieve 를 이용하면 바로 ResponseBody를 처리 할 수 있고, exchange 를 이용하면 세세한 컨트롤이 가능
                //Spring에서는 exchange 를 이용하게 되면 Response 컨텐츠에 대한 모든 처리를 직접 하면서 발생할 수 있는 memory leak(누수) 가능성 때문에 가급적 retrieve 를 사용하기를 권고
                .retrieve()
                //각 상태코드에 따라 임의의 처리를 하거나 Exception 을 랩핑하고 싶을 때는 onStatus() 함수를 사용하여 해결 할 수 있습니다.
                .onStatus(status -> status.is4xxClientError()
                                || status.is5xxServerError()
                        , clientResponse ->
                                clientResponse.bodyToMono(String.class)
                                        .map(body -> new RuntimeException(body)))
                .bodyToMono(ProductListRes.class);
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
        return webClient
                .mutate()
                .build()
                .post()
                .uri("/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                //Mono 나 Flux 객체를 통해 RequestBody시 사용하는 RequestBodySpec
                .body(Flux.just(data), OmCart.class)
                //form 데이터 전송시 BodyInserters.fromFormData() 또는 bodyValue(MultiValueMap<String, String>) 로 데이터 전송
                //.body(BodyInserters.fromFormData("id", idValue)
                //                            .with("pwd", pwdValue)
                //         )
                .retrieve()
                .bodyToFlux(Map.class);
    }

    public Mono<ReturnDto> getProdDtoList(OmCart data) {
        return webClient
                .mutate()
                .build()
                .post()
                .uri("/product/v1/detail/productDetailList?dataType=LIGHT2")
                .contentType(MediaType.APPLICATION_JSON)
                //Mono 나 Flux 객체를 통해 RequestBody시 사용하는 RequestBodySpec
                .body(Flux.just(data), OmCart.class)
                //form 데이터 전송시 BodyInserters.fromFormData() 또는 bodyValue(MultiValueMap<String, String>) 로 데이터 전송
                //.body(BodyInserters.fromFormData("id", idValue)
                //                            .with("pwd", pwdValue)
                //         )
                .retrieve()
                .bodyToMono(ReturnDto.class);
    }

    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors)
    {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());

            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    private Flux<OmCart> getCartListWithProductList() {
        //전체 카트리스트
        Flux<OmCart> cartList = cartRepository.findAll();
        //중복제거 후 상품정보 요청
        Flux<ReturnDto> listFlux = getProductInfoMapList(cartList);

        //mapping
        return cartList.concatMap(c->{
            return listFlux.concatMap(p -> {
                log.info("cart >>>>>>{}", c );
                p.getData().stream().forEach( api ->{
                    if(api.getSitmNo().equals(c.getSitmNo()) && api.getSpdNo().equals(c.getSpdNo())){
                        c.setProduct(api);
                    }
                });
                return Flux.just(c);
            });
        });
    }

    private Flux<ReturnDto> getProductInfoMapList(Flux<OmCart> cartList) {

        Mono<List<OmCart>> listMono = cartList.collectList().map(x -> {
            return x.stream().filter(distinctByKeys(OmCart::getSpdNo, OmCart::getSitmNo))
                    .collect(Collectors.toList());
        });

        return listMono.flatMapMany( data -> {
            return webClient.mutate()
                    .build()
                    .post()
                    .uri("/product/v1/detail/productDetailList?dataType=LIGHT2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToFlux(ReturnDto.class);
        });
    }

    @Override
    public Mono<List<DvGroup>> getCartGroupedListAll() {
        return getDvGroupList();
    }


    private Mono<List<DvGroup>> getDvGroupList() {
//        Flux<OmCart> cartList = cartRepository.findAll();
//        Flux<List<Map>> cartListWithProduct = cartList
//                .concatMap(cart-> this.getProdMapList(cart).collectList());

        //등록일자 순 내림차순정렬
        //trNo로 group by
        return this.getCartListWithProductList()
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
                })
                .sort(Comparator.comparing(DvGroup::getRegDttm).reversed())
                .collectList();
    }



    private Mono<OmCart> getProductByCartSn(OmCart omCart) {
        return Mono.just(omCart)
                .zipWith(this.getProdInfo(omCart).collectList())
                .map(combine -> combine.getT1().withProduct(combine.getT2().get(0)));

    }

    private Flux<Map> getProdInfo(OmCart omCart){
        return Flux.just(omCart)
                .flatMap(data -> webClient.mutate()
                                .build()
                                .post()
                                .uri("/product/v1/detail/productDetailList?dataType=LIGHT2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(data)
                                .retrieve()
                                .bodyToFlux(Map.class)
                );
    }
}
