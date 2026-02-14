CREATE TABLE IF NOT EXISTS suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER DEFAULT 0,
    price_netto DOUBLE PRECISION,
    price_brutto DOUBLE PRECISION,
    supplier_id INTEGER REFERENCES suppliers(id) ON DELETE SET NULL,
    order_number VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    product_name VARCHAR(255),
    quantity INTEGER,
    price_netto DOUBLE PRECISION,
    price_brutto DOUBLE PRECISION,
    order_number VARCHAR(50),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--dane na start
-- INSERT INTO suppliers (name, phone, email) VALUES ('Hurtownia Budowlana', '123456789', 'kontakt@hurt.pl');