package com.simpleflow.runner.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CodeExecutor {

    public static String runWithTimeout(Callable<String> task, long timeoutMs) throws Exception {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(task);

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // interrupt execution
            throw new RuntimeException("Execution timed out (" + timeoutMs + "ms)");
        } finally {
            executor.shutdownNow();
        }
    }
}
