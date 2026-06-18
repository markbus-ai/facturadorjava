package com.sfi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescuentoPorcentual implements DescuentoStrategy {
    private final BigDecimal porcentaje;

    public DescuentoPorcentual(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.porcentaje = porcentaje;
    }

    @Override
    public DiscountType getType() { return DiscountType.PERCENTAGE; }

    @Override
    public BigDecimal getValue() { return porcentaje; }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        return subtotal.multiply(porcentaje)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
