package com.simpleflow.runner.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin
public class RunController {

    @PostMapping("/run")
    public Map<String, String> run(@RequestBody Map<String, String> body) {
        return Map.of("output", "OK");
    }
}
