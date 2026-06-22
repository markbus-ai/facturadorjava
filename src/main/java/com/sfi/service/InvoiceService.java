package com.sfi.service;

import com.sfi.dao.ClientDAO;
import com.sfi.dao.InvoiceDAO;
import com.sfi.dao.ProductDAO;
import com.sfi.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceService {
    private final InvoiceDAO invoiceDAO;
    private final ProductDAO productDAO;
    private final ClientDAO clientDAO;

    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.productDAO = new ProductDAO();
        this.clientDAO = new ClientDAO();
    }

    public Invoice emitirFactura(Long userId, Long clientId, List<InvoiceItem> items, DescuentoStrategy descuento) {
        if (userId == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un ítem.");
        }
        if (clientId != null) {
            clientDAO.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado ID: " + clientId));
        }

        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setClientId(clientId);
        
        String number;
        int retries = 0;
        do {
            number = generarNumeroFactura();
            retries++;
            if (retries > 10) {
                throw new IllegalStateException("No se pudo generar un número de factura único.");
            }
        } while (invoiceDAO.existsInvoiceNumber(number));
        invoice.setInvoiceNumber(number);

        for (InvoiceItem item : items) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + item.getProductId()));
            if (product.getPrice() == null) {
                throw new IllegalArgumentException("El producto '" + product.getName() + "' no tiene precio asignado");
            }
            if (!product.isActive()) {
                throw new IllegalArgumentException("El producto '" + product.getName() + "' está inactivo y no se puede facturar.");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Stock insuficiente para: " + product.getName()
                        + " (disponible: " + product.getStock() + ", solicitado: " + item.getQuantity() + ")");
            }
            item.setUnitPrice(product.getPrice());
            item.setProductName(product.getName());
            
            BigDecimal subtotal = InvoiceCalculator.calculateItemSubtotal(item);
            item.setSubtotal(subtotal);
            
            invoice.addItem(item);
        }

        if (descuento == null) {
            throw new IllegalArgumentException("La estrategia de descuento es obligatoria");
        }
        invoice.applyDiscount(descuento);
        return invoiceDAO.save(invoice);
    }

    public List<Invoice> findAll() {
        return invoiceDAO.findAll();
    }

    public List<Invoice> myInvoices(Long userId) {
        return invoiceDAO.findByUserId(userId);
    }

    public void delete(Long id) {
        invoiceDAO.delete(id);
    }

    public BigDecimal getTotalFacturado() {
        return invoiceDAO.getTotalFacturado();
    }

    public long getCantidadFacturas() {
        return invoiceDAO.getCantidadFacturas();
    }

    private String generarNumeroFactura() {
        return "FAC-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + (int)(Math.random() * 1000);
    }
}
