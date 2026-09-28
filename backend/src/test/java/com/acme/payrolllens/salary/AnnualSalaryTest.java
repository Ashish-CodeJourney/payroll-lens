package com.acme.payrolllens.salary;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class AnnualSalaryTest {
    @Test
    void rejectsZeroAnnualBaseSalary() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnnualSalary(BigDecimal.ZERO, Currency.getInstance("USD")));
    }

    @Test
    void rejectsNegativeAnnualBaseSalary() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnnualSalary(new BigDecimal("-0.01"), Currency.getInstance("USD")));
    }

    @Test
    void preservesValidAmountAndCurrency() {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("125000.50"), Currency.getInstance("EUR"));

        assertEquals(new BigDecimal("125000.50"), salary.amount());
        assertEquals(Currency.getInstance("EUR"), salary.currency());
    }

    @Test
    void rejectsMissingAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnnualSalary(null, Currency.getInstance("USD")));
    }

    @Test
    void rejectsMissingCurrency() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnnualSalary(new BigDecimal("100000"), null));
    }
}
