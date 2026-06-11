package com.sfi.model;

import java.math.BigDecimal;

public interface DescuentoStrategy {
    DiscountType getType();
    BigDecimal getValue();
    BigDecimal calculate(BigDecimal subtotal);
}
