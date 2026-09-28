package com.acme.payrolllens.analytics;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsReport(String reportingCurrency, String rateDate, long headcount,
        BigDecimal annualTotalUsd, BigDecimal medianAnnualUsd, List<Breakdown> byCountry,
        List<Breakdown> byDepartment, List<Breakdown> byLevel, List<DistributionBand> distribution) {
    public record Breakdown(String label, long headcount, BigDecimal annualTotalUsd,
            BigDecimal medianAnnualUsd) { }

    public record DistributionBand(String label, long headcount) { }
}
