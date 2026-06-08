package org.springframework.http;

public class HttpStatus {
    public static final HttpStatus CREATED = new HttpStatus("CREATED");
    public static final HttpStatus CONFLICT = new HttpStatus("CONFLICT");

    private final String name;

    public HttpStatus(String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}
