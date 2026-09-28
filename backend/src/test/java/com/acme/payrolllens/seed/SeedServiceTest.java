package com.acme.payrolllens.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SeedServiceTest {
    @Autowired
    private SeedService seedService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedsExactlyTenThousandEmployeesAndIsSafeToRerun() {
        assertEquals(10_000, seedService.seed());
        assertEquals(10_000, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class));
        assertEquals(0, seedService.seed());
        assertEquals(10_000, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Integer.class));
    }
}
