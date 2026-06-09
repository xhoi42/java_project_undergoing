package com.homedecor.exception;

/**
 * Thrown when a customer tries to order more units than are available in stock.
 *
 * Also UNCHECKED (extends RuntimeException).
 * This is a business-rule violation — something a user might realistically trigger —
 * but it doesn't need to be declared on every method signature.
 */
public class OutOfStockException extends RuntimeException {

    private final String productName;
    private final int    requested;
    private final int    available;

    /**
     * Full constructor: captures what was requested vs. what's available.
     *
     * @param productName name of the product that ran out
     * @param requested   how many units the customer wanted
     * @param available   how many units are actually in stock
     */
    public OutOfStockException(String productName, int requested, int available) {
        super("'" + productName + "' is out of stock. Requested: " + requested
              + ", available: " + available + ".");
        this.productName = productName;
        this.requested   = requested;
        this.available   = available;
    }

    /**
     * Simple constructor for a plain message.
     */
    public OutOfStockException(String message) {
        super(message);
        this.productName = "unknown";
        this.requested   = -1;
        this.available   = -1;
    }

    public String getProductName() { return productName; }
    public int    getRequested()   { return requested; }
    public int    getAvailable()   { return available; }
}
