package org.springframework.web.servlet.config.annotation;

public class CorsRegistry {
    private String[] allowedOrigins = {"*"};
    private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE"};
    private String[] allowedHeaders = {"*"};
    private long maxAge = 3600;

    public CorsRegistry addMapping(String pathPattern) {
        return this;
    }

    public CorsRegistry allowedOrigins(String... origins) {
        this.allowedOrigins = origins;
        return this;
    }

    public CorsRegistry allowedMethods(String... methods) {
        this.allowedMethods = methods;
        return this;
    }

    public CorsRegistry allowedHeaders(String... headers) {
        this.allowedHeaders = headers;
        return this;
    }

    public CorsRegistry allowCredentials(boolean allow) {
        return this;
    }

    public CorsRegistry maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }
}
