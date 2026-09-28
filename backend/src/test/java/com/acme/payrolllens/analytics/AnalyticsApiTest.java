package com.acme.payrolllens.analytics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.payrolllens.employee.Employee;
import com.acme.payrolllens.employee.EmployeeRepository;
import com.acme.payrolllens.salary.AnnualSalary;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired EmployeeRepository repository;

    @Test
    void reportsActiveFilteredPayInDatedUsdAndCountryBreakdowns() throws Exception {
        Employee us = repository.save(employee("ACM-40001", "US", "Engineering", "L2", "USD", "100000.00"));
        repository.save(employee("ACM-40002", "DE", "Engineering", "L3", "EUR", "100000.00"));
        repository.save(employee("ACM-40003", "US", "Sales", "L2", "USD", "50000.00"));
        us.archive();
        repository.saveAndFlush(us);

        mockMvc.perform(get("/api/analytics").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headcount").value(1))
                .andExpect(jsonPath("$.reportingCurrency").value("USD"))
                .andExpect(jsonPath("$.rateDate").exists())
                .andExpect(jsonPath("$.annualTotalUsd").value(110000.00))
                .andExpect(jsonPath("$.medianAnnualUsd").value(110000.00))
                .andExpect(jsonPath("$.byCountry[0].label").value("DE"))
                .andExpect(jsonPath("$.byCountry[0].headcount").value(1));
    }

    private Employee employee(String number, String country, String department, String level,
            String currency, String amount) {
        return Employee.create(number, number, number.toLowerCase() + "@example.com", country,
                department, "Engineer", level,
                new AnnualSalary(new BigDecimal(amount), Currency.getInstance(currency)));
    }
}
