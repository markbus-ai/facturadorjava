package com.sfi.model;

import java.math.BigDecimal;

public class SinDescuento implements DescuentoStrategy {
    @Override
    public DiscountType getType() { return DiscountType.NONE; }

    @Override
    public BigDecimal getValue() { return BigDecimal.ZERO; }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }
}
