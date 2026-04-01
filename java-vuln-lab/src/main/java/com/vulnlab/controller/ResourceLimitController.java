package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/resource")
public class ResourceLimitController {

    @GetMapping("/regex")
    public String regex(@RequestParam(value = "input", defaultValue = "") String input) {
        Pattern pattern = Pattern.compile("^(a+)+$");
        long startTime = System.currentTimeMillis();

        try {
            Matcher matcher = pattern.matcher(input);
            boolean matches = matcher.matches();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            return String.format(
                "Regex: ^(a+)+$\nInput: %s\nMatches: %s\nDuration: %d ms",
                input.isEmpty() ? "(empty)" : input, matches, duration
            );
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/memory")
    public String memory(@RequestParam(value = "size", defaultValue = "1000") int size) {
        int maxSize = 1000000;
        if (size > maxSize) {
            size = maxSize;
        }

        long startTime = System.currentTimeMillis();
        long startMem = Runtime.getRuntime().freeMemory();

        try {
            List<byte[]> arrays = new ArrayList<>();
            for (int i = 0; i < size / 1000; i++) {
                arrays.add(new byte[1024 * 100]);
            }

            long endTime = System.currentTimeMillis();
            long endMem = Runtime.getRuntime().freeMemory();
            long duration = endTime - startTime;
            long memUsed = startMem - endMem;

            return String.format(
                "Size: %d\nArrays: %d\nMemory: %d bytes\nDuration: %d ms",
                size, arrays.size(), memUsed, duration
            );
        } catch (OutOfMemoryError e) {
            return "OOM: " + e.getMessage();
        }
    }

    @GetMapping("/cpu")
    public String cpu(@RequestParam(value = "n", defaultValue = "30") int n) {
        int maxN = 45;
        if (n > maxN) {
            n = maxN;
        }

        long startTime = System.currentTimeMillis();
        long result = fibonacci(n);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        return String.format(
            "Fibonacci(%d) = %d\nDuration: %d ms",
            n, result, duration
        );
    }

    @GetMapping("/info")
    public String info() {
        return String.format(
            "Resource Limit Testing\nJava: %s\nOS: %s %s",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }

    private long fibonacci(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
