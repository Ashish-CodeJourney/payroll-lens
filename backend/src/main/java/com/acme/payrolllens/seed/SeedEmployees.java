package com.acme.payrolllens.seed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public final class SeedEmployees {
    private static final String[] COUNTRIES = {"US", "IN", "GB", "DE", "CA"};
    private static final String[] CURRENCIES = {"USD", "INR", "GBP", "EUR", "CAD"};
    private static final long[] BASE_SALARIES = {55_000, 700_000, 42_000, 48_000, 52_000};
    private static final long[] LEVEL_INCREASES = {28_000, 350_000, 22_000, 25_000, 26_000};
    private static final String[] DEPARTMENTS = {"Engineering", "Sales", "Operations", "Finance", "People"};
    private static final String[] JOB_TITLES = {"Software Engineer", "Account Executive",
            "Operations Specialist", "Financial Analyst", "HR Partner"};
    private static final String[] LEVELS = {"L1", "L2", "L3", "L4", "L5"};
    private static final String[] FIRST_NAMES = {"Alex", "Amina", "Arjun", "Ava", "Ben",
            "Chloe", "Daniel", "Elena", "Fatima", "Grace", "Hugo", "Isabel", "Jordan",
            "Kai", "Leah", "Maya", "Noah", "Priya", "Sam", "Zoe"};
    private static final String[] LAST_NAMES = {"Ahmed", "Brown", "Chen", "Das", "Evans",
            "Fernandez", "Gupta", "Harris", "Ito", "Jones", "Khan", "Lee", "Martin",
            "Nguyen", "Patel", "Robinson", "Shah", "Singh", "Taylor", "Wilson"};

    private SeedEmployees() {
    }

    public static List<SeedEmployee> generate() {
        return IntStream.rangeClosed(1, 10_000).mapToObj(SeedEmployees::employeeAt).toList();
    }

    private static SeedEmployee employeeAt(int index) {
        int country = (index - 1) % COUNTRIES.length;
        int department = ((index - 1) / COUNTRIES.length) % DEPARTMENTS.length;
        int level = ((index - 1) / (COUNTRIES.length * DEPARTMENTS.length)) % LEVELS.length;
        long salary = BASE_SALARIES[country] + level * LEVEL_INCREASES[country]
                + (index % 137L) * (BASE_SALARIES[country] / 100L);
        String number = String.format(Locale.ROOT, "ACM-%05d", index);
        String name = FIRST_NAMES[(index - 1) % FIRST_NAMES.length] + " "
                + LAST_NAMES[((index - 1) / FIRST_NAMES.length) % LAST_NAMES.length];
        String email = String.format(Locale.ROOT, "employee%05d@acme.example", index);
        return new SeedEmployee(number, name, email, COUNTRIES[country],
                DEPARTMENTS[department], JOB_TITLES[department], LEVELS[level],
                BigDecimal.valueOf(salary), CURRENCIES[country]);
    }
}
