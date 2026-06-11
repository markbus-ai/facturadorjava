package com.sfi.service;

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

    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.productDAO = new ProductDAO();
    }

    public Invoice emitirFactura(Long userId, Long clientId, List<InvoiceItem> items, DescuentoStrategy descuento) {
        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setClientId(clientId);
        invoice.setInvoiceNumber(generarNumeroFactura());

        for (InvoiceItem item : items) {
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + item.getProductId()));
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Stock insuficiente para: " + product.getName()
                        + " (disponible: " + product.getStock() + ", solicitado: " + item.getQuantity() + ")");
            }
            item.setUnitPrice(product.getPrice());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            if (item.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal disc = lineTotal.multiply(item.getDiscountValue()).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                item.setSubtotal(lineTotal.subtract(disc));
            } else if (item.getDiscountType() == DiscountType.NOMINAL) {
                BigDecimal disc = item.getDiscountValue().min(lineTotal);
                item.setSubtotal(lineTotal.subtract(disc));
            } else {
                item.setSubtotal(lineTotal);
            }

            item.setProductName(product.getName());
            invoice.addItem(item);
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
