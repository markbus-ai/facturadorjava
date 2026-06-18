package com.sfi.model;

import java.math.BigDecimal;

public class DescuentoNominal implements DescuentoStrategy {
    private final BigDecimal monto;

    public DescuentoNominal(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        this.monto = monto;
    }

    @Override
    public DiscountType getType() { return DiscountType.NOMINAL; }

    @Override
    public BigDecimal getValue() { return monto; }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (monto.compareTo(subtotal) > 0) {
            return subtotal;
        }
        return monto;
    }
}
