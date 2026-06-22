package com.sfi.dao;

import com.sfi.config.DatabaseConfig;
import com.sfi.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    public Optional<Product> findById(Long id) {
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por ID: " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Product> findByCode(String code) {
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto por codigo: " + code, e);
        }
        return Optional.empty();
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id ORDER BY p.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos", e);
        }
        return products;
    }

    public List<Product> findAllActive() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.active = TRUE ORDER BY p.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos activos", e);
        }
        return products;
    }

    public List<Product> findAllActiveWithStock() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.active = TRUE AND p.stock > 0 ORDER BY p.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos activos con stock", e);
        }
        return products;
    }

    public List<Product> findLowStock() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, s.name as supplier_name FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.id WHERE p.active = TRUE AND p.stock < p.min_stock ORDER BY p.name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos con stock bajo", e);
        }
        return products;
    }

    public void save(Product product) {
        String sql = "INSERT INTO products (code, name, description, price, stock, min_stock, supplier_id, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getMinStock());
            if (product.getSupplierId() != null) stmt.setLong(7, product.getSupplierId());
            else stmt.setNull(7, Types.BIGINT);
            stmt.setBoolean(8, product.isActive());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) product.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto: " + product.getCode(), e);
        }
    }

    public void update(Product product) {
        String sql = "UPDATE products SET code = ?, name = ?, description = ?, price = ?, stock = ?, min_stock = ?, supplier_id = ?, active = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getMinStock());
            if (product.getSupplierId() != null) stmt.setLong(7, product.getSupplierId());
            else stmt.setNull(7, Types.BIGINT);
            stmt.setBoolean(8, product.isActive());
            stmt.setLong(9, product.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto ID: " + product.getId(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto ID: " + id, e);
        }
    }

    public boolean hasInvoiceItems(Long productId) {
        String sql = "SELECT COUNT(*) FROM invoice_items WHERE product_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar items de factura del producto ID: " + productId, e);
        }
        return false;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setSupplierId(rs.getObject("supplier_id", Long.class));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());
        return p;
    }
}
