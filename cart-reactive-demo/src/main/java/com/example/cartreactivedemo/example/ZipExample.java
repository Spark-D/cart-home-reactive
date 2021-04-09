package com.example.cartreactivedemo.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class ZipExample {
    public static void main(String[] args) {

//        zipStreams1().zipWith(zipStreams2(), (one, two) -> one + "_" + two).subscribe(item -> System.out.println(item));

        monoStream().zipWith(zipStreams1().collectList())
                .map(data -> data.getT1() + "_"+ data.getT2())
                .subscribe(System.out::println);
    }

    private static Flux<String> zipStreams1() {
        final List<String> items1 = Arrays.asList("one", "two", "three");
        return Flux.fromIterable(items1);
    }

    private static Flux<String> zipStreams2() {
        final List<String> items2 = Arrays.asList("five", "six", "seven");
        return Flux.fromIterable(items2);
    }

    private static Mono<String> monoStream() {
        final String odNo = "T1234";
        return Mono.just(odNo);
    }
}
