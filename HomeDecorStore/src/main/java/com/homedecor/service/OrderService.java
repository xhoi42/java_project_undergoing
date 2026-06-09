package com.homedecor.service;

import com.homedecor.exception.InvalidOrderException;
import com.homedecor.exception.OutOfStockException;
import com.homedecor.model.*;
import com.homedecor.model.Order.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for Order-related business logic.
 *
 * This is the most important service — it coordinates between
 * ProductService (stock), CustomerService (customer lookup),
 * and the Order/OrderItem model classes.
 *
 * ── R1: Collections ──────────────────────────────────────────────────────
 * Uses HashMap<Integer, Order> for fast order lookup by ID.
 *
 * ── R3: Lambdas ──────────────────────────────────────────────────────────
 * Used in filtering, sorting, and total calculations.
 *
 * ── R4: Streams ──────────────────────────────────────────────────────────
 * Used in revenue calculation (reduce), order filtering, and reporting.
 */
@Service
public class OrderService {

    // ── R1: HashMap — key=orderId, value=Order ────────────────────────────────
    private final Map<Integer, Order> orders = new HashMap<>();

    private final ProductService  productService;
    private final CustomerService customerService;

    private int nextOrderId = 1;   // simple auto-increment ID counter

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * OrderService depends on ProductService and CustomerService.
     * We inject them through the constructor — this is called
     * "Constructor Injection" and is a best practice (easy to test).
     */
    public OrderService(ProductService productService, CustomerService customerService) {
        this.productService  = productService;
        this.customerService = customerService;
    }

    // ── Core Order Operations ─────────────────────────────────────────────────

    /**
     * Places a new order for a customer.
     *
     * Steps:
     *  1. Look up the customer
     *  2. Validate all items have enough stock
     *  3. Create the Order and OrderItems
     *  4. Reduce stock for each product
     *  5. Add the order to the customer's history
     *  6. Save the order
     *
     * @param customerId     who is ordering
     * @param itemRequests   Map of productId → quantity
     * @return the created Order
     * @throws InvalidOrderException if the cart is empty or order is invalid
     * @throws OutOfStockException   if any product doesn't have enough stock
     */
    public Order placeOrder(int customerId, Map<Integer, Integer> itemRequests)
            throws InvalidOrderException {

        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new InvalidOrderException("Cannot place an order with no items.");
        }

        // Step 1: look up customer
        Customer customer = customerService.getById(customerId);

        // Step 2: validate stock for ALL items before creating anything
        // R3: lambda in forEach   R4: forEach terminal operation
        itemRequests.forEach((productId, quantity) ->
            productService.validateStock(productId, quantity)
            // throws OutOfStockException automatically if not enough stock
        );

        // Step 3: create the Order
        Order order = new Order(nextOrderId++, customer);

        // Step 4 + 5: add items and reduce stock
        for (Map.Entry<Integer, Integer> entry : itemRequests.entrySet()) {
            int productId = entry.getKey();
            int quantity  = entry.getValue();

            Product product = productService.getById(productId);
            order.addItem(new OrderItem(product, quantity));  // may throw InvalidOrderException
            product.reduceStock(quantity);                    // deduct from inventory
        }

        // Step 6: persist and link to customer
        orders.put(order.getId(), order);
        customer.addOrder(order);

        return order;
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the order to look up
     * @return the Order
     * @throws InvalidOrderException if not found
     */
    public Order getById(int orderId) throws InvalidOrderException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new InvalidOrderException("Order #" + orderId + " not found.");
        }
        return order;
    }

    /**
     * Advances an order to its next status.
     * PENDING → CONFIRMED → SHIPPED → DELIVERED
     *
     * @param orderId the order to advance
     * @throws InvalidOrderException if already in a terminal state
     */
    public void advanceOrderStatus(int orderId) throws InvalidOrderException {
        Order order = getById(orderId);
        order.advanceStatus();
    }

    /**
     * Cancels an order.
     *
     * @param orderId the order to cancel
     * @throws InvalidOrderException if it cannot be cancelled
     */
    public void cancelOrder(int orderId) throws InvalidOrderException {
        Order order = getById(orderId);
        order.cancel();
    }

    // ── Query / Reporting Methods (R3 + R4) ───────────────────────────────────

    /**
     * Returns all orders for a specific customer.
     *
     * R3: lambda filter   R4: stream → filter → collect
     */
    public List<Order> getOrdersByCustomer(int customerId) {
        return orders.values()
                     .stream()
                     .filter(o -> o.getCustomer().getId() == customerId)  // R3: lambda
                     .collect(Collectors.toList());
    }

    /**
     * Returns all orders with a given status.
     *
     * @param status the status to filter by
     * @return list of matching orders
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orders.values()
                     .stream()
                     .filter(o -> o.getStatus() == status)   // R3: lambda
                     .collect(Collectors.toList());
    }

    /**
     * Returns all orders, sorted by total value (highest first).
     *
     * R3: Comparator lambda   R4: sorted terminal
     */
    public List<Order> getOrdersSortedByTotal() {
        return orders.values()
                     .stream()
                     .sorted((o1, o2) ->
                         o2.calculateTotal().compareTo(o1.calculateTotal())  // R3: lambda
                     )
                     .collect(Collectors.toList());
    }

    /**
     * Calculates total revenue from all DELIVERED orders.
     *
     * ── R4: reduce ───────────────────────────────────────────────────────────
     * reduce() is a terminal Stream operation that combines all elements
     * into a single result. Here we start from BigDecimal.ZERO and add
     * each order's total one by one.
     *
     * Think of it as: total = 0; for each order: total += order.total
     * But written as a functional pipeline.
     *
     * @return total revenue as BigDecimal
     */
    public BigDecimal calculateTotalRevenue() {
        return orders.values()
                     .stream()
                     .filter(o -> o.getStatus() == OrderStatus.DELIVERED)   // R3: lambda
                     // R4: map each order to its total value
                     .map(Order::calculateTotal)
                     // R4: reduce — combine all totals into one sum
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates total revenue from orders in any status.
     * Useful for projected/pending revenue reporting.
     */
    public BigDecimal calculateProjectedRevenue() {
        return orders.values()
                     .stream()
                     .filter(o -> o.getStatus() != OrderStatus.CANCELLED)  // R3: lambda
                     .map(Order::calculateTotal)
                     .reduce(BigDecimal.ZERO, BigDecimal::add);             // R4: reduce
    }

    /**
     * Returns a summary of how many orders exist per status.
     *
     * R4: groupingBy collector
     * Result example: {PENDING=3, CONFIRMED=1, DELIVERED=5}
     */
    public Map<OrderStatus, Long> getOrderCountByStatus() {
        return orders.values()
                     .stream()
                     .collect(Collectors.groupingBy(
                         Order::getStatus,     // R3: method reference classifier
                         Collectors.counting() // count per group
                     ));
    }

    /** @return total number of orders in the system */
    public int getTotalOrderCount() {
        return orders.size();
    }
}
