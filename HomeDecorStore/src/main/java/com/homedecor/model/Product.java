package com.homedecor.model;

import java.math.BigDecimal;

/**
 * Represents a product sold in the Home Decor store.
 *
 * We use BigDecimal for prices, NOT double or float.
 * This is the standard in any commerce application because
 * floating-point arithmetic is imprecise — e.g., 0.1 + 0.2 in
 * double gives 0.30000000000000004, which is wrong for a price.
 * BigDecimal is exact.
 *
 * Each Product belongs to a Category (composition relationship).
 */
public class Product {

    // ── Fields ───────────────────────────────────────────────────────────────
    private int        id;
    private String     name;
    private BigDecimal price;
    private int        stockQuantity;   // how many units are in the warehouse
    private Category   category;        // which category this product belongs to
    private String     description;     // optional longer description
    private boolean    available;       // whether the product is listed for sale

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * Creates a new Product.
     * By default, a product is 'available' when created.
     *
     * @param id            unique identifier
     * @param name          product display name
     * @param price         unit price (use BigDecimal for money!)
     * @param stockQuantity how many units are in stock
     * @param category      the Category this product belongs to
     */
    public Product(int id, String name, BigDecimal price, int stockQuantity, Category category) {
        this.id            = id;
        this.name          = name;
        this.price         = price;
        this.stockQuantity = stockQuantity;
        this.category      = category;
        this.available     = true;   // default: product is active
    }

    /**
     * No-argument constructor needed for JSON deserialization.
     */
    public Product() {
        this.available = true;
    }

    /**
     * Sets the product ID, used by JSON deserialization.
     */
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // ── Business Logic Methods ────────────────────────────────────────────────

    /**
     * Reduces stock by the given quantity.
     * Called when an order is placed.
     *
     * @param quantity how many units to deduct
     * @throws IllegalArgumentException if quantity is <= 0 or exceeds stock
     */
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reduce must be positive.");
        }
        if (quantity > this.stockQuantity) {
            throw new IllegalArgumentException(
                "Cannot reduce stock by " + quantity + "; only " + stockQuantity + " available."
            );
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Adds stock (used when new inventory arrives).
     *
     * @param quantity how many new units to add
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive.");
        }
        this.stockQuantity += quantity;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int        getId()            { return id; }
    public String     getName()          { return name; }
    public BigDecimal getPrice()         { return price; }
    public int        getStockQuantity() { return stockQuantity; }
    public Category   getCategory()      { return category; }
    public String     getDescription()   { return description; }
    public boolean    isAvailable()      { return available; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setName(String name)               { this.name        = name; }
    public void setPrice(BigDecimal price)         { this.price       = price; }
    public void setDescription(String description) { this.description = description; }
    public void setAvailable(boolean available)    { this.available   = available; }
    // Note: stockQuantity is changed via reduceStock()/addStock(), not a plain setter,
    // to enforce business rules (can't go negative, etc.)

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Product{id="     + id
             + ", name='"        + name + "'"
             + ", price=$"       + price
             + ", stock="        + stockQuantity
             + ", category="     + (category != null ? category.getName() : "none")
             + "}";
    }
}
