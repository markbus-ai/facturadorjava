package com.sfi.dao;

import com.sfi.config.DatabaseConfig;
import com.sfi.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAO {

    public Optional<Supplier> findById(Long id) {
        String sql = "SELECT id, name, contact, phone, email, address, cuit FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar proveedor ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name, contact, phone, email, address, cuit FROM suppliers ORDER BY name";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) suppliers.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar proveedores", e);
        }
        return suppliers;
    }

    public void save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact, phone, email, address, cuit) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContact() != null ? supplier.getContact() : "");
            stmt.setString(3, supplier.getPhone() != null ? supplier.getPhone() : "");
            stmt.setString(4, supplier.getEmail() != null ? supplier.getEmail() : "");
            stmt.setString(5, supplier.getAddress() != null ? supplier.getAddress() : "");
            stmt.setString(6, supplier.getCuit() != null ? supplier.getCuit() : "");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) supplier.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar proveedor: " + supplier.getName(), e);
        }
    }

    public void update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, phone = ?, email = ?, address = ?, cuit = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContact() != null ? supplier.getContact() : "");
            stmt.setString(3, supplier.getPhone() != null ? supplier.getPhone() : "");
            stmt.setString(4, supplier.getEmail() != null ? supplier.getEmail() : "");
            stmt.setString(5, supplier.getAddress() != null ? supplier.getAddress() : "");
            stmt.setString(6, supplier.getCuit() != null ? supplier.getCuit() : "");
            stmt.setLong(7, supplier.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar proveedor ID: " + supplier.getId(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar proveedor ID: " + id, e);
        }
    }

    public boolean hasProducts(Long id) {
        String sql = "SELECT COUNT(*) FROM products WHERE supplier_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar productos del proveedor ID: " + id, e);
        }
        return false;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setContact(rs.getString("contact"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setCuit(rs.getString("cuit"));
        return s;
    }
}
