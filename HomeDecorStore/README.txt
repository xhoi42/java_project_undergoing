========================================
  Home Decor & Aesthetic Interior Store
  CS 305 - Advanced Java | UNYT Spring 2026
  Student: [Your Name Here]
========================================

--- COMPLETED FEATURES ---

✅ Step 1: Models & Utilities (GenericDataStore, Collections, Enums)
✅ Step 2: Service Layer (ProductService, OrderService, CustomerService)
✅ Step 3: Database (JDBC repositories, schema.sql with sample data)
✅ Step 4: REST API (Spring Boot controllers @RestController, @RequestMapping)
✅ Step 5: Concurrency (Order processing, Revenue calculations)
✅ Step 6: Design Patterns (Builder: CartBuilder, Strategy: DiscountStrategy)
✅ Step 7: Frontend (Modern HTML5 UI with shopping cart, checkout, analytics)
✅ Step 8: CORS Configuration (Frontend-backend communication enabled)

--- HOW TO COMPILE AND RUN ---

Quick Start (All-in-One):
  See SETUP.md for comprehensive setup guide with troubleshooting

Requirements:
  - Java 17 or higher          (check with: java -version)
  - Maven 3.6+                 (check with: mvn --version) [OPTIONAL]
  - MySQL 8+                   (check with: mysql --version) [Optional for full DB]

OPTION A: Full Spring Boot Application (Recommended)
  1. mvn clean install
  2. mvn spring-boot:run
  3. Open: http://localhost:8080

OPTION B: Quick Compile & Run (No Maven/No Database)
  1. javac -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
  2. java -cp out com.homedecor.Main

OPTION C: Using Compiled Stubs (For Testing Logic)
  1. java -cp out com.homedecor.HomeDecorStoreApplication

--- PROJECT STRUCTURE (All Steps) ---

--- PROJECT STRUCTURE (All Steps) ---

src/
└── com/homedecor/
    ├── HomeDecorStoreApplication.java     Spring Boot entry point
    ├── Main.java                          Demo mode entry point
    ├── model/                             (Step 1)
    │   ├── Category.java                  Product category with JSON support
    │   ├── Product.java                   Store product with price/stock
    │   ├── Customer.java                  Registered customer
    │   ├── Order.java                     Customer order with status enum
    │   ├── OrderItem.java                 One line item in order
    │   └── CartItem.java                  Shopping cart item
    ├── exception/                         (Step 1)
    │   ├── ProductNotFoundException.java  Unchecked - product not found
    │   ├── OutOfStockException.java       Unchecked - insufficient stock
    │   └── InvalidOrderException.java     Checked - invalid order state
    ├── service/                           (Step 2)
    │   ├── ProductService.java            Product business logic
    │   ├── OrderService.java              Order processing & revenue
    │   └── CustomerService.java           Customer management
    ├── repository/                        (Step 3)
    │   ├── DatabaseConnection.java        JDBC connection singleton
    │   ├── ProductRepository.java         Product data access layer
    │   └── OrderRepository.java           Order data access with transactions
    ├── rest/                              (Step 4)
    │   ├── ProductController.java         REST endpoints for products
    │   └── OrderController.java           REST endpoints for orders
    ├── pattern/                           (Step 6)
    │   ├── CartBuilder.java               Builder pattern for shopping carts
    │   └── DiscountStrategy.java          Strategy pattern for discount algorithms
    ├── config/                            (Step 8)
    │   └── CorsConfig.java                CORS configuration for frontend access
    └── util/                              (Step 1)
        └── DataStore.java                 Generic in-memory store (R2: Generics)

Additional Resources:
├── src/com/homedecor/main/resources/
│   ├── application.properties             Spring Boot database config
│   └── schema.sql                         MySQL schema + sample data
├── src/com/frontend/
│   ├── index.html                         Modern e-commerce UI
│   └── [CSS/JavaScript embedded]          Shopping, checkout, analytics
├── pom.xml                                Maven configuration
├── SETUP.md                               Comprehensive setup guide
├── setup.ps1                              Windows setup script
├── setup.sh                               Linux/Mac setup script
└── README.txt                             This file

--- REST API ENDPOINTS ---

Products:
  GET    /api/products                    List all products
  GET    /api/products/{id}               Get product by ID
  GET    /api/products/search             Search by keyword
  GET    /api/products/filter             Filter by max price
  GET    /api/products/stats              Get category statistics

Orders:
  GET    /api/orders                      List all orders
  GET    /api/orders/{id}                 Get order by ID
  POST   /api/orders                      Place a new order
  PUT    /api/orders/{id}/advance         Advance order status
  GET    /api/orders/revenue              Get total revenue

--- FEATURES IMPLEMENTED ---

Backend (Java/Spring Boot):
  ✅ REST API with 9 endpoints (GET, POST, PUT)
  ✅ Service layer with business logic
  ✅ JDBC repositories for data access
  ✅ Collections Framework (HashMap, TreeSet, ArrayList)
  ✅ Streams API and Lambda expressions
  ✅ Custom exception handling
  ✅ Design Patterns: Builder (CartBuilder), Strategy (DiscountStrategy)
  ✅ Constructor injection for dependencies
  ✅ Generic DataStore<T> for type-safe storage
  ✅ Spring Boot annotations (@Service, @Repository, @RestController)
  ✅ CORS configuration for browser access

Frontend (HTML5/JavaScript):
  ✅ Modern responsive e-commerce UI
  ✅ Product catalog with real-time search
  ✅ Filter by price and category
  ✅ Shopping cart with add/remove functionality
  ✅ Checkout with order placement
  ✅ Order tracking and status management
  ✅ Analytics dashboard with revenue tracking
  ✅ Auto-detect backend API endpoint
  ✅ Comprehensive error handling
  ✅ Demo data fallback for offline testing

Database (MySQL):
  ✅ Schema with 5 product categories
  ✅ 10 sample products with pricing
  ✅ 3 demo customers with order history
  ✅ Transaction-safe order operations
  ✅ Foreign key relationships
  ✅ Stock quantity tracking

--- TECHNICAL DETAILS ---

R1 - Generics: DataStore<T> demonstrates parameterized types
R2 - Collections: HashMap, TreeSet, ArrayList, Stream operations
R3 - Streams/Lambdas: ProductService uses filter(), map(), sorted()
R4 - Constructors/Overloading: Multiple constructors in model classes
R5 - Concurrency: Order processing and revenue calculations
R6 - JDBC: ProductRepository, OrderRepository with PreparedStatement
R7 - JPA/Transactions: OrderRepository with atomic operations
R8 - Spring Boot: @RestController, @Service, @Repository, @RequestMapping
R9 - Design Patterns: Builder (CartBuilder), Strategy (DiscountStrategy)

--- TESTING THE APPLICATION ---

1. Start Backend:
   mvn spring-boot:run

2. Verify API is running:
   curl http://localhost:8080/api/products

3. Serve Frontend:
   python -m http.server 8000 --directory src/com/frontend

4. Open in Browser:
   http://localhost:8000/index.html

5. Test Features:
   - Browse products
   - Add items to cart
   - Place order (use customer ID 1, 2, or 3)
   - Track order status
   - View analytics

--- AI DISCLOSURE ---
[Fill in your AI disclosure here per Section 6 of the project description]
