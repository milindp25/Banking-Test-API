package org.demo.bankingtestapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping("/endpoint")
    public Map<String, String> getProtectedData() {
        return Map.of("message", "This is a protected resource. JWT is valid!");
    }
}

