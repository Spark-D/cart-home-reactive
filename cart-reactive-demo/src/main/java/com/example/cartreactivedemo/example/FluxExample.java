package com.example.cartreactivedemo.example;

import com.example.cartreactivedemo.dto.DvGroup;
import com.example.cartreactivedemo.dto.OmCart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

public class FluxExample {


    public static void main(String [] args) {
        final List<String> basket1 = Arrays.asList(new String[]{"kiwi", "orange", "lemon", "orange", "lemon", "kiwi"});
        final List<String> basket2 = Arrays.asList(new String[]{"banana", "lemon", "lemon", "kiwi"});
        final List<String> basket3 = Arrays.asList(new String[]{"strawberry", "orange", "lemon", "grape", "strawberry"});
        final List<List<String>> baskets = Arrays.asList(basket1, basket2, basket3);
        final Flux<List<String>> basketFlux = Flux.fromIterable(baskets);

        basketFlux.concatMap(basket -> {
            final Mono<List<String>> distinctFruits = Flux.fromIterable(basket).distinct().collectList().log("mono list>>>>>>");
            final Mono<Map<String, List>> countFruitsMono = Flux.fromIterable(basket).log("mono map>>>>>")
                    .groupBy(fruit -> fruit)// 바구니로 부터 넘어온 과일 기준으로 group을 묶는다.
                    .concatMap(groupedFlux -> groupedFlux.collectList() //groupedFlux.count()
                            .map(count -> {
                                System.out.println("groupByTest::"+ count.toString());
                                final Map<String, List> fruitCount = new LinkedHashMap<>();
                                fruitCount.put(groupedFlux.key(), count);
                                return fruitCount;
                            }) // 각 과일별로 개수를 Map으로 리턴
                    )
                    .log("concat map>>>>>") // concatMap으로 순서보장
                    .reduce((accumulatedMap, currentMap) -> new LinkedHashMap<String, List>() { {
                        putAll(accumulatedMap);
                        putAll(currentMap);
                    }}); // 그동안 누적된 accumulatedMap에 현재 넘어오는 currentMap을 합쳐서 새로운 Map을 만든다. // map끼리 putAll하여 하나의 Map으로 만든다.
            // return ???
            return countFruitsMono;
        });


        List<OmCart> cartList = new ArrayList<>();
        cartList.add(new OmCart("T01", "LE", "LE01", "자켓", 1, LocalDateTime.now().minusMonths(1))); //5
        cartList.add(new OmCart("T02", "LE", "LE01", "청바지", 1, LocalDateTime.now().minusDays(1))); // 3
        cartList.add(new OmCart("T03", "LM", "LM01", "너구리", 1, LocalDateTime.now().minusDays(2))); //4
        cartList.add(new OmCart("T04", "LO", "LO01", "마스크", 1, LocalDateTime.now().minusMinutes(10))); //2
        cartList.add(new OmCart("T05", "LB", "LB01", "립스틱", 1, LocalDateTime.now()));  //1

//        Flux.fromIterable(cartList)
//                .subscribe(cart -> System.out.println(cart.toString()));

        //등록 시간 내림차순
//        Flux.fromIterable(cartList)
//                .sort(Comparator.comparing(OmCart::getRegDttm).reversed())
//                .doOnNext(cart -> System.out.println(cart))
//                .subscribe();


        //trNo로 그룹바이
//        Flux.fromIterable(cartList).sort(Comparator.comparing(OmCart::getRegDttm).reversed())
//                .groupBy(cart -> cart.getTrNo())
//                .concatMap( trNo -> {
//                    Mono<Map<Object, List>> mono = trNo.collectList()
//                            .map( list -> {
//                        Map<Object,List> item = new HashMap<>();
//                        item.put(trNo.key(), list);
//                        return item;
//                    });
//                    return mono;
//                }).subscribe(data -> {
//            System.out.println(data);
//        });


        Flux.fromIterable(cartList).sort(Comparator.comparing(OmCart::getRegDttm).reversed())
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
                .collectList()
                .subscribe(data -> {
//                    System.out.println(data);
        });


        /////////////////////////////


//        two();
//        groupByTest();

    }

    private static void two() {
        List<String> array = new ArrayList<String>();
        array.addAll(Arrays.asList(new String[]{"a", "b", "c", "d", "e", "e"}));

        Flux.fromIterable(array).groupBy( arr -> arr).map( arg-> {  //첫번째 map, arg는 GroupedFlux
//                arg.subscribe(data-> System.out.println(data.toString()));
                Mono<Map<Object, Object>> mono = arg.count().map( count -> { //두번째 map, arg.count()는 Mono<Long>
                    Map<Object, Object> item = new HashMap<>();
                    item.put(arg.key(), count);
                    return item;
            });
            return mono;  //첫번째 map 값을 Mono<Map<Object, Object>>로 바꾼다음,
        }).subscribe( (data)->{
//            System.out.println(data);
            data.subscribe(System.out::println);
        });
    }

    private static void groupByTest() {
        Flux.range(1, 258)
                .groupBy(val -> val)
                .concatMap(g -> g.map(val -> val + "test"))
//                .subscribe(data-> System.out::println);
                .doOnNext(System.out::println);
//                .blockLast();
    }
}
