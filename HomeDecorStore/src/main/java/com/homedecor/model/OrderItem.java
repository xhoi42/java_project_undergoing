package com.homedecor.model;

import java.math.BigDecimal;

/**
 * Represents a single line in an order: "2x Boho Wool Rug @ $149.99 each".
 *
 * An Order contains multiple OrderItems (composition).
 * This mirrors real e-commerce: one checkout can have many different products.
 *
 * Key concept: we SNAPSHOT the price at the time of the order.
 * If the product price changes later in the database, this order's
 * price should not change — hence 'unitPrice' is stored separately.
 */
public class OrderItem {

    // ── Fields ───────────────────────────────────────────────────────────────
    private Product    product;
    private int        quantity;
    private BigDecimal unitPrice;   // price at the time this item was ordered (snapshot!)

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * Creates an OrderItem.
     * Automatically captures the product's current price as the unit price.
     *
     * @param product  the product being ordered
     * @param quantity how many units of this product
     */
    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = product.getPrice();   // snapshot the price now
    }

    // ── Business Logic ────────────────────────────────────────────────────────

    /**
     * Calculates the subtotal for this line item.
     * e.g. quantity=2, unitPrice=149.99 → subtotal=299.98
     *
     * BigDecimal.multiply() is used (not *) because these are monetary values.
     *
     * @return quantity x unitPrice
     */
    public BigDecimal getSubtotal() {
        // BigDecimal.valueOf(quantity) converts the int to BigDecimal
        // so we can multiply two BigDecimals together
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Product    getProduct()   { return product; }
    public int        getQuantity()  { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "OrderItem{"
             + quantity + "x '" + product.getName() + "'"
             + " @ $" + unitPrice
             + " = $" + getSubtotal()
             + "}";
    }
}
