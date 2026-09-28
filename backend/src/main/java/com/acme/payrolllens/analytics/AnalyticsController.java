package com.acme.payrolllens.analytics;

import com.acme.payrolllens.employee.EmployeeRepository;
import com.acme.payrolllens.employee.EmployeeSpecifications;
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
    public AnalyticsReport report(@RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level) {
        return calculator.calculate(repository.findAll(
                EmployeeSpecifications.matching(query, country, department, level, "ACTIVE")));
    }
}
