package com.simpleflow.runner.controller;

import com.simpleflow.lang.Main;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RunController {

    @PostMapping("/run")
    public Map<String, String> run(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");

            if (code == null || code.isBlank()) {
                return Map.of("output", "No code provided");
            }

            String output = Main.run(code);
            return Map.of("output", output);

        } catch (Exception e) {
            return Map.of("output", "Error: " + e.getMessage());
        }
    }
}
