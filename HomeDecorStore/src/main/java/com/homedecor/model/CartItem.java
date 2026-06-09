package com.homedecor.model;

import java.math.BigDecimal;

/**
 * Represents one product sitting in a customer's shopping cart.
 *
 * CartItem is TEMPORARY — it lives only while the customer is browsing.
 * When the customer checks out, CartItems are converted into OrderItems
 * and placed into a real Order. After checkout the cart is cleared.
 *
 * CartItem is similar to OrderItem but:
 *   - It does NOT snapshot the price (it always shows the live product price)
 *   - It can be updated (change quantity) or removed before checkout
 */
public class CartItem {

    // ── Fields ───────────────────────────────────────────────────────────────
    private Product product;
    private int     quantity;

    // ── Constructor ──────────────────────────────────────────────────────────
    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null in a CartItem.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Cart item quantity must be at least 1.");
        }
        this.product  = product;
        this.quantity = quantity;
    }

    // ── Business Logic ────────────────────────────────────────────────────────

    /**
     * Returns the current line subtotal.
     * Uses the LIVE product price (no snapshot), because the customer
     * hasn't paid yet — prices can still change.
     */
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /** Increases the quantity in the cart. */
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be positive.");
        }
        this.quantity += amount;
    }

    /** Decreases the quantity. Quantity must not fall below 1. */
    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be positive.");
        }
        if (quantity - amount < 1) {
            throw new IllegalArgumentException("Quantity cannot drop below 1. Remove the item instead.");
        }
        this.quantity -= amount;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Product getProduct()  { return product; }
    public int     getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.quantity = quantity;
    }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "CartItem{"
             + quantity + "x '" + product.getName() + "'"
             + " (live price: $" + product.getPrice() + ")"
             + " subtotal=$"     + getSubtotal()
             + "}";
    }
}
