package com.homedecor.exception;

/**
 * Thrown when an order operation is attempted that isn't allowed.
 * Examples:
 *   - Adding an item to a SHIPPED order
 *   - Cancelling a DELIVERED order
 *   - Submitting an empty order
 *
 * This is a CHECKED exception (extends Exception, NOT RuntimeException).
 * Checked exceptions MUST be either:
 *   a) caught with try/catch, OR
 *   b) declared with 'throws' in the method signature
 *
 * Use checked exceptions for conditions the caller is expected to handle —
 * like "this order state transition is invalid; please show the user an error".
 *
 * Compare to OutOfStockException (unchecked) — the difference is a design choice.
 * InvalidOrderException is checked because any method that touches order status
 * transitions must acknowledge it can fail.
 */
public class InvalidOrderException extends Exception {

    private final int orderId;

    /**
     * Constructor with order ID for context.
     */
    public InvalidOrderException(int orderId, String message) {
        super("Order #" + orderId + ": " + message);
        this.orderId = orderId;
    }

    /**
     * Constructor with just a message (when order ID is unknown).
     */
    public InvalidOrderException(String message) {
        super(message);
        this.orderId = -1;
    }

    public int getOrderId() {
        return orderId;
    }
}
