package warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/warehouse_erp";
    private static final String USER = "NAME_USERDB";
    private static final String PASSWORD = "YOUR_PASSWORD";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Metoda inicjalizująca bazę danych.
     * Tworzy tabele, jeśli nie istnieją w PostgreSQL.
     */
    public void initializeDatabase() {
        String createSuppliers = "CREATE TABLE IF NOT EXISTS suppliers (" +
                "id SERIAL PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "phone TEXT," +
                "email TEXT" +
                ");";

        String createProducts = "CREATE TABLE IF NOT EXISTS products (" +
                "id SERIAL PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "quantity INTEGER DEFAULT 0," +
                "price_netto DOUBLE PRECISION," +
                "price_brutto DOUBLE PRECISION," +
                "supplier_id INTEGER REFERENCES suppliers(id) ON DELETE SET NULL," +
                "order_number TEXT" +
                ");";

        String createOrders = "CREATE TABLE IF NOT EXISTS orders (" +
                "id SERIAL PRIMARY KEY," +
                "product_name TEXT," +
                "quantity INTEGER," +
                "price_netto DOUBLE PRECISION," +
                "price_brutto DOUBLE PRECISION," +
                "order_number TEXT," +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSuppliers);
            stmt.execute(createProducts);
            stmt.execute(createOrders);

            System.out.println("Baza danych została zainicjalizowana pomyślnie.");

        } catch (SQLException e) {
            System.err.println("Błąd podczas inicjalizacji bazy: " + e.getMessage());
        }
    }

    // --- SEKCJA PRODUKTY ---

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.name as s_name FROM products p " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.id ORDER BY p.id";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                products.add(new Product(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getInt("quantity"),
                        result.getDouble("price_netto"),
                        result.getDouble("price_brutto"),
                        result.getInt("supplier_id"),
                        result.getString("s_name"),
                        result.getString("order_number")
                ));
            }
        }
        return products;
    }

    public void addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, quantity, price_netto, price_brutto, supplier_id, order_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement(sql)) {

            pstatement.setString(1, p.getName());
            pstatement.setInt(2, p.getQuantity());
            pstatement.setDouble(3, p.getPriceNetto());
            pstatement.setDouble(4, p.getPriceBrutto());
            pstatement.setInt(5, p.getSupplierId());
            pstatement.setString(6, p.getOrderNumber());
            pstatement.executeUpdate();
        }
    }

    public void updateProduct(Product p) throws SQLException {
        String sql = "UPDATE products SET name = ?, quantity = ?, price_netto = ?, price_brutto = ? WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement(sql)) {
            pstatement.setString(1, p.getName());
            pstatement.setInt(2, p.getQuantity());
            pstatement.setDouble(3, p.getPriceNetto());
            pstatement.setDouble(4, p.getPriceBrutto());
            pstatement.setInt(5, p.getId());
            pstatement.executeUpdate();
        }
    }

    // --- SEKCJA DOSTAWCY ---

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY id";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }
        }
        return suppliers;
    }

    public void addSupplier(String name, String phone, String email) throws SQLException {
        String sql = "INSERT INTO suppliers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
        }
    }

    public void updateSupplier(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement(sql)) {
            pstatement.setString(1, s.getName());
            pstatement.setString(2, s.getPhone());
            pstatement.setString(3, s.getEmail());
            pstatement.setInt(4, s.getId());
            pstatement.executeUpdate();
        }
    }

    // --- SEKCJA ZAMÓWIENIA ---

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                orders.add(new Order(
                        result.getInt("id"),
                        result.getString("product_name"),
                        result.getInt("quantity"),
                        result.getDouble("price_netto"),
                        result.getDouble("price_brutto"),
                        result.getString("order_number"),
                        result.getString("order_date")
                ));
            }
        }
        return orders;
    }

    public void addOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (product_name, quantity, price_netto, price_brutto, order_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, order.getProductName());
            pstmt.setInt(2, order.getQuantity());
            pstmt.setDouble(3, order.getPriceNetto());
            pstmt.setDouble(4, order.getPriceBrutto());
            pstmt.setString(5, order.getOrderNumber());
            pstmt.executeUpdate();
        }
    }

    // --- SEKCJA LOGIKI I USUWANIA ---

    public void updateProductQuantity(String productName, int quantityToSubstract) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE name = ?";
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement(sql)) {
            pstatement.setInt(1, quantityToSubstract);
            pstatement.setString(2, productName);
            pstatement.executeUpdate();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            pstatement.setInt(1, id);
            pstatement.executeUpdate();
        }
    }

    public void deleteSupplier(int id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement("DELETE FROM suppliers WHERE id = ?")) {
            pstatement.setInt(1, id);
            pstatement.executeUpdate();
        }
    }

    public void deleteOrder(int id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement pstatement = connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            pstatement.setInt(1, id);
            pstatement.executeUpdate();
        }
    }
}