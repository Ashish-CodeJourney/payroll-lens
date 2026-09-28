package com.acme.payrolllens.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acme.payrolllens.employee.Employee;
import com.acme.payrolllens.salary.AnnualSalary;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyticsCalculatorTest {
    private final AnalyticsCalculator calculator = new AnalyticsCalculator();

    @Test
    void emptyPopulationHasZeroMeasuresAndNoBreakdowns() {
        AnalyticsReport report = calculator.calculate(List.of());
        assertEquals(0, report.headcount());
        assertEquals(new BigDecimal("0.00"), report.annualTotalUsd());
        assertEquals(new BigDecimal("0.00"), report.medianAnnualUsd());
        assertEquals(List.of(), report.byCountry());
        assertEquals(0, report.distribution().stream().mapToLong(AnalyticsReport.DistributionBand::headcount).sum());
    }

    @Test
    void evenPopulationUsesMidpointAndPlacesBandBoundariesPrecisely() {
        AnalyticsReport report = calculator.calculate(List.of(
                employee("USD", "49999.99"), employee("USD", "50000.00"),
                employee("USD", "100000.00"), employee("USD", "150000.00")));
        assertEquals(new BigDecimal("349999.99"), report.annualTotalUsd());
        assertEquals(new BigDecimal("75000.00"), report.medianAnnualUsd());
        assertEquals(List.of(1L, 1L, 1L, 1L), report.distribution().stream()
                .map(AnalyticsReport.DistributionBand::headcount).toList());
    }

    @Test
    void unknownCurrencyCannotBeSilentlyAddedToUsd() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(List.of(employee("JPY", "100000"))));
        assertEquals("No USD reporting rate for currency JPY", error.getMessage());
    }

    private Employee employee(String currency, String amount) {
        return Employee.create("ACM-50001", "Alex Lee", "alex@example.com", "US",
                "Engineering", "Engineer", "L2",
                new AnnualSalary(new BigDecimal(amount), Currency.getInstance(currency)));
    }
}
