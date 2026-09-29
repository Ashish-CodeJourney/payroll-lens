package com.acme.payrolllens.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SingleOriginUiController {
    @GetMapping({"/", "/employees", "/employees/new", "/employees/{id}", "/reports"})
    public String entryPoint() {
        return "forward:/index.html";
    }
}
