package org.springframework.http;

public class ResponseEntity<T> {

    public static <T> ResponseEntity<T> ok(T body) { return new ResponseEntity<>(); }

    public static BodyBuilder status(HttpStatus status) { return new BodyBuilder(); }

    public static BodyBuilder badRequest() { return new BodyBuilder(); }

    public static BodyBuilder notFound() { return new BodyBuilder(); }

    public static BodyBuilder noContent() { return new BodyBuilder(); }

    public static class BodyBuilder {
        public <T> ResponseEntity<T> body(T body) { return new ResponseEntity<>(); }
        public <T> ResponseEntity<T> build() { return new ResponseEntity<>(); }
    }
}
