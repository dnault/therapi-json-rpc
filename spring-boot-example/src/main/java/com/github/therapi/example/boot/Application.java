package com.github.therapi.example.boot;

import static com.github.therapi.jackson.ObjectMappers.newLenientObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.therapi.jackson.enums.LowerCamelCaseEnumModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * This sample application uses component scanning to detect and instantiate the service beans.
 * In a real application you can define the beans using whatever technique you prefer.
 *
 * @see ExampleJsonRpcController
 * @see SomeOtherRestController
 * @see GreetingServiceImpl
 *
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * This is one way to create and customize the Jackson ObjectMapper
     * that will convert your Java objects to and from JSON.
     */
    @Bean
    public ObjectMapper jsonRpcObjectMapper() {
        ObjectMapper objectMapper = newLenientObjectMapper();
        objectMapper.registerModule(new LowerCamelCaseEnumModule());
        return objectMapper;
    }
}
