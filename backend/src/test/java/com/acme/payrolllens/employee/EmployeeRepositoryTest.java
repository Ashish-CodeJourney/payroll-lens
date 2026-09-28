package com.acme.payrolllens.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acme.payrolllens.salary.AnnualSalary;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void storesCurrentSalaryAndProfileForAnEmployee() {
        Employee employee = Employee.create("ACM-00001", "Priya Shah", "priya@example.com",
                "IN", "Engineering", "Software Engineer", "L3",
                new AnnualSalary(new BigDecimal("120000.00"), Currency.getInstance("INR")));

        repository.saveAndFlush(employee);
        entityManager.clear();

        Employee stored = repository.findByEmployeeNumber("ACM-00001").orElseThrow();
        assertEquals("Priya Shah", stored.getFullName());
        assertEquals("priya@example.com", stored.getEmail());
        assertEquals("IN", stored.getCountryCode());
        assertEquals("Engineering", stored.getDepartment());
        assertEquals("Software Engineer", stored.getJobTitle());
        assertEquals("L3", stored.getJobLevel());
        assertEquals(new BigDecimal("120000.00"), stored.getSalary().amount());
        assertEquals(Currency.getInstance("INR"), stored.getSalary().currency());
        assertFalse(stored.isArchived());
    }

    @Test
    void rejectsDuplicateEmployeeNumbers() {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("80000.00"), Currency.getInstance("USD"));
        repository.saveAndFlush(Employee.create("ACM-00001", "Alex Lee", "alex@example.com",
                "US", "Sales", "Manager", "L4", salary));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(Employee.create("ACM-00001", "Sam Lee", "sam@example.com",
                        "US", "Sales", "Manager", "L4", salary)));
    }

    @Test
    void rejectsDuplicateEmails() {
        AnnualSalary salary = new AnnualSalary(new BigDecimal("80000.00"), Currency.getInstance("USD"));
        repository.saveAndFlush(Employee.create("ACM-00001", "Alex Lee", "alex@example.com",
                "US", "Sales", "Manager", "L4", salary));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(Employee.create("ACM-00002", "Sam Lee", "alex@example.com",
                        "US", "Sales", "Manager", "L4", salary)));
    }
}
