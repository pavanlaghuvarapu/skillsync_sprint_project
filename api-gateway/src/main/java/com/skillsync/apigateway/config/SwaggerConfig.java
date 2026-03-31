package com.skillsync.apigateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setConfigUrl("/v3/api-docs/swagger-config");

        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

        urls.add(createUrl("auth-service",     "/auth-service/v3/api-docs"));
        urls.add(createUrl("user-service",     "/user-service/v3/api-docs"));
        urls.add(createUrl("mentor-service",   "/mentor-service/v3/api-docs"));
        urls.add(createUrl("session-service",  "/session-service/v3/api-docs"));
        urls.add(createUrl("review-service",   "/review-service/v3/api-docs"));
        urls.add(createUrl("notification-service", "/notification-service/v3/api-docs"));
        urls.add(createUrl("group-service",    "/group-service/v3/api-docs"));

        props.setUrls(urls);
        return props;
    }

    private AbstractSwaggerUiConfigProperties.SwaggerUrl createUrl(String name, String url) {
        AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        swaggerUrl.setName(name);
        swaggerUrl.setUrl(url);
        return swaggerUrl;
    }
}
