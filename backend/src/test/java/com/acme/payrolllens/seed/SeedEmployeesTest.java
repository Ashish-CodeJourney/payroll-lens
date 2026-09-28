package com.acme.payrolllens.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SeedEmployeesTest {
    @Test
    void createsTenThousandRepeatableSyntheticEmployees() {
        List<SeedEmployee> employees = SeedEmployees.generate();

        assertEquals(10_000, employees.size());
        assertEquals("ACM-00001", employees.getFirst().employeeNumber());
        assertEquals("ACM-10000", employees.getLast().employeeNumber());
        assertEquals(10_000, employees.stream().map(SeedEmployee::employeeNumber).collect(Collectors.toSet()).size());
        assertEquals(10_000, employees.stream().map(SeedEmployee::email).collect(Collectors.toSet()).size());
        assertEquals(employees, SeedEmployees.generate());

        Map<String, String> currencies = Map.of("US", "USD", "IN", "INR", "GB", "GBP",
                "DE", "EUR", "CA", "CAD");
        assertEquals(currencies.keySet(), employees.stream().map(SeedEmployee::countryCode).collect(Collectors.toSet()));
        assertEquals(Set.of("Engineering", "Sales", "Operations", "Finance", "People"),
                employees.stream().map(SeedEmployee::department).collect(Collectors.toSet()));
        assertTrue(employees.stream().allMatch(employee ->
                currencies.get(employee.countryCode()).equals(employee.currencyCode())
                        && employee.annualSalary().signum() > 0
                        && !employee.fullName().isBlank()
                        && !employee.jobTitle().isBlank()
                        && !employee.jobLevel().isBlank()));
    }
}
