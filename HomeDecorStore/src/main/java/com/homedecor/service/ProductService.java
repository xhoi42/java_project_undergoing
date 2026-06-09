package com.homedecor.service;

import com.homedecor.exception.OutOfStockException;
import com.homedecor.exception.ProductNotFoundException;
import com.homedecor.model.Category;
import com.homedecor.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class that contains all business logic for Products.
 *
 * ── R1: Collections Framework ─────────────────────────────────────────────
 * We use THREE distinct collection types here, each chosen for a reason:
 *
 *   HashMap<Integer, Product>  productCatalog
 *     → Key = product ID, Value = Product object
 *     → Chosen because we VERY often look up a product by its ID.
 *       HashMap gives O(1) lookup — instant, regardless of how many products exist.
 *       A List would require scanning every element: O(n).
 *
 *   TreeSet<String>  productNames
 *     → Stores product names in sorted (alphabetical) order automatically.
 *     → Chosen so we can display a sorted name list without calling sort() every time.
 *     → TreeSet also guarantees no duplicates.
 *
 *   ArrayList is used in Customer (orderHistory) — imported via model layer.
 *
 * ── R3: Lambda Expressions ────────────────────────────────────────────────
 * Used in filtering, sorting, and searching methods below.
 *
 * ── R4: Stream API ────────────────────────────────────────────────────────
 * Used to process collections with filter → map → collect pipelines.
 */
@Service
public class ProductService {

    // ── R1: HashMap — fast ID-based lookup ───────────────────────────────────
    private final Map<Integer, Product> productCatalog = new HashMap<>();

