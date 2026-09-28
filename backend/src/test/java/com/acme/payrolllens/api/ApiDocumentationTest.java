package com.acme.payrolllens.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiDocumentationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void publishesAnOpenApiContractForEmployeeManagementAndReports() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Payroll Lens API"))
                .andExpect(jsonPath("$.info.version").value("0.1.0"))
                .andExpect(jsonPath("$.servers[0].url").value("/"))
                .andExpect(jsonPath("$.paths['/api/employees'].get").exists())
                .andExpect(jsonPath("$.paths['/api/employees'].post").exists())
                .andExpect(jsonPath("$.paths['/api/employees/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/employees/{id}/archive'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/analytics'].get").exists());
    }

    @Test
    void explainsTheDirectoryAndReportSemanticsInTheInteractiveReference() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/employees'].get.summary")
                        .value("Search and page employees"))
                .andExpect(jsonPath("$.paths['/api/employees'].post.summary")
                        .value("Create an employee"))
                .andExpect(jsonPath("$.paths['/api/employees/{id}'].get.summary")
                        .value("Get an employee"))
                .andExpect(jsonPath("$.paths['/api/employees/{id}'].put.summary")
                        .value("Update an employee and current salary"))
                .andExpect(jsonPath("$.paths['/api/employees/{id}/archive'].patch.summary")
                        .value("Archive an employee"))
                .andExpect(jsonPath("$.paths['/api/analytics'].get.summary")
                        .value("Report active employee pay in USD"))
                .andExpect(jsonPath("$.paths['/api/analytics'].get.description")
                        .value(org.hamcrest.Matchers.containsString("2026-01-01")));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
