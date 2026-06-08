package com.homedecor.exception;

/**
 * Thrown when a requested product does not exist in the system.
 *
 * This is an UNCHECKED exception (extends RuntimeException).
 * Unchecked exceptions do NOT need to be declared in method signatures
 * with 'throws' — the caller can choose to catch them or let them bubble up.
 *
 * Use unchecked for programming errors or situations that "shouldn't happen"
 * in normal flow (like searching for an ID that doesn't exist).
 */
public class ProductNotFoundException extends RuntimeException {

    // Stores the ID that wasn't found, so callers can include it in error messages
    private final int productId;

    /**
     * Constructor for when you know the missing product's ID.
     */
    public ProductNotFoundException(int productId) {
        // super() calls the parent class (RuntimeException) constructor with a message
        super("Product with ID " + productId + " was not found.");
        this.productId = productId;
    }

    /**
     * Constructor for a custom message (used when you don't have the ID).
     */
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = -1;  // -1 signals "unknown"
    }

    /** @return the ID that was searched for, or -1 if unknown */
    public int getProductId() {
        return productId;
    }
}
