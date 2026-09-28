package com.acme.payrolllens.employee;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class EmployeeSpecifications {
    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(String query, String country, String department, String level) {
        return (root, criteria, builder) -> {
            List<Predicate> conditions = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                conditions.add(builder.or(
                        builder.like(builder.lower(root.get("fullName")), pattern),
                        builder.like(builder.lower(root.get("email")), pattern),
                        builder.like(builder.lower(root.get("employeeNumber")), pattern)));
            }
            if (country != null && !country.isBlank()) {
                conditions.add(builder.equal(root.get("countryCode"), country));
            }
            if (department != null && !department.isBlank()) {
                conditions.add(builder.equal(root.get("department"), department));
            }
            if (level != null && !level.isBlank()) {
                conditions.add(builder.equal(root.get("jobLevel"), level));
            }
            return builder.and(conditions.toArray(Predicate[]::new));
        };
    }
}
