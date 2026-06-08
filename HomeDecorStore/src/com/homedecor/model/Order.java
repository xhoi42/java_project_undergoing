package com.homedecor.model;

import com.homedecor.exception.InvalidOrderException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order — a collection of OrderItems placed at one time.
 *
 * Lifecycle of an order:
 *   PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 *                        \-> CANCELLED
 *
 * We use an enum (OrderStatus) to represent the status.
 * An enum is a special type that limits a variable to a fixed set of values.
 * This is safer than using plain Strings like "pending", "PENDING", "Pending"
 * which could all mean the same thing but wouldn't be equal in comparisons.
 */
public class Order {

    // ── Inner Enum ────────────────────────────────────────────────────────────
    /**
     * The set of all valid order statuses.
     * Declaring this inside Order keeps everything tidy.
     */
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    // ── Fields ───────────────────────────────────────────────────────────────
    private int             id;
    private Customer        customer;
    private List<OrderItem> items;        // the line items in this order
    private OrderStatus     status;
    private LocalDateTime   createdAt;    // when the order was placed

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * Creates a new Order for a customer.
     * Status starts as PENDING until payment is confirmed.
     */
    public Order(int id, Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("An order must have a customer.");
        }
        this.id        = id;
        this.customer  = customer;
        this.items     = new ArrayList<>();           // empty until items are added
        this.status    = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();         // capture current timestamp
    }

    // ── Business Logic ────────────────────────────────────────────────────────

    /**
     * Adds a product line to this order.
     * An order cannot be modified after it has been confirmed.
     *
     * @param item the OrderItem to add
     * @throws InvalidOrderException if the order is no longer editable
     */
    public void addItem(OrderItem item) throws InvalidOrderException {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderException(
                "Cannot modify order #" + id + " — it is already " + status + "."
            );
        }
        if (item == null) {
            throw new InvalidOrderException("Cannot add a null item to an order.");
        }
        items.add(item);
    }

    /**
     * Calculates the grand total of all line items.
     *
     * We use BigDecimal.ZERO as the starting value and add each subtotal.
     * This is safer than starting from 0.0 (double) which can accumulate
     * floating-point rounding errors.
     *
     * @return sum of all OrderItem subtotals
     */
    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Advances the order to the next logical status.
     * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
     *
     * @throws InvalidOrderException if the order is already in a terminal state
     */
    public void advanceStatus() throws InvalidOrderException {
        switch (status) {
            case PENDING:   status = OrderStatus.CONFIRMED; break;
            case CONFIRMED: status = OrderStatus.SHIPPED;   break;
            case SHIPPED:   status = OrderStatus.DELIVERED; break;
            case DELIVERED:
            case CANCELLED:
                throw new InvalidOrderException(
                    "Order #" + id + " is already in a terminal state: " + status
                );
        }
    }

    /**
     * Cancels the order, but only if it hasn't been shipped yet.
     *
     * @throws InvalidOrderException if the order cannot be cancelled
     */
    public void cancel() throws InvalidOrderException {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new InvalidOrderException(
                "Order #" + id + " cannot be cancelled — it has already been " + status + "."
            );
        }
        status = OrderStatus.CANCELLED;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int             getId()        { return id; }
    public Customer        getCustomer()  { return customer; }
    public List<OrderItem> getItems()     { return items; }
    public OrderStatus     getStatus()    { return status; }
    public LocalDateTime   getCreatedAt() { return createdAt; }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Order{id="     + id
             + ", customer='"  + customer.getFullName() + "'"
             + ", items="      + items.size()
             + ", status="     + status
             + ", total=$"     + calculateTotal()
             + ", createdAt="  + createdAt
             + "}";
    }
}
