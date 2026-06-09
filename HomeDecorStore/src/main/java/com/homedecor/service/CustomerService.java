package com.homedecor.service;

import com.homedecor.exception.ProductNotFoundException;
import com.homedecor.model.Customer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Customer-related business logic.
 *
 * ── R1: Collections ──────────────────────────────────────────────────────
 * Uses HashMap<Integer, Customer> for fast ID-based customer lookup.
 */
@Service
public class CustomerService {

    // ── R1: HashMap — key=customerId, value=Customer ─────────────────────────
    private final Map<Integer, Customer> customers = new HashMap<>();

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * Registers a new customer.
     *
     * @param customer the customer to register
     * @throws IllegalArgumentException if email already exists
     */
    public void register(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        // Check for duplicate email — stream through values looking for a match
        // R3: lambda   R4: anyMatch terminal operation
        boolean emailTaken = customers.values()
                                      .stream()
                                      .anyMatch(c -> c.getEmail()
                                                      .equalsIgnoreCase(customer.getEmail()));
        if (emailTaken) {
            throw new IllegalArgumentException(
                "Email '" + customer.getEmail() + "' is already registered."
            );
        }
        customers.put(customer.getId(), customer);
    }

    /**
     * Finds a customer by their ID.
     *
     * @param id the customer's ID
     * @return the Customer
     * @throws ProductNotFoundException repurposed here — ideally you'd have a
     *         CustomerNotFoundException, but we reuse what exists for brevity
     */
    public Customer getById(int id) {
        Customer c = customers.get(id);
        if (c == null) {
            throw new IllegalArgumentException("Customer with ID " + id + " not found.");
        }
        return c;
    }

    /**
     * Finds a customer by their email address.
     *
     * R3 + R4: stream with filter and findFirst
     *
     * @param email the email to search for
     * @return the matching Customer, or null if not found
     */
    public Customer getByEmail(String email) {
        return customers.values()
                        .stream()
                        // R3: lambda — case-insensitive email comparison
                        .filter(c -> c.getEmail().equalsIgnoreCase(email))
                        .findFirst()      // R4: short-circuit terminal op
                        .orElse(null);    // return null if not found
    }

    /**
     * Returns all registered customers.
     *
     * R4: stream → collect
     */
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    /**
     * Returns the top N customers by number of orders placed.
     *
     * R3: Comparator lambda   R4: sorted + limit + collect
     *
     * @param n how many top customers to return
     * @return list of top customers, most orders first
     */
    public List<Customer> getTopCustomers(int n) {
        return customers.values()
                        .stream()
                        // R3: lambda comparator — sort by order count descending
                        .sorted((c1, c2) -> c2.getTotalOrders() - c1.getTotalOrders())
                        .limit(n)                       // R4: take only top N
                        .collect(Collectors.toList());
    }

    /**
     * Updates a customer's contact info.
     */
    public void updateContact(int id, String phone, String email) {
        Customer c = getById(id);
        c.setPhone(phone);
        c.setEmail(email);
    }

    /** @return total number of registered customers */
    public int getTotalCustomers() {
        return customers.size();
    }
}
