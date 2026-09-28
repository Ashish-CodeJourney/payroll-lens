package com.acme.payrolllens.employee;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public EmployeePage list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String level) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be nonnegative and size must be positive");
        }
        return EmployeePage.from(repository.findAll(
                EmployeeSpecifications.matching(query, country, department, level),
                PageRequest.of(page, Math.min(size, 100), Sort.by("employeeNumber").ascending())));
    }

    @PostMapping
    public ResponseEntity<EmployeeView> create(@Valid @RequestBody EmployeeInput input) {
        Employee employee = repository.save(input.toEmployee());
        return ResponseEntity.created(URI.create("/api/employees/" + employee.getId()))
                .body(EmployeeView.from(employee));
    }

    @GetMapping("/{id}")
    public EmployeeView get(@PathVariable Long id) {
        return EmployeeView.from(repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id)));
    }
}
