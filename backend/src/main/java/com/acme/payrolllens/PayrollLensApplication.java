package com.acme.payrolllens;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Payroll Lens API", version = "0.1.0",
        description = "Manage employees and explore current annual base salary reports."),
        servers = @Server(url = "/"))
public class PayrollLensApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayrollLensApplication.class, args);
    }
}
