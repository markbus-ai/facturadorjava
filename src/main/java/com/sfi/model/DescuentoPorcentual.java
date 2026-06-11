package com.sfi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DescuentoPorcentual implements DescuentoStrategy {
    private final BigDecimal porcentaje;

    public DescuentoPorcentual(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.porcentaje = BigDecimal.valueOf(porcentaje);
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
