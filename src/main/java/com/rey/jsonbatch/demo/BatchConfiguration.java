package com.rey.jsonbatch.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.rey.jsonbatch.BatchEngine;
import com.rey.jsonbatch.JsonBuilder;
import com.rey.jsonbatch.RequestDispatcher;
import com.rey.jsonbatch.apachehttpclient.ApacheHttpClientRequestDispatcher;
import com.rey.jsonbatch.function.BeanShellFunction;
import com.rey.jsonbatch.function.Function;
import com.rey.jsonbatch.function.Functions;
import com.rey.jsonbatch.function.GroovyFunction;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class BatchConfiguration {

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return objectMapper;
    }

    @Bean
    public BatchEngine batchEngine(ObjectMapper objectMapper) {
        com.jayway.jsonpath.Configuration conf = com.jayway.jsonpath.Configuration.builder()
                .options(Option.SUPPRESS_EXCEPTIONS)
                .jsonProvider(new JacksonJsonProvider(objectMapper))
                .mappingProvider(new JacksonMappingProvider(objectMapper))
                .build();

        List<Function> functions = new ArrayList<>();
        Collections.addAll(functions, Functions.basic());
        Collections.addAll(functions, new BeanShellFunction(), new GroovyFunction());
        JsonBuilder jsonBuilder = new JsonBuilder(functions.toArray(new Function[0]));

        RequestDispatcher requestDispatcher = new ApacheHttpClientRequestDispatcher(HttpClients.createDefault());

        return new BatchEngine(conf, jsonBuilder, requestDispatcher);
    }

}
