package com.acme.payrolllens.analytics;

import com.acme.payrolllens.employee.Employee;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsCalculator {
    public static final String RATE_DATE = "2026-01-01";
    private static final Map<String, BigDecimal> USD_PER_UNIT = Map.of(
            "USD", new BigDecimal("1.00"),
            "EUR", new BigDecimal("1.10"),
            "GBP", new BigDecimal("1.25"),
            "INR", new BigDecimal("0.012"),
            "CAD", new BigDecimal("0.74"));
    private static final BigDecimal FIFTY_THOUSAND = new BigDecimal("50000");
    private static final BigDecimal HUNDRED_THOUSAND = new BigDecimal("100000");
    private static final BigDecimal HUNDRED_FIFTY_THOUSAND = new BigDecimal("150000");

    public AnalyticsReport calculate(List<Employee> employees) {
        List<PayRow> rows = employees.stream().filter(employee -> !employee.isArchived())
                .map(employee -> new PayRow(employee, toUsd(employee)))
                .toList();
        return new AnalyticsReport("USD", RATE_DATE, rows.size(), total(rows), median(rows),
                breakdown(rows, Employee::getCountryCode),
                breakdown(rows, Employee::getDepartment),
                breakdown(rows, Employee::getJobLevel), distribution(rows));
    }

    private BigDecimal toUsd(Employee employee) {
        String currency = employee.getSalary().currency().getCurrencyCode();
        BigDecimal rate = USD_PER_UNIT.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("No USD reporting rate for currency " + currency);
        }
        return employee.getSalary().amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal total(List<PayRow> rows) {
        return rows.stream().map(PayRow::usd).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    private BigDecimal median(List<PayRow> rows) {
        if (rows.isEmpty()) {
            return new BigDecimal("0.00");
        }
        List<BigDecimal> values = rows.stream().map(PayRow::usd).sorted().toList();
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        }
        return values.get(middle - 1).add(values.get(middle))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private List<AnalyticsReport.Breakdown> breakdown(List<PayRow> rows,
            Function<Employee, String> label) {
        Map<String, List<PayRow>> groups = rows.stream()
                .collect(Collectors.groupingBy(row -> label.apply(row.employee())));
        return groups.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new AnalyticsReport.Breakdown(entry.getKey(), entry.getValue().size(),
                        total(entry.getValue()), median(entry.getValue())))
                .toList();
    }

    private List<AnalyticsReport.DistributionBand> distribution(List<PayRow> rows) {
        long under50 = rows.stream().filter(row -> row.usd().compareTo(FIFTY_THOUSAND) < 0).count();
        long from50 = rows.stream().filter(row -> row.usd().compareTo(FIFTY_THOUSAND) >= 0
                && row.usd().compareTo(HUNDRED_THOUSAND) < 0).count();
        long from100 = rows.stream().filter(row -> row.usd().compareTo(HUNDRED_THOUSAND) >= 0
                && row.usd().compareTo(HUNDRED_FIFTY_THOUSAND) < 0).count();
        long from150 = rows.stream().filter(row -> row.usd().compareTo(HUNDRED_FIFTY_THOUSAND) >= 0).count();
        return List.of(new AnalyticsReport.DistributionBand("Under $50k", under50),
                new AnalyticsReport.DistributionBand("$50k–$99,999", from50),
                new AnalyticsReport.DistributionBand("$100k–$149,999", from100),
                new AnalyticsReport.DistributionBand("$150k+", from150));
    }

    private record PayRow(Employee employee, BigDecimal usd) { }
}
