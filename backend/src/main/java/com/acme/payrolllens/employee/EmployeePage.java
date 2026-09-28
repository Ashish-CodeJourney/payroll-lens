package com.acme.payrolllens.employee;

import java.util.List;
import org.springframework.data.domain.Page;

public record EmployeePage(List<EmployeeView> items, int page, int size,
        long totalElements, int totalPages) {
    public static EmployeePage from(Page<Employee> result) {
        return new EmployeePage(result.getContent().stream().map(EmployeeView::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
