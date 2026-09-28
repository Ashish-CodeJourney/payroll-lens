package com.acme.payrolllens.seed;

import java.math.BigDecimal;

public record SeedEmployee(String employeeNumber, String fullName, String email,
        String countryCode, String department, String jobTitle, String jobLevel,
        BigDecimal annualSalary, String currencyCode) {
}
