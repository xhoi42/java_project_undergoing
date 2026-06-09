package com.homedecor.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic in-memory store for any type of object.
 *
 * ══════════════════════════════════════════════════════
 *  R2 — GENERICS  (required for your project)
 * ══════════════════════════════════════════════════════
 *
 * The '<T>' after the class name is the TYPE PARAMETER.
 * It's a placeholder that gets filled in when you create the object:
 *
 *   DataStore<Product>  productStore  = new DataStore<>();
 *   DataStore<Customer> customerStore = new DataStore<>();
 *   DataStore<Order>    orderStore    = new DataStore<>();
 *
 * Without generics, you'd need a separate ProductList, CustomerList, etc.
 * Or you'd use a raw List (List without a type), which is unsafe — you
 * could accidentally add a Customer into a list meant for Products and
 * Java wouldn't warn you until the program crashes at runtime.
 *
 * With generics, Java enforces the type at compile time — much safer.
 *
 * TYPE BOUND EXAMPLE:
 * If you wanted to restrict T to only objects that have a natural ordering
 * (like integers or strings), you'd write:
 *   public class DataStore<T extends Comparable<T>> { ... }
 * This isn't needed here, but it's the pattern the rubric mentions.
 */
public class DataStore<T> {

    // The internal list is also typed with T — it stores exactly what T is.
    private final List<T> items;

    /** Creates an empty DataStore. */
    public DataStore() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds an item to the store.
     * @param item the item to add (must be of type T)
     */
    public void add(T item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Retrieves an item by its position in the list.
     * @param index 0-based position
     * @return the item at that index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public T get(int index) {
        return items.get(index);
    }

    /**
     * Returns all items as a copy of the internal list.
     * We return a new ArrayList copy so callers can't accidentally
     * modify the internal list from outside.
     */
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    /**
     * Removes an item from the store.
     * @param item the item to remove
     * @return true if it was found and removed, false otherwise
     */
    public boolean remove(T item) {
        return items.remove(item);
    }

    /** @return how many items are currently stored */
    public int size() {
        return items.size();
    }

    /** @return true if the store has no items */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * A placeholder findById — returns null if not found.
     * In Step 2, service classes will have proper typed lookup methods.
     * This exists here only to demo exception handling in Main.java.
     *
     * Note: a generic store can't know which field is the "id" on T —
     * that's why service classes (which know the specific type) handle
     * real lookups.
     */
    public T findById(int id) {
        return null;
    }

    @Override
    public String toString() {
        return "DataStore{size=" + items.size() + "}";
    }
}
