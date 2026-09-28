package com.acme.payrolllens.employee;

import com.acme.payrolllens.salary.AnnualSalary;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_number", nullable = false)
    private String employeeNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(nullable = false)
    private String department;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "job_level", nullable = false)
    private String jobLevel;

    @Column(name = "annual_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal annualSalary;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private boolean archived;

    protected Employee() {
    }

    private Employee(String employeeNumber, String fullName, String email, String countryCode,
            String department, String jobTitle, String jobLevel, AnnualSalary salary) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.email = email;
        this.countryCode = countryCode;
        this.department = department;
        this.jobTitle = jobTitle;
        this.jobLevel = jobLevel;
        this.annualSalary = salary.amount();
        this.currencyCode = salary.currency().getCurrencyCode();
    }

    public static Employee create(String employeeNumber, String fullName, String email,
            String countryCode, String department, String jobTitle, String jobLevel,
            AnnualSalary salary) {
        return new Employee(employeeNumber, fullName, email, countryCode, department,
                jobTitle, jobLevel, salary);
    }

    public void update(String employeeNumber, String fullName, String email,
            String countryCode, String department, String jobTitle, String jobLevel,
            AnnualSalary salary) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.email = email;
        this.countryCode = countryCode;
        this.department = department;
        this.jobTitle = jobTitle;
        this.jobLevel = jobLevel;
        this.annualSalary = salary.amount();
        this.currencyCode = salary.currency().getCurrencyCode();
    }

    public Long getId() { return id; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCountryCode() { return countryCode; }
    public String getDepartment() { return department; }
    public String getJobTitle() { return jobTitle; }
    public String getJobLevel() { return jobLevel; }
    public AnnualSalary getSalary() {
        return new AnnualSalary(annualSalary, Currency.getInstance(currencyCode));
    }
    public boolean isArchived() { return archived; }
}
