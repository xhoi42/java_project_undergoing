# Home Decor Store - Setup & Run Guide

## Project Structure

- `src/com/homedecor/` - Java backend (Spring Boot)
  - `model/` - Data models (Product, Order, Customer, etc.)
  - `service/` - Business logic (ProductService, OrderService, etc.)
  - `rest/` - REST API controllers (ProductController, OrderController)
  - `repository/` - Data access layers with JDBC
  - `exception/` - Custom exceptions
  - `pattern/` - Design patterns (Builder, Strategy)
  - `util/` - Utilities
  - `config/` - Spring configuration (CORS)
- `src/com/frontend/index.html` - Frontend (modern e-commerce UI)
- `pom.xml` - Maven configuration
- `src/com/homedecor/main/resources/application.properties` - Database & server config
- `src/com/homedecor/main/resources/schema.sql` - Database schema & sample data

## ✓ What's Fixed

✅ All Java files compile without errors (50 class files generated)
✅ Spring Boot annotations (@Service, @Repository, @RestController, etc.)
✅ REST API endpoints for products, orders, and analytics
✅ CORS configuration for frontend-backend communication
✅ HTML frontend with real-time product catalog, shopping cart, orders tracking
✅ Database schema with sample data ready

## Prerequisites

### 1. Java 17+ (or OpenJDK)
```bash
java -version   # Verify Java is installed
```

### 2. MySQL 8+
```bash
mysql --version   # Verify MySQL is installed
```

### 3. Maven (Optional - for building with dependencies)
```bash
mvn --version   # Verify Maven is installed
```

## Database Setup

### Create Database and User

1. Connect to MySQL:
```bash
mysql -u root -p
```

2. Run these SQL commands:
```sql
CREATE DATABASE homedecor;
CREATE USER 'homedecor_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON homedecor.* TO 'homedecor_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Load Sample Data

```bash
mysql -u homedecor_user -p homedecor < src/com/homedecor/main/resources/schema.sql
```

When prompted, enter password: `password123`

This creates:
- 5 categories (Living Room, Bedroom, Kitchen, Bathroom, Office)
- 10 sample products with prices and stock
- 3 demo customers

## Running the Application

### Option A: Using Maven (Full Spring Boot)

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Option B: Using Java Directly (With stubs - for testing)

```bash
java -cp out com.homedecor.HomeDecorStoreApplication
```

Note: This uses compiled stub Spring classes; database won't connect. Use Option A for full functionality.

### Option C: Using Main class (Demo mode)

```bash
java -cp out com.homedecor.Main
```

This runs the application demo without Spring Boot.

## Access the Application

Once the backend is running on `http://localhost:8080`:

### 1. Visit the Frontend
Open `src/com/frontend/index.html` in a web browser (or use a local server):
```bash
# Using Python 3
python -m http.server 8000 --directory src/com/frontend

# Using Node.js http-server
npx http-server src/com/frontend -p 8000
```

Then visit: `http://localhost:8000/index.html`

### 2. Test the API Directly

```bash
# Get all products
curl http://localhost:8080/api/products

# Search products
curl "http://localhost:8080/api/products/search?keyword=rug"

# Filter by max price
curl "http://localhost:8080/api/products/filter?maxPrice=100"

# Get all orders
curl http://localhost:8080/api/orders

# Get analytics
curl http://localhost:8080/api/orders/revenue
curl http://localhost:8080/api/products/stats

# Place an order (create order.json first)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "items": {"1": 2, "3": 1}}'
```

## Features Implemented

### Frontend (HTML/CSS/JavaScript)
- 🛒 **Product Catalog** - Browse 10+ curated home decor items
- 🔍 **Search & Filter** - Search by name, filter by price, sort by category
- 🛍️ **Shopping Cart** - Add/remove items, adjust quantities
- 📦 **Order Management** - Place orders, track status (PENDING → DELIVERED)
- 📊 **Analytics Dashboard** - View sales stats, revenue, product categories
- 📱 **Responsive Design** - Works on desktop and mobile
- 🎨 **Modern UI** - Dark theme with gold accents, smooth animations

### Backend (Java/Spring Boot)
- ✅ REST API endpoints (GET, POST, PUT, DELETE)
- ✅ Services layer for business logic
- ✅ JDBC repositories for database access
- ✅ Custom exceptions for error handling
- ✅ Design patterns: Builder, Strategy, Singleton
- ✅ Collections Framework (HashMap, TreeSet, ArrayList)
- ✅ Lambda expressions & Streams API
- ✅ CORS configuration for frontend access

### Database (MySQL)
- Products with categories, pricing, and stock tracking
- Orders with multiple items and status tracking
- Customers with contact information
- Order history and revenue reporting

## Demo Credentials

You can test with these pre-loaded customers:

| Customer ID | Name | Email |
|---|---|---|
| 1 | Ana Berisha | ana.berisha@email.com |
| 2 | Dion Krasniqi | dion.krasniqi@email.com |
| 3 | Elsa Marku | elsa.marku@email.com |

## Troubleshooting

### "Connection refused" on port 8080
- Make sure the backend is running: `mvn spring-boot:run`
- Wait 5-10 seconds for Spring Boot to start
- Check if another app is using port 8080

### "No database driver found"
- Ensure MySQL is running and accessible
- Verify credentials in `application.properties`
- Check that `mysql-connector-j` is installed via Maven

### Frontend shows "Backend Offline"
- Verify backend is running on `http://localhost:8080/api/products`
- Check browser console (F12) for CORS or connection errors
- Ensure frontend is served from a web server (not `file://`)

### Database connection fails
- Run schema.sql to create tables: `mysql -u homedecor_user -p homedecor < schema.sql`
- Verify MySQL credentials: `mysql -u homedecor_user -p` → enter `password123`
- Check that database `homedecor` exists: `mysql -u homedecor_user -p -e "SHOW DATABASES;"`

## Next Steps

1. **Start MySQL**: Ensure the database service is running
2. **Run Database Setup**: Load the schema and sample data
3. **Start Backend**: `mvn spring-boot:run`
4. **Serve Frontend**: Use a local web server to serve `index.html`
5. **Test API**: Use the curl commands above or the frontend UI
6. **Place an Order**: Use customer ID 1, add products, place order
7. **Track Orders**: View order status and analytics on the dashboard

Enjoy! 🎨✨
