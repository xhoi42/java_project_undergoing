package com.homedecor.pattern;

import com.homedecor.model.CartItem;
import com.homedecor.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the BUILDER design pattern for constructing a shopping cart.
 *
 * ── R9: Design Pattern — Builder ─────────────────────────────────────────
 *
 * PROBLEM it solves:
 * A shopping cart has many optional properties:
 *   - items (multiple)
 *   - discount percentage
 *   - promo code
 *   - gift wrapping flag
 *   - delivery notes
 *
 * Without Builder, you'd need one giant constructor like:
 *   new Cart(customer, items, discount, promoCode, giftWrap, notes)
 *
 * This is terrible because:
 *   - Hard to read — you can't tell which argument is which at the call site
 *   - Hard to use — you must pass null for every optional field you don't need
 *   - Brittle — adding a new field means changing every constructor call
 *
 * WITH Builder:
 *   Cart cart = new CartBuilder()
 *       .addItem(rug, 1)
 *       .addItem(vase, 2)
 *       .applyDiscount(10)
 *       .withPromoCode("WELCOME10")
 *       .withGiftWrap(true)
 *       .build();
 *
 * This is:
 *   - Readable — each method name says exactly what it does
 *   - Flexible — only set the fields you need, skip the rest
 *   - Safe    — build() validates everything before creating the object
 *
 * ── Structure ─────────────────────────────────────────────────────────────
 * CartBuilder        → the builder class (accumulates configuration)
 * CartBuilder.Cart   → the final product (immutable, created by build())
 *
 * The Cart inner class is immutable — once built, it cannot be changed.
 * This is intentional: the Builder pattern separates construction from use.
 */
public class CartBuilder {

    // ── Builder fields (accumulated step by step) ─────────────────────────────
    private final List<CartItem> items          = new ArrayList<>();
    private       BigDecimal     discountPercent = BigDecimal.ZERO;
    private       String         promoCode       = null;
    private       boolean        giftWrap        = false;
    private       String         deliveryNotes   = "";

    // ── Builder Methods ───────────────────────────────────────────────────────
    // Each method returns 'this' (the builder itself) so calls can be chained:
    // builder.addItem(...).addItem(...).applyDiscount(...).build()

    /**
     * Adds a product with the specified quantity to the cart.
     *
     * @param product  the product to add
     * @param quantity how many units
     * @return this builder (for chaining)
     */
    public CartBuilder addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Cannot add a null product to the cart.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        // If this product is already in the cart, increase its quantity
        // R3: lambda in stream search
        CartItem existing = items.stream()
                                 .filter(ci -> ci.getProduct().getId() == product.getId())
                                 .findFirst()
                                 .orElse(null);

        if (existing != null) {
            existing.increaseQuantity(quantity);
        } else {
            items.add(new CartItem(product, quantity));
        }

        return this;   // return 'this' to allow method chaining
    }

    /**
     * Removes a product from the cart entirely.
     *
     * @param productId the ID of the product to remove
     * @return this builder (for chaining)
     */
    public CartBuilder removeItem(int productId) {
        // R3: lambda in removeIf
        items.removeIf(ci -> ci.getProduct().getId() == productId);
        return this;
    }

    /**
     * Applies a percentage discount to the entire cart.
     *
     * @param percent discount percentage, e.g. 10 means 10% off
     * @return this builder (for chaining)
     * @throws IllegalArgumentException if percent is not between 0 and 100
     */
    public CartBuilder applyDiscount(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100.");
        }
        this.discountPercent = BigDecimal.valueOf(percent);
        return this;
    }

    /**
     * Attaches a promotional code to the cart.
     *
     * @param code the promo code string
     * @return this builder (for chaining)
     */
    public CartBuilder withPromoCode(String code) {
        this.promoCode = code;
        return this;
    }

    /**
     * Sets whether this order should be gift-wrapped.
     *
     * @param giftWrap true to add gift wrapping
     * @return this builder (for chaining)
     */
    public CartBuilder withGiftWrap(boolean giftWrap) {
        this.giftWrap = giftWrap;
        return this;
    }

    /**
     * Adds a delivery note (e.g. "Leave at front door").
     *
     * @param notes the delivery instructions
     * @return this builder (for chaining)
     */
    public CartBuilder withDeliveryNotes(String notes) {
        this.deliveryNotes = notes;
        return this;
    }

    /**
     * Validates everything and constructs the final immutable Cart object.
     *
     * Validation happens HERE (in build()) rather than scattered across
     * multiple setters. This is one of the key advantages of the Builder pattern —
     * you can enforce complex cross-field rules in one place.
     *
     * @return a fully validated, immutable Cart
     * @throws IllegalStateException if the cart is empty
     */
    public Cart build() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot build a cart with no items.");
        }
        // Pass all accumulated values to the Cart constructor
        return new Cart(items, discountPercent, promoCode, giftWrap, deliveryNotes);
    }

    // ── Cart — the final immutable product ───────────────────────────────────
    /**
     * Represents a fully built, ready-to-checkout shopping cart.
     *
     * All fields are final (immutable) — the cart cannot be changed after build().
     * Only CartBuilder can create a Cart (the constructor is package-private).
     */
    public static class Cart {

        private final List<CartItem> items;
        private final BigDecimal     discountPercent;
        private final String         promoCode;
        private final boolean        giftWrap;
        private final String         deliveryNotes;

        // Package-private constructor — only CartBuilder can call this
        Cart(List<CartItem> items, BigDecimal discountPercent,
             String promoCode, boolean giftWrap, String deliveryNotes) {

            // Store an unmodifiable copy so nobody can mutate the list later
            this.items           = Collections.unmodifiableList(new ArrayList<>(items));
            this.discountPercent = discountPercent;
            this.promoCode       = promoCode;
            this.giftWrap        = giftWrap;
            this.deliveryNotes   = deliveryNotes;
        }

        /**
         * Calculates the raw subtotal before any discount.
         * R3: method reference   R4: map + reduce stream
         */
        public BigDecimal getSubtotal() {
            return items.stream()
                        .map(CartItem::getSubtotal)          // R3: method reference
                        .reduce(BigDecimal.ZERO, BigDecimal::add);  // R4: reduce
        }

        /**
         * Calculates the discount amount in currency.
         * e.g. subtotal=$200, discount=10% → discountAmount=$20
         */
        public BigDecimal getDiscountAmount() {
            if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            // subtotal * (discountPercent / 100)
            return getSubtotal()
                       .multiply(discountPercent)
                       .divide(BigDecimal.valueOf(100));
        }

        /**
         * Calculates the final total after discount.
         * subtotal - discountAmount
         */
        public BigDecimal getTotal() {
            return getSubtotal().subtract(getDiscountAmount());
        }

        // ── Getters ───────────────────────────────────────────────────────────
        public List<CartItem> getItems()           { return items; }
        public BigDecimal     getDiscountPercent() { return discountPercent; }
        public String         getPromoCode()       { return promoCode; }
        public boolean        isGiftWrap()         { return giftWrap; }
        public String         getDeliveryNotes()   { return deliveryNotes; }
        public int            getItemCount()       { return items.size(); }

        @Override
        public String toString() {
            return "Cart{"
                 + "items="            + items.size()
                 + ", subtotal=$"      + getSubtotal()
                 + ", discount="       + discountPercent + "%"
                 + ", discountAmount=$"+ getDiscountAmount()
                 + ", total=$"         + getTotal()
                 + ", giftWrap="       + giftWrap
                 + (promoCode != null ? ", promoCode='" + promoCode + "'" : "")
                 + "}";
        }
    }
}
