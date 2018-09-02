package com.github.therapi.jsonrpc.web;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Spring to serve the static resources required by the API documentation web page.
 * Only necessary if the API documentation page renders incorrectly without it.
 * <p>
 * Normally the static resources are automatically served by the servlet container,
 * but Spring's default Web MVC configuration may prevent that from happening.
 * <p>
 * To activate, add a bean of this class to your application context.
 */
public class ApidocWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/therapi/**")
                .addResourceLocations("classpath:/META-INF/resources/therapi/");
    }
}
