package com.homedecor.rest;

import com.homedecor.exception.ProductNotFoundException;
import com.homedecor.model.Product;
import com.homedecor.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Product-related HTTP endpoints.
 *
 * ── R8: RESTful Web Services ──────────────────────────────────────────────
 * This class exposes HTTP endpoints that any client (browser, mobile app,
 * Postman) can call. Spring Boot handles all the HTTP plumbing for us.
 *
 * ── Key Annotations ───────────────────────────────────────────────────────
 * @RestController   → marks this class as a REST controller.
 *                     Every method automatically returns JSON (not HTML).
 *
 * @RequestMapping   → sets the base URL path for all methods in this class.
 *                     All product endpoints start with /api/products
 *
 * @GetMapping       → handles HTTP GET requests  (read data)
 * @PostMapping      → handles HTTP POST requests  (create data)
 * @PutMapping       → handles HTTP PUT requests   (update data)
 * @DeleteMapping    → handles HTTP DELETE requests (delete data)
 *
 * @PathVariable     → pulls a value out of the URL, e.g. /products/5 → id=5
 * @RequestBody      → reads the JSON body of the request and maps it to a Java object
 * @RequestParam     → reads a query parameter, e.g. /products?maxPrice=50 → maxPrice=50
 *
 * ── HTTP Status Codes used ────────────────────────────────────────────────
 * 200 OK            → success, returning data
 * 201 CREATED       → success, new resource was created
 * 204 NO CONTENT    → success, nothing to return (e.g. after delete)
 * 400 BAD REQUEST   → client sent invalid data
 * 404 NOT FOUND     → the resource doesn't exist
 * 500 SERVER ERROR  → unexpected error on the server
 *
 * ── How to test these endpoints ───────────────────────────────────────────
 * Use Postman or curl. Examples:
 *
 *   GET    http://localhost:8080/api/products
 *   GET    http://localhost:8080/api/products/1
 *   GET    http://localhost:8080/api/products/search?keyword=rug
 *   GET    http://localhost:8080/api/products/filter?maxPrice=50
 *   POST   http://localhost:8080/api/products          (body: JSON product)
 *   PUT    http://localhost:8080/api/products/1/price  (body: {"price": 99.99})
 *   DELETE http://localhost:8080/api/products/1
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    // The service layer handles all business logic — the controller just
    // receives the request, calls the service, and returns the response.
    private final ProductService productService;

    /**
     * Constructor injection — Spring automatically provides the ProductService bean.
     * This is the recommended way to inject dependencies in Spring Boot.
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ── GET /api/products ─────────────────────────────────────────────────────
    /**
     * Returns all products in the catalog.
     *
     * Response: 200 OK + JSON array of all products
     *
     * Example response:
     * [
     *   {"id":1, "name":"Boho Wool Rug", "price":149.99, ...},
     *   {"id":2, "name":"Arch Floor Lamp", "price":89.50, ...}
     * ]
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);   // 200 OK + body
    }

    // ── GET /api/products/{id} ────────────────────────────────────────────────
    /**
     * Returns a single product by its ID.
     *
     * @PathVariable extracts the {id} segment from the URL.
     * e.g. GET /api/products/3 → id = 3
     *
     * Response: 200 OK + product JSON, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        try {
            Product product = productService.getById(id);
            return ResponseEntity.ok(product);   // 200 OK
        } catch (ProductNotFoundException e) {
            // Return 404 with no body — the product doesn't exist
            return ResponseEntity.notFound().build();   // 404 NOT FOUND
        }
    }

    // ── GET /api/products/search?keyword=rug ─────────────────────────────────
    /**
     * Searches products by name keyword.
     *
     * @RequestParam reads the ?keyword=... part of the URL.
     * 'required = false' means the parameter is optional.
     * 'defaultValue = ""' means if not provided, search with empty string (returns all).
     *
     * Response: 200 OK + JSON array of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword) {

        List<Product> results = productService.searchByName(keyword);
        return ResponseEntity.ok(results);
    }

    // ── GET /api/products/filter?maxPrice=100 ────────────────────────────────
    /**
     * Filters products by maximum price.
     *
     * Response: 200 OK + JSON array of products at or below maxPrice
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterByPrice(
            @RequestParam BigDecimal maxPrice) {

        List<Product> results = productService.getByMaxPrice(maxPrice);
        return ResponseEntity.ok(results);
    }

    // ── GET /api/products/sorted ──────────────────────────────────────────────
    /**
     * Returns all products sorted by price ascending (cheapest first).
     *
     * Response: 200 OK + sorted JSON array
     */
    @GetMapping("/sorted")
    public ResponseEntity<List<Product>> getProductsSorted() {
        List<Product> sorted = productService.getSortedByPriceAsc();
        return ResponseEntity.ok(sorted);
    }

    // ── GET /api/products/stats ───────────────────────────────────────────────
    /**
     * Returns catalog statistics: total count, average price, count by category.
     *
     * Response: 200 OK + JSON object with stats
     *
     * Example response:
     * {
     *   "totalProducts": 4,
     *   "averagePrice": 70.37,
     *   "countByCategory": {"Living Room": 2, "Bedroom": 1, "Kitchen": 1}
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCatalogStats() {
        Map<String, Object> stats = Map.of(
            "totalProducts",   productService.getTotalProductCount(),
            "averagePrice",    productService.getAveragePrice().orElse(0.0),
            "countByCategory", productService.countByCategory()
        );
        return ResponseEntity.ok(stats);
    }

    // ── POST /api/products ────────────────────────────────────────────────────
    /**
     * Creates a new product.
     *
     * @RequestBody reads the JSON body and maps it to a Product object.
     * Spring uses Jackson (a JSON library) to do this automatically.
     *
     * Example request body:
     * {
     *   "name": "Rattan Side Table",
     *   "price": 79.99,
     *   "stockQuantity": 15,
     *   "category": {"id": 1}
     * }
     *
     * Response: 201 CREATED + the saved product JSON
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            productService.addProduct(product);
            // 201 CREATED — resource was successfully created
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (IllegalArgumentException e) {
            // 400 BAD REQUEST — e.g. duplicate ID
            return ResponseEntity.badRequest().build();
        }
    }

    // ── PUT /api/products/{id}/price ──────────────────────────────────────────
    /**
     * Updates a product's price and description.
     *
     * @RequestBody reads a JSON object with "price" and "description" fields.
     *
     * Example request body:
     * {
     *   "price": 129.99,
     *   "description": "Updated boho rug with new colorway"
     * }
     *
     * Response: 200 OK + updated product, or 404 if not found
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<Product> updateProductPrice(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {

        try {
            BigDecimal newPrice   = new BigDecimal(body.get("price").toString());
            String     newDesc    = body.getOrDefault("description", "").toString();

            productService.updateProduct(id, newPrice, newDesc);
            Product updated = productService.getById(id);
            return ResponseEntity.ok(updated);   // 200 OK + updated product

        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();   // 404
        } catch (Exception e) {
            return ResponseEntity.badRequest().build(); // 400
        }
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────────────────
    /**
     * Removes a product from the catalog.
     *
     * Response: 204 NO CONTENT on success, 404 if product not found.
     * 204 means "it worked, but there's nothing to return".
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        try {
            productService.removeProduct(id);
            return ResponseEntity.noContent().build();   // 204 NO CONTENT
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();    // 404 NOT FOUND
        }
    }
}
