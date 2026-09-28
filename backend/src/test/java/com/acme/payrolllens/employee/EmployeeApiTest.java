package com.acme.payrolllens.employee;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class EmployeeApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository repository;

    @Test
    void returnsAStablePageOfEmployees() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("85000.00"), Currency.getInstance("USD"));
        repository.save(Employee.create("ACM-00003", "Charlie Chen", "charlie@example.com",
                "US", "Sales", "Account Executive", "L2", salary));
        repository.save(Employee.create("ACM-00001", "Alex Lee", "alex@example.com",
                "US", "Engineering", "Software Engineer", "L1", salary));
        repository.saveAndFlush(Employee.create("ACM-00002", "Blair Shah", "blair@example.com",
                "US", "Finance", "Financial Analyst", "L3", salary));

        mockMvc.perform(get("/api/employees").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.items[0].employeeNumber").value("ACM-00002"))
                .andExpect(jsonPath("$.items[0].annualSalary").value(85000.00))
                .andExpect(jsonPath("$.items[0].currencyCode").value("USD"));
    }

    @Test
    void combinesSearchCountryDepartmentAndLevelFilters() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("85000.00"), Currency.getInstance("USD"));
        repository.save(Employee.create("ACM-00011", "Priya Shah", "priya.in@example.com",
                "IN", "Engineering", "Software Engineer", "L3", salary));
        repository.save(Employee.create("ACM-00012", "Maya Shah", "maya.us@example.com",
                "US", "Engineering", "Software Engineer", "L3", salary));
        repository.save(Employee.create("ACM-00013", "Asha Shah", "asha.in@example.com",
                "IN", "Sales", "Account Executive", "L3", salary));
        repository.saveAndFlush(Employee.create("ACM-00014", "Sam Shah", "sam.in@example.com",
                "IN", "Engineering", "Software Engineer", "L2", salary));

        mockMvc.perform(get("/api/employees").param("query", "SHAH")
                        .param("country", "IN").param("department", "Engineering")
                        .param("level", "L3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].employeeNumber").value("ACM-00011"));
    }

    @Test
    void searchesEmployeeNumberAndEmailAsWellAsName() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("85000.00"), Currency.getInstance("USD"));
        repository.saveAndFlush(Employee.create("ACM-04567", "Jordan Lee", "jordan.lee@example.com",
                "US", "Engineering", "Software Engineer", "L2", salary));

        mockMvc.perform(get("/api/employees").param("query", "04567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/employees").param("query", "JORDAN.LEE@"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void capsPageSizeAtOneHundred() throws Exception {
        mockMvc.perform(get("/api/employees").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void rejectsInvalidPagingInputsWithAConsistentError() throws Exception {
        mockMvc.perform(get("/api/employees").param("page", "-1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/employees").param("size", "0"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
