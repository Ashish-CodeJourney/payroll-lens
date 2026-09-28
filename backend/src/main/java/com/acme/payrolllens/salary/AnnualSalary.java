package com.acme.payrolllens.salary;

import java.math.BigDecimal;
import java.util.Currency;

public record AnnualSalary(BigDecimal amount, Currency currency) {
    public AnnualSalary {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Annual base salary must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Salary currency is required");
        }
    }
}
