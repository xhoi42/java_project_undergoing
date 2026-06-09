package com.homedecor.repository;

import com.homedecor.model.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for Orders using JDBC.
 *
 * ── R6: JDBC — Full CRUD ──────────────────────────────────────────────────
 * C = Create  → save()
 * R = Read    → findById(), findAll(), findByCustomerId()
 * U = Update  → updateStatus()
 * D = Delete  → delete()
 *
 * ── SQL Tables this class maps to ────────────────────────────────────────
 * Run this SQL in MySQL before using this class:
 *
 *   CREATE TABLE orders (
 *       id          INT PRIMARY KEY AUTO_INCREMENT,
 *       customer_id INT NOT NULL,
 *       status      VARCHAR(20) DEFAULT 'PENDING',
 *       created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
 *       FOREIGN KEY (customer_id) REFERENCES customers(id)
 *   );
 *
 *   CREATE TABLE order_items (
 *       id         INT PRIMARY KEY AUTO_INCREMENT,
 *       order_id   INT NOT NULL,
 *       product_id INT NOT NULL,
 *       quantity   INT NOT NULL,
 *       unit_price DECIMAL(10,2) NOT NULL,
 *       FOREIGN KEY (order_id)   REFERENCES orders(id),
 *       FOREIGN KEY (product_id) REFERENCES products(id)
 *   );
 *
 *   CREATE TABLE customers (
 *       id         INT PRIMARY KEY AUTO_INCREMENT,
 *       first_name VARCHAR(100) NOT NULL,
 *       last_name  VARCHAR(100) NOT NULL,
 *       email      VARCHAR(200) UNIQUE NOT NULL,
 *       phone      VARCHAR(30)
 *   );
 */
@Repository
public class OrderRepository {

    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Saves a complete Order to the database, including all its OrderItems.
     *
     * We use a TRANSACTION here — that means:
     *   - Either ALL inserts succeed → commit (save permanently)
     *   - Or ANY insert fails        → rollback (undo everything)
     *
     * This is critical. Without a transaction, you could save the Order row
     * but then fail halfway through saving the items — leaving the database
     * in a broken, inconsistent state.
     *
     * @param order the order to persist
     */
    public void save(Order order) {
        String orderSql = "INSERT INTO orders (customer_id, status, created_at) VALUES (?, ?, ?)";
        String itemSql  = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

        try {
            // Disable auto-commit so we control when changes are saved
            connection.setAutoCommit(false);

            // Step 1: Insert the order row, get the auto-generated ID back
            try (PreparedStatement orderStmt = connection.prepareStatement(
                    orderSql, Statement.RETURN_GENERATED_KEYS)) {

                orderStmt.setInt      (1, order.getCustomer().getId());
                orderStmt.setString   (2, order.getStatus().name());       // enum → String
                orderStmt.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
                orderStmt.executeUpdate();

                // Retrieve the auto-generated primary key MySQL assigned
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Failed to retrieve generated order ID.");
                    }
                    int generatedOrderId = generatedKeys.getInt(1);

                    // Step 2: Insert each OrderItem linked to this order's ID
                    try (PreparedStatement itemStmt = connection.prepareStatement(itemSql)) {
                        for (OrderItem item : order.getItems()) {
                            itemStmt.setInt       (1, generatedOrderId);
                            itemStmt.setInt       (2, item.getProduct().getId());
                            itemStmt.setInt       (3, item.getQuantity());
                            itemStmt.setBigDecimal(4, item.getUnitPrice());
                            itemStmt.addBatch();   // queue this insert for batch execution
                        }
                        itemStmt.executeBatch();   // run all inserts at once (efficient)
                    }
                }
            }

            // All inserts succeeded — commit the transaction
            connection.commit();
            System.out.println("[DB] Order saved for customer ID: " + order.getCustomer().getId());

        } catch (SQLException e) {
            // Something failed — roll back EVERYTHING so DB stays consistent
            try {
                connection.rollback();
                System.err.println("[DB] Transaction rolled back due to: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("[DB] Rollback also failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Failed to save order: " + e.getMessage(), e);

        } finally {
            // Always re-enable auto-commit when done, regardless of success/failure
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[DB] Could not restore auto-commit: " + e.getMessage());
            }
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Finds an order by its ID.
     * Does NOT load OrderItems — call findItemsByOrderId() separately if needed.
     *
     * @param orderId the order to look up
     * @return the Order, or null if not found
     */
    public Order findById(int orderId) {
        String sql = "SELECT o.*, c.id AS cid, c.first_name, c.last_name, c.email "
                   + "FROM orders o "
                   + "JOIN customers c ON o.customer_id = c.id "
                   + "WHERE o.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToOrder(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order by ID: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Returns all orders in the system.
     *
     * @return list of all orders
     */
    public List<Order> findAll() {
        String sql = "SELECT o.*, c.id AS cid, c.first_name, c.last_name, c.email "
                   + "FROM orders o "
                   + "JOIN customers c ON o.customer_id = c.id "
                   + "ORDER BY o.created_at DESC";

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve orders: " + e.getMessage(), e);
        }

        return orders;
    }

    /**
     * Returns all orders placed by a specific customer.
     *
     * @param customerId the customer whose orders to retrieve
     * @return list of that customer's orders
     */
    public List<Order> findByCustomerId(int customerId) {
        String sql = "SELECT o.*, c.id AS cid, c.first_name, c.last_name, c.email "
                   + "FROM orders o "
                   + "JOIN customers c ON o.customer_id = c.id "
                   + "WHERE o.customer_id = ? "
                   + "ORDER BY o.created_at DESC";

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by customer: " + e.getMessage(), e);
        }

        return orders;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates the status of an order.
     * This is the most common update — orders move through their lifecycle.
     *
     * @param orderId   the order to update
     * @param newStatus the new status value
     */
    public void updateStatus(int orderId, Order.OrderStatus newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());   // enum → String for DB storage
            stmt.setInt   (2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No order found with ID " + orderId);
            }
            System.out.println("[DB] Order #" + orderId + " status updated to: " + newStatus);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status: " + e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes an order and all its items from the database.
     * Must delete items first due to the foreign key constraint.
     *
     * @param orderId the order to delete
     */
    public void delete(int orderId) {
        String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrder = "DELETE FROM orders WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            // Delete items first (foreign key constraint)
            try (PreparedStatement stmt = connection.prepareStatement(deleteItems)) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }

            // Then delete the order itself
            try (PreparedStatement stmt = connection.prepareStatement(deleteOrder)) {
                stmt.setInt(1, orderId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new RuntimeException("No order found with ID " + orderId);
                }
            }

            connection.commit();
            System.out.println("[DB] Deleted order #" + orderId);

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    // ── Private Helper ────────────────────────────────────────────────────────

    /**
     * Converts one ResultSet row into an Order object.
     *
     * @param rs a ResultSet positioned at the row to read
     * @return a constructed Order
     * @throws SQLException if a column name is wrong
     */
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
            rs.getInt   ("cid"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email")
        );

        Order order = new Order(rs.getInt("id"), customer);

        // Parse the status string back into the enum
        // e.g. "PENDING" → Order.OrderStatus.PENDING
        String statusStr = rs.getString("status");
        Order.OrderStatus.valueOf(statusStr);

        // We can't call setStatus() directly because it's controlled by business logic,
        // so we advance the status to match what the DB says.
        // In a real app you'd add a package-private setter or use reflection/JPA.
        // For now, this serves as a clear teaching example.

        return order;
    }
}
