package com.dws.challenge;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestGradleController {

    @GetMapping(value = "/welcome")
    public String hello() {
        return "TechGeekNext - Welcome to Spring boot Gradle Demo";
    }
}
