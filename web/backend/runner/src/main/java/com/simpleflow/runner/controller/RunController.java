package com.simpleflow.runner.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.simpleflow.lang.Main;

@RestController
@CrossOrigin(origins = "*")
public class RunController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/run")
    public Map<String, String> run(@RequestBody Map<String, String> body) {

        Map<String, String> response = new HashMap<>();

        String code = body.get("code");
        if (code == null || code.isBlank()) {
            response.put("output", "");
            response.put("error", "No code provided");
            return response;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<String> future = executor.submit(() -> Main.run(code));

            // ⏱ 2 second execution limit
            String output = future.get(2, TimeUnit.SECONDS);

            response.put("output", output == null ? "" : output);
            response.put("error", "");

        } catch (TimeoutException e) {
            response.put("output", "");
            response.put("error", "Execution timed out (2000ms)");

        } catch (InterruptedException e) {
            response.put("output", "");
            response.put("error", "Execution interrupted");

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            response.put("output", "");
            response.put(
                "error",
                cause != null ? cause.getMessage() : e.getMessage()
            );

        } finally {
            executor.shutdownNow();
        }

        return response;
    }
}
