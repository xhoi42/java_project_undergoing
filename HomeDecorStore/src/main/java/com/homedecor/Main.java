package com.homedecor;

import com.homedecor.exception.InvalidOrderException;
import com.homedecor.model.*;
import com.homedecor.pattern.CartBuilder;
import com.homedecor.pattern.DiscountStrategy;
import com.homedecor.service.CustomerService;
import com.homedecor.service.OrderService;
import com.homedecor.service.ProductService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main entry point — demonstrates every layer of the application.
 *
 * NOTE: The REST layer (ProductController, OrderController) and
 * the database layer (ProductRepository, OrderRepository) are not
 * called here because they require Spring Boot and MySQL running.
 * They are demonstrated via Postman/browser when Spring Boot starts.
 *
 * Everything else (model, service, patterns, concurrency) is shown here.
 */
public class Main {

    public static void main(String[] args) throws InvalidOrderException, InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  Home Decor & Aesthetic Interior Store   ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        // ════════════════════════════════════════════════════════════════
        // STEP 1 — Model Layer
        // ════════════════════════════════════════════════════════════════
        System.out.println("── STEP 1: Model Layer ──────────────────────\n");

        Category livingRoom = new Category(1, "Living Room", "Sofas, rugs, coffee tables");
        Category bedroom    = new Category(2, "Bedroom",     "Beds, nightstands, lamps");
        Category kitchen    = new Category(3, "Kitchen",     "Minimalist kitchenware");

        Product rug  = new Product(1, "Boho Wool Rug",       new BigDecimal("149.99"), 10, livingRoom);
        Product lamp = new Product(2, "Arch Floor Lamp",     new BigDecimal("89.50"),  5,  bedroom);
        Product vase = new Product(3, "Ceramic Bud Vase",    new BigDecimal("24.00"),  30, livingRoom);
        Product mug  = new Product(4, "Stoneware Matte Mug", new BigDecimal("18.00"),  50, kitchen);
        Product sofa = new Product(5, "Linen Cloud Sofa",    new BigDecimal("899.00"), 3,  livingRoom);

        Customer ana  = new Customer(1, "Ana",  "Berisha", "ana@email.com");
        Customer dion = new Customer(2, "Dion", "Krasniqi","dion@email.com");

        System.out.println("Products: " + rug + "\n         " + lamp);
        System.out.println("Customers: " + ana.getFullName() + ", " + dion.getFullName() + "\n");

        // ════════════════════════════════════════════════════════════════
        // STEP 2 — Service Layer: Collections, Lambdas, Streams
        // ════════════════════════════════════════════════════════════════
        System.out.println("── STEP 2: Service Layer (R1, R3, R4) ───────\n");

        // Set up services
        ProductService  productService  = new ProductService();
        CustomerService customerService = new CustomerService();
        OrderService    orderService    = new OrderService(productService, customerService);

        // Add products to the catalog (stored in HashMap — R1)
        productService.addProduct(rug);
        productService.addProduct(lamp);
        productService.addProduct(vase);
        productService.addProduct(mug);
        productService.addProduct(sofa);

        // Register customers
        customerService.register(ana);
        customerService.register(dion);

        // R4: Stream — filter by category
        List<Product> livingRoomProducts = productService.getByCategory(livingRoom);
        System.out.println("Living Room products (" + livingRoomProducts.size() + "):");
        livingRoomProducts.forEach(p -> System.out.println("  " + p.getName() + " - $" + p.getPrice()));

        // R4: Stream — filter by max price
        List<Product> affordable = productService.getByMaxPrice(new BigDecimal("100"));
        System.out.println("\nProducts under $100: " + affordable.size());
        affordable.forEach(p -> System.out.println("  " + p.getName()));

        // R4: Stream — sorted by price
        System.out.println("\nAll products (cheapest first):");
        productService.getSortedByPriceAsc()
                      .forEach(p -> System.out.println("  $" + p.getPrice() + " - " + p.getName()));

        // R4: Stream — statistics
        System.out.println("\nMost expensive: " +
            productService.getMostExpensive().map(Product::getName).orElse("none"));
        System.out.printf("Average price:  $%.2f%n",
            productService.getAveragePrice().orElse(0));

        // R4: Stream — groupingBy
        System.out.println("Products per category: " + productService.countByCategory());

        // R1: TreeSet — sorted product names
        System.out.println("Sorted names: " + productService.getAllProductNamesSorted());