    // ── R1: TreeSet — always-sorted unique name index ────────────────────────
    private final Set<String> productNames = new TreeSet<>();

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Adds a product to the catalog.
     *
     * @param product the product to add
     * @throws IllegalArgumentException if a product with that ID already exists
     */
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (productCatalog.containsKey(product.getId())) {
            throw new IllegalArgumentException(
                "Product with ID " + product.getId() + " already exists."
            );
        }
        productCatalog.put(product.getId(), product);   // HashMap.put()
        productNames.add(product.getName());             // TreeSet.add() — stays sorted
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id the product's unique ID
     * @return the Product
     * @throws ProductNotFoundException if no product has that ID
     */
    public Product getById(int id) {
        // HashMap.get() — O(1), no loop needed
        Product product = productCatalog.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    /**
     * Updates an existing product's price and description.
     *
     * @param id          the product to update
     * @param newPrice    the new price
     * @param description the new description
     */
    public void updateProduct(int id, BigDecimal newPrice, String description) {
        Product product = getById(id);   // throws if not found
        product.setPrice(newPrice);
        product.setDescription(description);
    }

    /**
     * Removes a product from the catalog.
     *
     * @param id the product to remove
     */
    public void removeProduct(int id) {
        Product product = getById(id);
        productCatalog.remove(id);
        productNames.remove(product.getName());
    }

    /**
     * Returns all products as a List.
     *
     * ── R4: Stream + R3: Lambda ──────────────────────────────────────────────
     * productCatalog.values() gives a Collection<Product>.
     * We stream it and collect into a new ArrayList.
     */
    public List<Product> getAllProducts() {
        return productCatalog.values()     // Collection<Product>
                             .stream()     // Stream<Product>
                             .collect(Collectors.toList());  // terminal op → List
    }

    // ── Filtering Methods (R3 + R4) ───────────────────────────────────────────

    /**
     * Returns all products in a given category.
     *
     * Stream pipeline:
     *   stream()                          → open a stream over all products
     *   .filter(p -> ...)                 → keep only products in this category  [R3: lambda]
     *   .collect(Collectors.toList())     → gather results into a List           [R4: terminal op]
     *
     * @param category the category to filter by
     * @return list of products in that category
     */
    public List<Product> getByCategory(Category category) {
        return productCatalog.values()
                             .stream()
                             // R3: lambda — p is each Product, -> is "maps to", result is boolean
                             .filter(p -> p.getCategory().getId() == category.getId())
                             .collect(Collectors.toList());
    }

    /**
     * Returns all products whose price is at or below the given maximum.
     *
     * @param maxPrice the price ceiling (inclusive)
     * @return list of matching products
     */
    public List<Product> getByMaxPrice(BigDecimal maxPrice) {
        return productCatalog.values()
                             .stream()
                             // R3: lambda using BigDecimal.compareTo (can't use <= on objects)
                             .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                             .collect(Collectors.toList());
    }

    /**
     * Returns all products currently in stock (stockQuantity > 0).
     */
    public List<Product> getInStockProducts() {
        return productCatalog.values()
                             .stream()
                             .filter(p -> p.getStockQuantity() > 0)   // R3: lambda
                             .collect(Collectors.toList());
    }

    /**
     * Searches products by name — case-insensitive partial match.
     * e.g. searching "rug" would match "Boho Wool Rug" and "Persian Rug".
     *
     * @param keyword the search term
     * @return list of matching products
     */
    public List<Product> searchByName(String keyword) {
        String lower = keyword.toLowerCase();
        return productCatalog.values()
                             .stream()
                             // R3: lambda — checks if the name contains the keyword
                             .filter(p -> p.getName().toLowerCase().contains(lower))
                             .collect(Collectors.toList());
    }

    // ── Sorting Methods (R3 + R4) ─────────────────────────────────────────────

    /**
     * Returns all products sorted by price (cheapest first).
     *
     * .sorted() takes a Comparator — we use Comparator.comparing() with a
     * method reference (Product::getPrice) which is shorthand for
     * the lambda:  p -> p.getPrice()
     */
    public List<Product> getSortedByPriceAsc() {
        return productCatalog.values()
                             .stream()
                             .sorted(Comparator.comparing(Product::getPrice))  // R3: method ref
                             .collect(Collectors.toList());
    }

    /**
     * Returns all products sorted by price (most expensive first).
     *
     * .reversed() flips the comparator order.
     */
    public List<Product> getSortedByPriceDesc() {
        return productCatalog.values()
                             .stream()
                             .sorted(Comparator.comparing(Product::getPrice).reversed())
                             .collect(Collectors.toList());
    }

    /**
     * Returns all products sorted alphabetically by name.
     */
    public List<Product> getSortedByName() {
        return productCatalog.values()
                             .stream()
                             .sorted(Comparator.comparing(Product::getName))
                             .collect(Collectors.toList());
    }

    // ── Aggregate / Statistics Methods (R4) ──────────────────────────────────

    /**
     * Returns the most expensive product in the catalog.
     *
     * .max() is a terminal Stream operation that takes a Comparator and
     * returns an Optional<Product> (because the catalog might be empty).
     *
     * @return Optional containing the priciest product, or empty if no products
     */
    public Optional<Product> getMostExpensive() {
        return productCatalog.values()
                             .stream()
                             .max(Comparator.comparing(Product::getPrice));
    }

    /**
     * Calculates the average price of all products.
     *
     * .mapToDouble() converts the Stream<Product> to a DoubleStream so we
     * can call .average() on it. Returns OptionalDouble.
     */
    public OptionalDouble getAveragePrice() {
        return productCatalog.values()
                             .stream()
                             .mapToDouble(p -> p.getPrice().doubleValue())  // R3: lambda
                             .average();                                     // R4: terminal op
    }

    /**
     * Returns the count of available products per category.
     *
     * .collect(Collectors.groupingBy(...)) groups elements by a classifier function
     * and counts them. Result: Map<String, Long> e.g. {"Living Room"=3, "Bedroom"=2}
     */
    public Map<String, Long> countByCategory() {
        return productCatalog.values()
                             .stream()
                             .collect(Collectors.groupingBy(
                                 p -> p.getCategory().getName(),  // R3: lambda classifier
                                 Collectors.counting()            // R4: downstream collector
                             ));
    }

    /**
     * Returns all product names in alphabetical order.
     * Uses the TreeSet which auto-sorts on insert — no Stream needed here.
     */
    public Set<String> getAllProductNamesSorted() {
        return Collections.unmodifiableSet(productNames);  // R1: TreeSet
    }

    /**
     * Checks stock and throws OutOfStockException if insufficient.
     * Called before placing an order.
     *
     * @param productId the product to check
     * @param quantity  how many units are needed
     * @throws OutOfStockException if stock is insufficient
     */
    public void validateStock(int productId, int quantity) {
        Product product = getById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException(
                product.getName(), quantity, product.getStockQuantity()
            );
        }
    }

    /** @return how many products are in the catalog */
    public int getTotalProductCount() {
        return productCatalog.size();
    }
}
