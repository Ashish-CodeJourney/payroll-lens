package com.acme.payrolllens.seed;

import jakarta.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeedService {
    private final JdbcTemplate jdbcTemplate;

    public SeedService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int seed() {
        Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class);
        if (existing != null && existing > 0) {
            return 0;
        }

        List<SeedEmployee> employees = SeedEmployees.generate();
        jdbcTemplate.batchUpdate("""
                INSERT INTO employees (employee_number, full_name, email, country_code,
                    department, job_title, job_level, annual_salary, currency_code, archived)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                SeedEmployee employee = employees.get(index);
                statement.setString(1, employee.employeeNumber());
                statement.setString(2, employee.fullName());
                statement.setString(3, employee.email());
                statement.setString(4, employee.countryCode());
                statement.setString(5, employee.department());
                statement.setString(6, employee.jobTitle());
                statement.setString(7, employee.jobLevel());
                statement.setBigDecimal(8, employee.annualSalary());
                statement.setString(9, employee.currencyCode());
            }

            @Override
            public int getBatchSize() {
                return employees.size();
            }
        });
        return employees.size();
    }
}
