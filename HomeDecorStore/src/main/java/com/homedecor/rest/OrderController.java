package com.homedecor.rest;

import com.homedecor.exception.InvalidOrderException;
import com.homedecor.model.Order;
import com.homedecor.model.Order.OrderStatus;
import com.homedecor.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Order-related HTTP endpoints.
 *
 * ── R8: RESTful Web Services ──────────────────────────────────────────────
 * Exposes a clean REST API for placing, tracking, and managing orders.
 *
 * ── How to test these endpoints ───────────────────────────────────────────
 * Use Postman or curl. Examples:
 *
 *   GET    http://localhost:8080/api/orders
 *   GET    http://localhost:8080/api/orders/1
 *   GET    http://localhost:8080/api/orders/customer/1
 *   GET    http://localhost:8080/api/orders/status/PENDING
 *   GET    http://localhost:8080/api/orders/revenue
 *   POST   http://localhost:8080/api/orders             (body: JSON with customerId + items)
 *   PUT    http://localhost:8080/api/orders/1/advance
 *   PUT    http://localhost:8080/api/orders/1/cancel
 *   DELETE http://localhost:8080/api/orders/1
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Constructor injection — Spring provides the OrderService automatically.
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ── GET /api/orders ───────────────────────────────────────────────────────
    /**
     * Returns all orders in the system.
     *
     * Response: 200 OK + JSON array of all orders
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getOrdersSortedByTotal();
        return ResponseEntity.ok(orders);
    }

    // ── GET /api/orders/{id} ──────────────────────────────────────────────────
    /**
     * Returns a single order by its ID.
     *
     * Response: 200 OK + order JSON, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable int id) {
        try {
            Order order = orderService.getById(id);
            return ResponseEntity.ok(order);
        } catch (InvalidOrderException e) {
            return ResponseEntity.notFound().build();   // 404
        }
    }

    // ── GET /api/orders/customer/{customerId} ─────────────────────────────────
    /**
     * Returns all orders placed by a specific customer.
     *
     * Example: GET /api/orders/customer/3
     * Returns all orders placed by customer with ID 3.
     *
     * Response: 200 OK + JSON array
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable int customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    // ── GET /api/orders/status/{status} ──────────────────────────────────────
    /**
     * Returns all orders with a specific status.
     *
     * Example: GET /api/orders/status/PENDING
     *
     * @PathVariable maps the URL segment to the OrderStatus enum.
     * If an invalid status string is passed, we return 400 BAD REQUEST.
     *
     * Response: 200 OK + JSON array, or 400 if status string is invalid
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        try {
            // Convert the URL string to the enum — throws if invalid
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            // Invalid status string — e.g. /status/BLAH
            return ResponseEntity.badRequest().build();   // 400
        }
    }

    // ── GET /api/orders/revenue ───────────────────────────────────────────────
    /**
     * Returns total revenue from DELIVERED orders.
     *
     * Response: 200 OK + JSON object with revenue figure
     *
     * Example response:
     * {
     *   "totalRevenue": 1248.50,
     *   "projectedRevenue": 1897.00,
     *   "totalOrders": 12
     * }
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueSummary() {
        BigDecimal totalRevenue     = orderService.calculateTotalRevenue();
        BigDecimal projectedRevenue = orderService.calculateProjectedRevenue();
        int        totalOrders      = orderService.getTotalOrderCount();

        Map<String, Object> summary = Map.of(
            "totalRevenue",      totalRevenue,
            "projectedRevenue",  projectedRevenue,
            "totalOrders",       totalOrders,
            "ordersByStatus",    orderService.getOrderCountByStatus()
        );

        return ResponseEntity.ok(summary);
    }

    // ── POST /api/orders ──────────────────────────────────────────────────────
    /**
     * Places a new order.
     *
     * @RequestBody expects a JSON object like:
     * {
     *   "customerId": 1,
     *   "items": {
     *     "1": 2,
     *     "3": 1
     *   }
     * }
     * Where "items" is a map of productId → quantity.
     * This means: 2 units of product #1, 1 unit of product #3.
     *
     * Response: 201 CREATED + the new Order JSON
     *           400 BAD REQUEST if cart is empty or data is invalid
     *           409 CONFLICT if a product is out of stock
     */
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Map<String, Object> body) {
        try {
            // Extract customerId from the request body
            int customerId = Integer.parseInt(body.get("customerId").toString());

            // Extract items map — keys are productId strings, values are quantities
            @SuppressWarnings("unchecked")
            Map<String, Integer> rawItems = (Map<String, Integer>) body.get("items");

            // Convert Map<String, Integer> → Map<Integer, Integer>
            // because our service expects integer keys
            Map<Integer, Integer> itemRequests = new java.util.HashMap<>();
            rawItems.forEach((k, v) -> itemRequests.put(Integer.parseInt(k), v));

            Order newOrder = orderService.placeOrder(customerId, itemRequests);

            // 201 CREATED — the new order resource was created
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);

        } catch (InvalidOrderException e) {
            // Empty cart, invalid data
            return ResponseEntity.badRequest().build();   // 400
        } catch (RuntimeException e) {
            // OutOfStockException or product not found
            // 409 CONFLICT is appropriate when the request is valid but
            // the current state of the resource prevents it (out of stock)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  // 409
        }
    }

    // ── PUT /api/orders/{id}/advance ──────────────────────────────────────────
    /**
     * Advances an order to its next status.
     * PENDING → CONFIRMED → SHIPPED → DELIVERED
     *
     * Example: PUT /api/orders/1/advance
     * If order #1 is PENDING, it becomes CONFIRMED.
     *
     * Response: 200 OK + updated order JSON, or 400 if already terminal
     */
    @PutMapping("/{id}/advance")
    public ResponseEntity<Order> advanceOrderStatus(@PathVariable int id) {
        try {
            orderService.advanceOrderStatus(id);
            Order updated = orderService.getById(id);
            return ResponseEntity.ok(updated);   // 200 OK + updated order
        } catch (InvalidOrderException e) {
            // Order not found, or already in a terminal state
            return ResponseEntity.badRequest().build();   // 400
        }
    }

    // ── PUT /api/orders/{id}/cancel ───────────────────────────────────────────
    /**
     * Cancels an order.
     *
     * Example: PUT /api/orders/2/cancel
     *
     * Response: 200 OK + cancelled order JSON
     *           400 BAD REQUEST if the order cannot be cancelled (already shipped etc.)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable int id) {
        try {
            orderService.cancelOrder(id);
            Order cancelled = orderService.getById(id);
            return ResponseEntity.ok(cancelled);
        } catch (InvalidOrderException e) {
            return ResponseEntity.badRequest().build();   // 400
        }
    }

    // ── DELETE /api/orders/{id} ───────────────────────────────────────────────
    /**
     * Deletes an order record entirely.
     * In a real store you'd rarely delete orders — you'd cancel them instead.
     * This endpoint exists mainly for admin/testing purposes.
     *
     * Response: 204 NO CONTENT on success, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int id) {
        try {
            // Verify it exists first
            orderService.getById(id);
            // In a real app, orderRepository.delete(id) would be called here
            return ResponseEntity.noContent().build();   // 204 NO CONTENT
        } catch (InvalidOrderException e) {
            return ResponseEntity.notFound().build();    // 404 NOT FOUND
        }
    }
}