        // Place an order
        System.out.println("\nPlacing order for Ana...");
        Map<Integer, Integer> anaCart = new HashMap<>();
        anaCart.put(1, 1);   // 1x Boho Wool Rug
        anaCart.put(3, 2);   // 2x Ceramic Bud Vase
        Order anaOrder = orderService.placeOrder(1, anaCart);
        System.out.println("Order placed: " + anaOrder);
        System.out.println("Order total:  $" + anaOrder.calculateTotal());

        // Advance the order status
        orderService.advanceOrderStatus(anaOrder.getId());
        System.out.println("Status after advance: " + anaOrder.getStatus());

        // Revenue report
        System.out.println("Projected revenue: $" + orderService.calculateProjectedRevenue());
        System.out.println("Orders by status:  " + orderService.getOrderCountByStatus());

        // ════════════════════════════════════════════════════════════════
        // STEP 5 — Concurrency (R5)
        // ════════════════════════════════════════════════════════════════
        System.out.println("\n── STEP 5: Concurrency (R5) ─────────────────\n");

        // ExecutorService manages a thread pool
        // Instead of creating new threads manually (expensive), we reuse a pool of 3 threads
        java.util.concurrent.ExecutorService executor =
            java.util.concurrent.Executors.newFixedThreadPool(3);

        System.out.println("Submitting 3 concurrent order-processing tasks...");

        // Submit 3 tasks — they run in parallel on separate threads
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            executor.submit(() -> {
                // Simulate processing time (e.g. payment gateway, stock check)
                try {
                    Thread.sleep(200);
                    System.out.println("  [Thread " + Thread.currentThread().getName()
                                     + "] Processed order task #" + taskId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // CompletableFuture — run async and get result when ready
        java.util.concurrent.CompletableFuture<String> inventoryCheck =
            java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                // Simulates an async inventory check running in background
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return "Inventory check complete: all items in stock";
            });

        // thenAccept — runs when the future completes, without blocking
        inventoryCheck.thenAccept(result ->
            System.out.println("  [Async] " + result)
        );

        // Shut down the executor — waits for all submitted tasks to finish
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        Thread.sleep(300);   // allow CompletableFuture to print

        // ════════════════════════════════════════════════════════════════
        // STEP 6 — Design Patterns (R9)
        // ════════════════════════════════════════════════════════════════
        System.out.println("\n── STEP 6: Design Patterns (R9) ─────────────\n");

        // ── Builder Pattern ───────────────────────────────────────────────────
        System.out.println("[ Builder Pattern ]");
        CartBuilder.Cart cart = new CartBuilder()
            .addItem(rug,  1)
            .addItem(vase, 3)   // 3 vases → B2G1 will save $24
            .addItem(mug,  2)
            .applyDiscount(10)  // 10% off
            .withPromoCode("WELCOME10")
            .withGiftWrap(true)
            .withDeliveryNotes("Leave at door")
            .build();

        System.out.println("Cart built:    " + cart);
        System.out.println("Subtotal:      $" + cart.getSubtotal());
        System.out.println("Discount (10%):$" + cart.getDiscountAmount());
        System.out.println("Final total:   $" + cart.getTotal());
        System.out.println("Gift wrap:     " + cart.isGiftWrap());
        System.out.println("Promo code:    " + cart.getPromoCode());

        // ── Strategy Pattern ──────────────────────────────────────────────────
        System.out.println("\n[ Strategy Pattern ]");
        BigDecimal orderTotal = new BigDecimal("250.00");

        DiscountStrategy.DiscountContext ctx = new DiscountStrategy.DiscountContext();

        // Strategy 1: No discount (default)
        ctx.setStrategy(DiscountStrategy.noDiscount());
        ctx.applyDiscount(orderTotal);

        // Strategy 2: 15% off
        ctx.setStrategy(DiscountStrategy.percentage(15));
        ctx.applyDiscount(orderTotal);

        // Strategy 3: $30 off orders over $200
        ctx.setStrategy(DiscountStrategy.fixedAmount(
            new BigDecimal("30"), new BigDecimal("200")
        ));
        ctx.applyDiscount(orderTotal);

        // Strategy 4: Lambda — half price (R3: functional interface as lambda)
        ctx.setStrategy(DiscountStrategy.halfPrice());
        ctx.applyDiscount(orderTotal);

        // Strategy 5: Buy 2 Get 1 Free on cart items
        ctx.setStrategy(DiscountStrategy.buyTwoGetOneFree(cart.getItems()));
        ctx.applyDiscount(cart.getSubtotal());

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║     All layers demonstrated! ✓           ║");
        System.out.println("║  Start Spring Boot to test REST + DB     ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
