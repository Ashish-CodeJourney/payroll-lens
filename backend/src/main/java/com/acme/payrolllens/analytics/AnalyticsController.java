package com.acme.payrolllens.analytics;

import com.acme.payrolllens.employee.EmployeeRepository;
import com.acme.payrolllens.employee.EmployeeSpecifications;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final EmployeeRepository repository;
    private final AnalyticsCalculator calculator;

    public AnalyticsController(EmployeeRepository repository, AnalyticsCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    @GetMapping
    @Operation(summary = "Report active employee pay in USD",
            description = "Filter active employees and convert local annual base salaries to USD "
                    + "using the fixed 2026-01-01 rate snapshot. Returns headcount, totals, median, "
                    + "breakdowns, and distribution.")
    public AnalyticsReport report(@RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level) {
        return calculator.calculate(repository.findAll(
                EmployeeSpecifications.matching(query, country, department, level, "ACTIVE")));
    }
}
