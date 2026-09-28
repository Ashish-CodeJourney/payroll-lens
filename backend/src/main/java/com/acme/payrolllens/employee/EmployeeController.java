package com.acme.payrolllens.employee;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Search and page employees",
            description = "Search by name, employee number, or email. Combine country, department, "
                    + "level, and status filters. Page size is capped at 100; default status is ACTIVE.")
    public EmployeePage list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "ACTIVE") String status) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be nonnegative and size must be positive");
        }
        return EmployeePage.from(repository.findAll(
                EmployeeSpecifications.matching(query, country, department, level, status),
                PageRequest.of(page, Math.min(size, 100), Sort.by("employeeNumber").ascending())));
    }

    @PostMapping
    @Operation(summary = "Create an employee",
            description = "Record current annual gross base salary in the employee's local currency.")
    public ResponseEntity<EmployeeView> create(@Valid @RequestBody EmployeeInput input) {
        Employee employee = repository.save(input.toEmployee());
        return ResponseEntity.created(URI.create("/api/employees/" + employee.getId()))
                .body(EmployeeView.from(employee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee")
    public EmployeeView get(@PathVariable Long id) {
        return EmployeeView.from(repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee and current salary",
            description = "Replace editable profile fields and current annual gross base salary.")
    public EmployeeView update(@PathVariable Long id, @Valid @RequestBody EmployeeInput input) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        input.applyTo(employee);
        return EmployeeView.from(repository.save(employee));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive an employee",
            description = "Exclude the employee from active pay reports while retaining their record.")
    public EmployeeView archive(@PathVariable Long id) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        employee.archive();
        return EmployeeView.from(repository.save(employee));
    }
}
