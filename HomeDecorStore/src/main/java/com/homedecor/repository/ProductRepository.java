package com.homedecor.repository;

import com.homedecor.model.Category;
import com.homedecor.model.Product;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for Products using JDBC.
 *
 * ── R6: JDBC — Full CRUD ──────────────────────────────────────────────────
 * C = Create  → save()
 * R = Read    → findById(), findAll()
 * U = Update  → update()
 * D = Delete  → delete()
 *
 * ── SQL Table this class maps to ─────────────────────────────────────────
 * Run this SQL in MySQL before using this class:
 *
 *   CREATE TABLE categories (
 *       id          INT PRIMARY KEY AUTO_INCREMENT,
 *       name        VARCHAR(100) NOT NULL,
 *       description TEXT
 *   );
 *
 *   CREATE TABLE products (
 *       id             INT PRIMARY KEY AUTO_INCREMENT,
 *       name           VARCHAR(200) NOT NULL,
 *       price          DECIMAL(10, 2) NOT NULL,
 *       stock_quantity INT DEFAULT 0,
 *       description    TEXT,
 *       available      BOOLEAN DEFAULT TRUE,
 *       category_id    INT,
 *       FOREIGN KEY (category_id) REFERENCES categories(id)
 *   );
 *
 * ── Why PreparedStatement instead of Statement? ───────────────────────────
 * NEVER do: "SELECT * FROM products WHERE id = " + id
 * That's a SQL Injection vulnerability — a user could type:
 *   id = "1 OR 1=1; DROP TABLE products;"
 * and destroy your database.
 *
 * PreparedStatement uses placeholders (?):
 *   "SELECT * FROM products WHERE id = ?"
 * and then stmt.setInt(1, id) fills in the value SAFELY.
 * The database treats it as data, never as SQL code.
 */
@Repository
public class ProductRepository {

    // Get the shared JDBC connection
    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new product into the database.
     *
     * @param product the product to save
     * @throws RuntimeException if the SQL fails
     */
    public void save(Product product) {
        // SQL with ? placeholders — values filled in safely below
        String sql = "INSERT INTO products (name, price, stock_quantity, description, available, category_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        // try-with-resources: automatically closes the PreparedStatement when done
        // even if an exception is thrown — prevents resource leaks
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Fill in the placeholders in order (1-indexed)
            stmt.setString (1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt    (3, product.getStockQuantity());
            stmt.setString (4, product.getDescription());
            stmt.setBoolean(5, product.isAvailable());
            stmt.setInt    (6, product.getCategory().getId());

            stmt.executeUpdate();   // run the INSERT
            System.out.println("[DB] Saved product: " + product.getName());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * Finds a product by its ID.
     * Uses a JOIN to also fetch the category name in one query.
     *
     * @param id the product ID to look up
     * @return the Product, or null if not found
     */
    public Product findById(int id) {
        // JOIN lets us get category data in the same query — no second query needed
        String sql = "SELECT p.*, c.name AS cat_name, c.description AS cat_desc "
                   + "FROM products p "
                   + "JOIN categories c ON p.category_id = c.id "
                   + "WHERE p.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            // executeQuery() returns a ResultSet — like a spreadsheet of results
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 'next()' moves to the first (and here, only) row
                    return mapRowToProduct(rs);   // convert the row to a Product object
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product by ID: " + e.getMessage(), e);
        }

        return null;   // product not found
    }

    /**
     * Returns all products from the database.
     *
     * @return list of all products
     */
    public List<Product> findAll() {
        String sql = "SELECT p.*, c.name AS cat_name, c.description AS cat_desc "
                   + "FROM products p "
                   + "JOIN categories c ON p.category_id = c.id "
                   + "ORDER BY p.name";

        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through every row in the result set
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve products: " + e.getMessage(), e);
        }

        return products;
    }

    /**
     * Returns all products in a specific category.
     *
     * @param categoryId the category to filter by
     * @return list of matching products
     */
    public List<Product> findByCategory(int categoryId) {
        String sql = "SELECT p.*, c.name AS cat_name, c.description AS cat_desc "
                   + "FROM products p "
                   + "JOIN categories c ON p.category_id = c.id "
                   + "WHERE p.category_id = ?";

        List<Product> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find products by category: " + e.getMessage(), e);
        }

        return products;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates a product's price, stock, description and availability.
     *
     * @param product the product with updated values (uses product.getId() to target the row)
     */
    public void update(Product product) {
        String sql = "UPDATE products SET name=?, price=?, stock_quantity=?, "
                   + "description=?, available=? WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString    (1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt       (3, product.getStockQuantity());
            stmt.setString    (4, product.getDescription());
            stmt.setBoolean   (5, product.isAvailable());
            stmt.setInt       (6, product.getId());          // WHERE id = ?

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No product found with ID " + product.getId());
            }
            System.out.println("[DB] Updated product ID: " + product.getId());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a product from the database by ID.
     *
     * @param id the product to delete
     */
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No product found with ID " + id + " to delete.");
            }
            System.out.println("[DB] Deleted product ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }

    // ── Private Helper ────────────────────────────────────────────────────────

    /**
     * Converts one row from the ResultSet into a Product object.
     * This mapping (DB row → Java object) is what an ORM like JPA does
     * automatically. Here we do it manually to understand what JPA replaces.
     *
     * @param rs a ResultSet positioned at the row to read
     * @return a fully constructed Product
     * @throws SQLException if a column name is wrong
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        // Build the Category from the JOIN columns
        Category category = new Category(
            rs.getInt   ("category_id"),
            rs.getString("cat_name"),
            rs.getString("cat_desc")
        );

        // Build the Product
        Product product = new Product(
            rs.getInt       ("id"),
            rs.getString    ("name"),
            rs.getBigDecimal("price"),
            rs.getInt       ("stock_quantity"),
            category
        );
        product.setDescription(rs.getString("description"));
        product.setAvailable  (rs.getBoolean("available"));

        return product;
    }
}
