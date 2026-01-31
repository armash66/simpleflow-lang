package com.simpleflow.runner.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.simpleflow.lang.Main;
import com.simpleflow.runner.util.CodeExecutor;

@RestController
@CrossOrigin(origins = "*")
public class RunController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/run")
    public Map<String, Object> run(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");

            if (code == null || code.isBlank()) {
                return Map.of(
                    "output", "",
                    "error", "No code provided"
                );
            }

            String output = CodeExecutor.runWithTimeout(
                () -> Main.run(code),
                2000 // 2 seconds
            );

            return Map.of(
                "output", output,
                "error", ""
            );

        } catch (Exception e) {
            return Map.of(
                "output", "",
                "error", e.getMessage()
            );
        }
    }
}
