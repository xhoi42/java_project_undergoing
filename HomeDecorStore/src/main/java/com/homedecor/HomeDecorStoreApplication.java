package com.homedecor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point.
 *
 * ── @SpringBootApplication ────────────────────────────────────────────────
 * This single annotation is actually THREE annotations combined:
 *
 *   @Configuration
 *     → Marks this class as a source of Spring "beans" (managed objects).
 *       A bean is any object whose lifecycle Spring controls — creates it,
 *       injects it where needed, destroys it when the app shuts down.
 *
 *   @EnableAutoConfiguration
 *     → Tells Spring Boot to automatically configure itself based on what's
 *       on the classpath. It sees spring-boot-starter-web → starts Tomcat.
 *       It sees spring-boot-starter-data-jpa → configures Hibernate.
 *       It sees mysql-connector-j → configures the DataSource.
 *       All of this happens without any XML config files.
 *
 *   @ComponentScan
 *     → Tells Spring to scan the 'com.homedecor' package (and all sub-packages)
 *       for classes annotated with @RestController, @Service, @Repository etc.
 *       and automatically register them as beans.
 *
 * ── How Spring Boot starts ────────────────────────────────────────────────
 * 1. SpringApplication.run() is called
 * 2. Spring scans all packages for annotated classes
 * 3. It creates beans for: ProductService, CustomerService, OrderService,
 *    ProductController, OrderController, all Repositories, etc.
 * 4. It injects dependencies (constructor injection we set up)
 * 5. It starts the embedded Tomcat server on port 8080
 * 6. Your REST endpoints are now live and accepting HTTP requests
 *
 * ── How to run ────────────────────────────────────────────────────────────
 * Option A (Maven):
 *   mvn spring-boot:run
 *
 * Option B (IntelliJ):
 *   Right-click this file → Run 'HomeDecorStoreApplication'
 *
 * Option C (JAR):
 *   mvn package
 *   java -jar target/home-decor-store-1.0.0.jar
 *
 * Then open: http://localhost:8080/api/products
 */
@SpringBootApplication
public class HomeDecorStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeDecorStoreApplication.class, args);
    }
}