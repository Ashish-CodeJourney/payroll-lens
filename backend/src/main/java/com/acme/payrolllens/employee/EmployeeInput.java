package com.acme.payrolllens.employee;

import com.acme.payrolllens.salary.AnnualSalary;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Currency;

public record EmployeeInput(@NotBlank String employeeNumber, @NotBlank String fullName,
        @NotBlank @Email String email, @NotBlank @Pattern(regexp = "[A-Z]{2}") String countryCode,
        @NotBlank String department, @NotBlank String jobTitle,
        @NotBlank @Pattern(regexp = "L[1-5]") String jobLevel,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal annualSalary,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currencyCode) {
    public Employee toEmployee() {
        return Employee.create(employeeNumber, fullName, email, countryCode,
                department, jobTitle, jobLevel,
                new AnnualSalary(annualSalary, Currency.getInstance(currencyCode)));
    }
}
