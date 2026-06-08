-- ══════════════════════════════════════════════════════════════════════════════
--  schema.sql
--  Run this in MySQL ONCE to create all tables before starting the application.
--
--  How to run:
--    mysql -u homedecor_user -p homedecor < src/main/resources/schema.sql
--
--  Or paste into MySQL Workbench / DBeaver and execute.
-- ══════════════════════════════════════════════════════════════════════════════

USE homedecor;

-- ── Drop tables in reverse dependency order ───────────────────────────────────
-- (child tables with foreign keys must be dropped before parent tables)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;

-- ── categories ────────────────────────────────────────────────────────────────
CREATE TABLE categories (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ── products ──────────────────────────────────────────────────────────────────
CREATE TABLE products (
    id             INT            PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(200)   NOT NULL,
    price          DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT            NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    description    TEXT,
    available      BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id    INT            NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

-- ── customers ─────────────────────────────────────────────────────────────────
CREATE TABLE customers (
    id         INT          PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    phone      VARCHAR(30)
);

-- ── orders ────────────────────────────────────────────────────────────────────
CREATE TABLE orders (
    id          INT         PRIMARY KEY AUTO_INCREMENT,
    customer_id INT         NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

-- ── order_items ───────────────────────────────────────────────────────────────
-- Stores each line of an order: which product, how many, and the price at time of order
CREATE TABLE order_items (
    id         INT            PRIMARY KEY AUTO_INCREMENT,
    order_id   INT            NOT NULL,
    product_id INT            NOT NULL,
    quantity   INT            NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- ── Sample Data ───────────────────────────────────────────────────────────────
-- Insert some categories
INSERT INTO categories (name, description) VALUES
    ('Living Room', 'Sofas, rugs, coffee tables, and decorative pieces'),
    ('Bedroom',     'Beds, nightstands, lamps, and cozy textiles'),
    ('Kitchen',     'Minimalist kitchenware, storage, and countertop pieces'),
    ('Bathroom',    'Towels, candles, and spa-inspired accessories'),
    ('Office',      'Desk accessories, plants, and aesthetic organizers');

-- Insert some products
INSERT INTO products (name, price, stock_quantity, description, available, category_id) VALUES
    ('Boho Wool Rug',           149.99, 10, 'Hand-woven wool rug with natural tones',        TRUE, 1),
    ('Arch Floor Lamp',          89.50,  5, 'Minimalist arched lamp with linen shade',        TRUE, 2),
    ('Ceramic Bud Vase',         24.00, 30, 'Matte ceramic vase, perfect for single stems',   TRUE, 1),
    ('Stoneware Matte Mug',      18.00, 50, 'Artisan-style mug in earthy tones',              TRUE, 3),
    ('Linen Cloud Sofa',        899.00,  3, 'Oversized linen sofa in natural beige',          TRUE, 1),
    ('Rattan Side Table',        79.99,  8, 'Handcrafted rattan table with glass top',        TRUE, 1),
    ('Japandi Desk Organizer',   34.00, 20, 'Bamboo and concrete desk organizer set',         TRUE, 5),
    ('Pampas Grass Bundle',      12.50, 40, 'Dried pampas grass for boho arrangements',       TRUE, 1),
    ('Wabi-Sabi Wall Mirror',   129.00,  6, 'Irregular-shaped mirror with wooden frame',      TRUE, 2),
    ('Aromatherapy Candle Set',  28.00, 25, 'Set of 3 soy wax candles, earthy scents',        TRUE, 4);

-- Insert some customers
INSERT INTO customers (first_name, last_name, email, phone) VALUES
    ('Ana',   'Berisha',  'ana.berisha@email.com',   '+355 69 111 2222'),
    ('Dion',  'Krasniqi', 'dion.krasniqi@email.com', '+355 69 333 4444'),
    ('Elsa',  'Marku',    'elsa.marku@email.com',    '+355 68 555 6666');