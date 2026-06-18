package com.sfi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InvoiceCalculator {

    public static class CalculationResult {
        public final BigDecimal rawSubtotal;       // Sum of qty * unitPrice
        public final BigDecimal totalItemDiscount; // Sum of item discounts
        public final BigDecimal afterItemsSubtotal; // rawSubtotal - totalItemDiscount
        public final BigDecimal globalDiscountAmount;
        public final BigDecimal taxableAmount;
        public final BigDecimal taxAmount;
        public final BigDecimal total;
        public final BigDecimal totalDiscount;    // totalItemDiscount + globalDiscountAmount

        public CalculationResult(BigDecimal rawSubtotal, BigDecimal totalItemDiscount, BigDecimal afterItemsSubtotal,
                                 BigDecimal globalDiscountAmount, BigDecimal taxableAmount, BigDecimal taxAmount,
                                 BigDecimal total, BigDecimal totalDiscount) {
            this.rawSubtotal = rawSubtotal;
            this.totalItemDiscount = totalItemDiscount;
            this.afterItemsSubtotal = afterItemsSubtotal;
            this.globalDiscountAmount = globalDiscountAmount;
            this.taxableAmount = taxableAmount;
            this.taxAmount = taxAmount;
            this.total = total;
            this.totalDiscount = totalDiscount;
        }
    }

    public static BigDecimal calculateItemSubtotal(InvoiceItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal lineDiscount = calculateItemDiscount(item);
        return lineTotal.subtract(lineDiscount);
    }

    public static BigDecimal calculateItemDiscount(InvoiceItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        DiscountType dtype = item.getDiscountType();
        BigDecimal dvalue = item.getDiscountValue();
        if (dtype == null || dvalue == null || dtype == DiscountType.NONE) {
            return BigDecimal.ZERO;
        }
        if (dtype == DiscountType.PERCENTAGE) {
            return lineTotal.multiply(dvalue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (dtype == DiscountType.NOMINAL) {
            return dvalue.min(lineTotal);
        }
        return BigDecimal.ZERO;
    }

    public static CalculationResult calculateInvoice(List<InvoiceItem> items, DescuentoStrategy globalDiscount, BigDecimal taxRate) {
        BigDecimal rawSubtotal = BigDecimal.ZERO;
        BigDecimal totalItemDiscount = BigDecimal.ZERO;

        for (InvoiceItem item : items) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            rawSubtotal = rawSubtotal.add(lineTotal);

            BigDecimal lineDiscount = calculateItemDiscount(item);
            totalItemDiscount = totalItemDiscount.add(lineDiscount);
            
            item.setSubtotal(lineTotal.subtract(lineDiscount));
        }

        BigDecimal afterItemsSubtotal = rawSubtotal.subtract(totalItemDiscount);
        BigDecimal globalDiscountAmount = globalDiscount.calculate(afterItemsSubtotal);
        BigDecimal taxableAmount = afterItemsSubtotal.subtract(globalDiscountAmount);
        
        BigDecimal divisor = new BigDecimal("100");
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal total = taxableAmount.add(taxAmount);
        BigDecimal totalDiscount = totalItemDiscount.add(globalDiscountAmount);

        return new CalculationResult(rawSubtotal, totalItemDiscount, afterItemsSubtotal, globalDiscountAmount,
                taxableAmount, taxAmount, total, totalDiscount);
    }
}
