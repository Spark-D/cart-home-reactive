package com.example.cartreactivedemo.config;

import com.example.cartreactivedemo.util.ThrowingConsumer;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        // Spring WebFlux 에서는 어플리케이션 메모리 문제를 피하기 위해 codec 처리를 위한 in-memory buffer 값이 256KB로 기본설정 되어 있습니다.
        //이 제약 때문에 256KB보다 큰 HTTP 메시지를 처리하려고 하면 DataBufferLimitException 에러가 발생하게 됩니다.
        //이 값을 늘려주기 위해서는 ExchageStrategies.builder() 를 통해 값을 늘려줘야 합니다.
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024*1024*50))
                .build();

        //개발 진행 시 Request/Response 정보를 상세히 확인하기 위해서는 ExchageStrateges 와 logging level 설정을 통해 로그 확인이 가능하도록 해 주는 것이 좋습니다.
        exchangeStrategies
                .messageWriters().stream()
                //Request 또는 Response 데이터에 대해 조작을 하거나 추가 작업을 하기 위해서는 WebClient.builder().filter() 메소드를 이용
                .filter(LoggingCodecSupport.class::isInstance)
                .forEach(writer -> ((LoggingCodecSupport)writer).setEnableLoggingRequestDetails(true));// 로깅사용여부

        //Connection pool Timeout 설정
        ConnectionProvider provider =
                ConnectionProvider.builder("custom")
                        .maxConnections(50)
                        .maxIdleTime(Duration.ofSeconds(20)) //최대 유효 커넥션유지 시간. 대상서버의 타임아웃 시간을 고려해서 설정해야함. 디폴트 설정되지않음.
                        .maxLifeTime(Duration.ofSeconds(60)) //커넥션이 유지되는 최대시간. 디폴트 설정되지않음
                        .pendingAcquireTimeout(Duration.ofSeconds(60)) //pending 되있는 획득?작업이 완료되지 않으면,PoolAcquireTimeoutException 이 발생.디폴트 45s.
                        .evictInBackground(Duration.ofSeconds(120))// evictInBackground 를 설정해서 연결에 대한 주기적인 검사를 수행. 2분마다 커넥션풀에서 제거할수있는 연결을 정기적으로 확인
                        .build();

        return WebClient.builder()
                .baseUrl("https://pbf.lotteon.com")
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient
                                        .create(provider)
                                        .secure(
                                                ThrowingConsumer.unchecked(
                                                        sslContextSpec -> sslContextSpec.sslContext(
                                                                SslContextBuilder
                                                                        .forClient()
                                                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                                                        .build()
                                                        )
                                                )
                                        )
                                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                        .doOnConnected(conn ->
                                                conn.addHandler(new ReadTimeoutHandler(10, TimeUnit.SECONDS)))
//                                        .tcpConfiguration(
//                                                client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
//                                                        .doOnConnected(
//                                                                conn -> conn.addHandlerLast(new ReadTimeoutHandler(180))
//                                                                        .addHandlerLast(new WriteTimeoutHandler(180))
//                                                        )
//                                        )
                        )
                )
                .exchangeStrategies(exchangeStrategies)
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        //요청 메소드, url, 헤더 정보 로깅
                        clientRequest -> {
                            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{} : {}", name, value)));
                            return Mono.just(clientRequest);
                        }
                ))
                .filter(ExchangeFilterFunction.ofResponseProcessor(
                        clientResponse -> {
                            //응답 헤더 로깅
                            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.info("{} : {}", name, value)));
//                            clientResponse.body().
                            return Mono.just(clientResponse);
                        }
                ))
                .defaultHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.3")
                .build();
    }
}
