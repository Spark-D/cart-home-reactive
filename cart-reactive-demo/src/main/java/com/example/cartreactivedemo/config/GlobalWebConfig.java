package com.example.cartreactivedemo.config;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


@Configuration
@EnableWebFlux
public class GlobalWebConfig implements WebFluxConfigurer {

    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // AllowCredentials을 true로 지정할 시 AllowedOrigin는 *를 받을 수 없다. private location만 적용 가능.
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }


    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true);

        ObjectMapper jackson2ObjectMapperConfigure = Jackson2ObjectMapperBuilder
                .json()
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer())
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer())
                .build()
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                ;

        configurer.defaultCodecs().jackson2SmileDecoder(new Jackson2JsonDecoder(jackson2ObjectMapperConfigure));
        configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(jackson2ObjectMapperConfigure));
    }

    class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

        protected LocalDateTimeSerializer(Class t) {
            super(t);
        }

        public LocalDateTimeSerializer() {
            this(null);
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if(Objects.nonNull(value)) {
                gen.writeString(value.format(DateTimeFormatter.ISO_DATE));
            }
        }
    }

    class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

        protected LocalDateTimeDeserializer(Class vc) {
            super(vc);
        }

        public LocalDateTimeDeserializer() {
            this(null);
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return LocalDateTime.parse(p.getText());
        }
    }

}

