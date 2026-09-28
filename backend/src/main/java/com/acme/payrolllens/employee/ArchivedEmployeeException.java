package com.acme.payrolllens.employee;

public class ArchivedEmployeeException extends RuntimeException {
    public ArchivedEmployeeException() {
        super("Archived employees are read-only");
    }
}
