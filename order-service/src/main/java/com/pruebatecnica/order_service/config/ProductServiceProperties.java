package com.pruebatecnica.order_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "product.service")
public class ProductServiceProperties {
    private String url = "http://localhost:8081"; // default value

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}