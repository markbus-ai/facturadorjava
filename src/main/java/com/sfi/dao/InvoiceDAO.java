package com.sfi.dao;

import com.sfi.config.DatabaseConfig;
import com.sfi.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDAO {

    public Optional<Invoice> findById(Long id) {
        String sql = "SELECT i.*, u.username, c.name AS client_name FROM invoices i "
                + "JOIN users u ON i.user_id = u.id "
                + "LEFT JOIN clients c ON i.client_id = c.id WHERE i.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapInvoice(rs);
                    invoice.setItems(findItemsByInvoiceId(conn, id));
                    return Optional.of(invoice);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar factura ID: " + id, e);
        }
        return Optional.empty();
    }

    public List<Invoice> findAll() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, u.username, c.name AS client_name FROM invoices i "
                + "JOIN users u ON i.user_id = u.id "
                + "LEFT JOIN clients c ON i.client_id = c.id ORDER BY i.issued_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Invoice invoice = mapInvoice(rs);
                invoice.setItems(findItemsByInvoiceId(conn, invoice.getId()));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar facturas", e);
        }
        return invoices;
    }

    public List<Invoice> findByUserId(Long userId) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, u.username, c.name AS client_name FROM invoices i "
                + "JOIN users u ON i.user_id = u.id "
                + "LEFT JOIN clients c ON i.client_id = c.id WHERE i.user_id = ? ORDER BY i.issued_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = mapInvoice(rs);
                    invoice.setItems(findItemsByInvoiceId(conn, invoice.getId()));
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar facturas del usuario " + userId, e);
        }
        return invoices;
    }

    public void delete(Long id) {
        String sqlSelectItems = "SELECT product_id, quantity FROM invoice_items WHERE invoice_id = ?";
        String sqlUpdateStock = "UPDATE products SET stock = stock + ? WHERE id = ?";
        String sqlDeleteInvoice = "DELETE FROM invoices WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get invoice items first
                List<InvoiceItem> items = new ArrayList<>();
                try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectItems)) {
                    stmtSelect.setLong(1, id);
                    try (ResultSet rs = stmtSelect.executeQuery()) {
                        while (rs.next()) {
                            InvoiceItem item = new InvoiceItem();
                            item.setProductId(rs.getLong("product_id"));
                            item.setQuantity(rs.getInt("quantity"));
                            items.add(item);
                        }
                    }
                }
                
                // Restore stock
                try (PreparedStatement stmtStock = conn.prepareStatement(sqlUpdateStock)) {
                    for (InvoiceItem item : items) {
                        stmtStock.setInt(1, item.getQuantity());
                        stmtStock.setLong(2, item.getProductId());
                        stmtStock.addBatch();
                    }
                    stmtStock.executeBatch();
                }
                
                // Delete invoice (will cascade delete items)
                try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteInvoice)) {
                    stmtDelete.setLong(1, id);
                    stmtDelete.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al eliminar factura y restaurar stock, transaccion revertida: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexion al eliminar factura ID: " + id, e);
        }
    }

    public Invoice save(Invoice invoice) {
        String sqlInvoice = "INSERT INTO invoices (invoice_number, user_id, client_id, subtotal, discount_type, discount_value, "
                + "discount_amount, taxable_amount, tax_rate, tax_amount, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO invoice_items (invoice_id, product_id, quantity, unit_price, subtotal, discount_type, discount_value) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlSelectStock = "SELECT stock, name FROM products WHERE id = ? FOR UPDATE";
        String sqlUpdateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long invoiceId;
                try (PreparedStatement stmt = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, invoice.getInvoiceNumber());
                    stmt.setLong(2, invoice.getUserId());
                    if (invoice.getClientId() != null) {
                        stmt.setLong(3, invoice.getClientId());
                    } else {
                        stmt.setNull(3, Types.BIGINT);
                    }
                    stmt.setBigDecimal(4, invoice.getSubtotal());
                    stmt.setString(5, invoice.getDiscountType().name());
                    stmt.setBigDecimal(6, invoice.getDiscountValue());
                    stmt.setBigDecimal(7, invoice.getDiscountAmount());
                    stmt.setBigDecimal(8, invoice.getTaxableAmount());
                    stmt.setBigDecimal(9, invoice.getTaxRate());
                    stmt.setBigDecimal(10, invoice.getTaxAmount());
                    stmt.setBigDecimal(11, invoice.getTotal());
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo obtener el ID generado para la factura");
                        }
                        invoiceId = keys.getLong(1);
                    }
                }

                try (PreparedStatement stmtSelectStock = conn.prepareStatement(sqlSelectStock);
                     PreparedStatement stmtStock = conn.prepareStatement(sqlUpdateStock);
                     PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {

                    for (InvoiceItem item : invoice.getItems()) {
                        // Check stock FOR UPDATE
                        stmtSelectStock.setLong(1, item.getProductId());
                        try (ResultSet rs = stmtSelectStock.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException("Producto no encontrado ID: " + item.getProductId());
                            }
                            int currentStock = rs.getInt("stock");
                            String prodName = rs.getString("name");
                            if (currentStock < item.getQuantity()) {
                                throw new SQLException("Stock insuficiente para el producto: " + prodName + " (Disponible: " + currentStock + ", Solicitado: " + item.getQuantity() + ")");
                            }
                        }

                        // Insert invoice item
                        stmtItem.setLong(1, invoiceId);
                        stmtItem.setLong(2, item.getProductId());
                        stmtItem.setInt(3, item.getQuantity());
                        stmtItem.setBigDecimal(4, item.getUnitPrice());
                        stmtItem.setBigDecimal(5, item.getSubtotal());
                        stmtItem.setString(6, item.getDiscountType().name());
                        stmtItem.setBigDecimal(7, item.getDiscountValue());
                        stmtItem.executeUpdate();

                        // Deduct stock
                        stmtStock.setInt(1, item.getQuantity());
                        stmtStock.setLong(2, item.getProductId());
                        stmtStock.executeUpdate();
                    }
                }

                conn.commit();
                invoice.setId(invoiceId);
                return invoice;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error al emitir factura, transaccion revertida: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexion al emitir factura", e);
        }
    }

    public boolean existsInvoiceNumber(String invoiceNumber) {
        String sql = "SELECT COUNT(*) FROM invoices WHERE invoice_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, invoiceNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar número de factura: " + invoiceNumber, e);
        }
        return false;
    }

    public BigDecimal getTotalFacturado() {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM invoices";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener total facturado", e);
        }
        return BigDecimal.ZERO;
    }

    public long getCantidadFacturas() {
        String sql = "SELECT COUNT(*) FROM invoices";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar facturas", e);
        }
        return 0;
    }

    private List<InvoiceItem> findItemsByInvoiceId(Connection conn, Long invoiceId) throws SQLException {
        List<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT ii.*, p.name AS product_name FROM invoice_items ii "
                + "JOIN products p ON ii.product_id = p.id WHERE ii.invoice_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getLong("id"));
                    item.setInvoiceId(rs.getLong("invoice_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
                    item.setDiscountValue(rs.getBigDecimal("discount_value"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong("id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setUserId(rs.getLong("user_id"));
        invoice.setUsername(rs.getString("username"));
        long clientId = rs.getLong("client_id");
        if (!rs.wasNull()) {
            invoice.setClientId(clientId);
            invoice.setClientName(rs.getString("client_name"));
        }
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
        invoice.setDiscountValue(rs.getBigDecimal("discount_value"));
        invoice.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        invoice.setTaxableAmount(rs.getBigDecimal("taxable_amount"));
        invoice.setTaxRate(rs.getBigDecimal("tax_rate"));
        invoice.setTaxAmount(rs.getBigDecimal("tax_amount"));
        invoice.setTotal(rs.getBigDecimal("total"));
        invoice.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
        return invoice;
    }
}
