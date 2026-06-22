package com.sfi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private Long id;
    private String invoiceNumber;
    private Long userId;
    private String username;
    private Long clientId;
    private String clientName;
    private BigDecimal subtotal;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal taxableAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private LocalDateTime issuedAt;
    private List<InvoiceItem> items;

    public Invoice() {
        this.items = new ArrayList<>();
        this.discountType = DiscountType.NONE;
        this.discountValue = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxRate = InvoiceCalculator.TAX_RATE;
    }

    public Invoice(Long userId, String invoiceNumber) {
        this();
        this.userId = userId;
        this.invoiceNumber = invoiceNumber;
    }

    public void addItem(InvoiceItem item) {
        this.items.add(item);
    }

    public void applyDiscount(DescuentoStrategy strategy) {
        InvoiceCalculator.CalculationResult res = InvoiceCalculator.calculateInvoice(this.items, strategy, this.taxRate);
        this.subtotal = res.afterItemsSubtotal;
        this.discountType = strategy.getType();
        this.discountValue = strategy.getValue();
        this.discountAmount = res.globalDiscountAmount;
        this.taxableAmount = res.taxableAmount;
        this.taxAmount = res.taxAmount;
        this.total = res.total;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
