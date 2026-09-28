package com.acme.payrolllens.employee;

import java.math.BigDecimal;

public record EmployeeView(Long id, String employeeNumber, String fullName, String email,
        String countryCode, String department, String jobTitle, String jobLevel,
        BigDecimal annualSalary, String currencyCode, boolean archived) {
    public static EmployeeView from(Employee employee) {
        return new EmployeeView(employee.getId(), employee.getEmployeeNumber(),
                employee.getFullName(), employee.getEmail(), employee.getCountryCode(),
                employee.getDepartment(), employee.getJobTitle(), employee.getJobLevel(),
                employee.getSalary().amount(), employee.getSalary().currency().getCurrencyCode(),
                employee.isArchived());
    }
}
