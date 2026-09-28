package com.acme.payrolllens.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.payrolllens.salary.AnnualSalary;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Currency;
import org.springframework.http.MediaType;
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

    @Autowired
    private EntityManager entityManager;

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

    @Test
    void createsAnEmployeeWithCurrentAnnualSalary() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
                        {"employeeNumber":"ACM-20001","fullName":"Priya Shah",
                         "email":"priya.shah@example.com","countryCode":"IN",
                         "department":"Engineering","jobTitle":"Software Engineer",
                         "jobLevel":"L3","annualSalary":1200000.00,"currencyCode":"INR"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeNumber").value("ACM-20001"))
                .andExpect(jsonPath("$.currencyCode").value("INR"));

        assertEquals("Priya Shah", repository.findByEmployeeNumber("ACM-20001").orElseThrow().getFullName());
    }

    @Test
    void rejectsInvalidEmployeeFieldsBeforeSaving() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
                        {"employeeNumber":"ACM-20002","fullName":" ",
                         "email":"not-an-email","countryCode":"IN",
                         "department":"Engineering","jobTitle":"Software Engineer",
                         "jobLevel":"L3","annualSalary":0,"currencyCode":"INR"}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.annualSalary").exists());
    }

    @Test
    void rejectsCurrencyWithoutAReportingRate() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
                        {"employeeNumber":"ACM-20004","fullName":"Aki Sato",
                         "email":"aki@example.com","countryCode":"JP",
                         "department":"Engineering","jobTitle":"Engineer",
                         "jobLevel":"L3","annualSalary":12000000.00,"currencyCode":"JPY"}
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors.currencyCode").exists());
    }

    @Test
    void reportsDuplicateEmployeeIdentifiersAsConflict() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("85000.00"), Currency.getInstance("USD"));
        repository.saveAndFlush(Employee.create("ACM-20003", "Alex Lee", "alex@example.com",
                "US", "Engineering", "Software Engineer", "L2", salary));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("""
                        {"employeeNumber":"ACM-20003","fullName":"Sam Lee",
                         "email":"sam@example.com","countryCode":"US",
                         "department":"Engineering","jobTitle":"Software Engineer",
                         "jobLevel":"L2","annualSalary":90000.00,"currencyCode":"USD"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void returnsOneEmployeeById() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("95000.00"), Currency.getInstance("EUR"));
        Employee employee = repository.saveAndFlush(Employee.create("ACM-30001", "Elena Weber",
                "elena@example.com", "DE", "Finance", "Financial Analyst", "L3", salary));

        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Elena Weber"))
                .andExpect(jsonPath("$.annualSalary").value(95000.00))
                .andExpect(jsonPath("$.currencyCode").value("EUR"));
    }

    @Test
    void reportsAnUnknownEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void updatesEmployeeProfileAndCurrentSalary() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("75000.00"), Currency.getInstance("USD"));
        Employee employee = repository.saveAndFlush(Employee.create("ACM-30002", "Alex Lee",
                "alex.lee@example.com", "US", "Sales", "Account Executive", "L2", salary));

        mockMvc.perform(put("/api/employees/{id}", employee.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeNumber":"ACM-30002","fullName":"Alex Morgan",
                                 "email":"alex.morgan@example.com","countryCode":"US",
                                 "department":"Engineering","jobTitle":"Software Engineer",
                                 "jobLevel":"L3","annualSalary":110000.00,"currencyCode":"USD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alex Morgan"))
                .andExpect(jsonPath("$.annualSalary").value(110000.00));

        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.annualSalary").value(110000.00));
    }

    @Test
    void archivesAnEmployeeWithoutRemovingTheirRecord() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("80000.00"), Currency.getInstance("USD"));
        Employee employee = repository.saveAndFlush(Employee.create("ACM-30003", "Sam Lee",
                "sam.lee@example.com", "US", "People", "HR Partner", "L2", salary));

        mockMvc.perform(patch("/api/employees/{id}/archive", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
        mockMvc.perform(get("/api/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void filtersActiveAndArchivedEmployees() throws Exception {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("80000.00"), Currency.getInstance("USD"));
        Employee archived = repository.save(Employee.create("ACM-30004", "Sam Lee",
                "sam.lee2@example.com", "US", "People", "HR Partner", "L2", salary));
        repository.saveAndFlush(Employee.create("ACM-30005", "Alex Lee",
                "alex.lee2@example.com", "US", "People", "HR Partner", "L2", salary));
        mockMvc.perform(patch("/api/employees/{id}/archive", archived.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].employeeNumber").value("ACM-30005"));
        mockMvc.perform(get("/api/employees").param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].employeeNumber").value("ACM-30004"));
    }
}
