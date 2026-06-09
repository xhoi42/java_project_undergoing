package com.homedecor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered customer of the store.
 *
 * A customer has personal details and a history of their past orders.
 * The order history uses ArrayList — one of the Collections Framework
 * types required by R1 of your project.
 *
 * ArrayList is a resizable array. It's the go-to when you need an
 * ordered list where you add items and iterate over them.
 */
public class Customer {

    // ── Fields ───────────────────────────────────────────────────────────────
    private int    id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;           // optional

    /**
     * orderHistory keeps track of all orders this customer has placed.
     * We use ArrayList<Order> because:
     *   - We need to add orders one at a time (dynamic size)
     *   - We often iterate through them in the order they were placed
     *   - Random access by index is fine (ArrayList is O(1) get)
     */
    private List<Order> orderHistory;

    // ── Constructor ──────────────────────────────────────────────────────────
    /**
     * Creates a new Customer.
     * The order history starts empty — it fills as orders are placed.
     */
    public Customer(int id, String firstName, String lastName, String email) {
        this.id           = id;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
        this.orderHistory = new ArrayList<>();  // start with an empty list
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    /** @return first + last name combined */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adds a completed order to this customer's history.
     * Called by OrderService after an order is confirmed.
     */
    public void addOrder(Order order) {
        if (order != null) {
            orderHistory.add(order);
        }
    }

    /** @return how many orders this customer has placed */
    public int getTotalOrders() {
        return orderHistory.size();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int         getId()           { return id; }
    public String      getFirstName()    { return firstName; }
    public String      getLastName()     { return lastName; }
    public String      getEmail()        { return email; }
    public String      getPhone()        { return phone; }
    public List<Order> getOrderHistory() { return orderHistory; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName  = lastName; }
    public void setEmail(String email)         { this.email     = email; }
    public void setPhone(String phone)         { this.phone     = phone; }

    // ── toString ──────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "Customer{id="   + id
             + ", name='"       + getFullName() + "'"
             + ", email='"      + email + "'"
             + ", orders="      + orderHistory.size()
             + "}";
    }
}
