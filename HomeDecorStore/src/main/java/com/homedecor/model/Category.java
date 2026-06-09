package com.homedecor.model;

/**
 * Represents a product category in the store.
 * Examples: "Living Room", "Bedroom", "Kitchen"
 *
 * This is a simple POJO (Plain Old Java Object) — it just holds data.
 * A POJO has:
 *   - private fields (so outside code can't modify them directly)
 *   - a constructor (to create the object with values)
 *   - getters / setters (controlled access to the fields)
 *   - a toString() (for easy printing/debugging)
 */
public class Category {

    // ── Fields ───────────────────────────────────────────────────────────────
    // 'private' means only code inside THIS class can read/write these directly.
    private int    id;
    private String name;
    private String description;

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * Creates a new Category.
     *
     * @param id          unique numeric identifier
     * @param name        display name shown to customers
     * @param description short blurb about this category
     */
    public Category(int id, String name, String description) {
        this.id          = id;       // 'this.id' refers to the field; 'id' is the parameter
        this.name        = name;
        this.description = description;
    }

    /**
     * No-argument constructor needed for frameworks that require a default constructor.
     */
    public Category() {
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    // Getters let other classes READ the private fields safely.

    /** @return the numeric ID of this category */
    public int getId() {
        return id;
    }

    /** @return the display name of this category */
    public String getName() {
        return name;
    }

    /** @return the short description of this category */
    public String getDescription() {
        return description;
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    // Setters let other classes CHANGE the private fields safely.

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ── toString ─────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable summary.
     * Without this, Java would print something like
     * "com.homedecor.model.Category@1b6d3586" which is meaningless.
     */
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
