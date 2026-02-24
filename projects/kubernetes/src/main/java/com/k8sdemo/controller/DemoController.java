package com.k8sdemo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DemoController exposes endpoints that make Kubernetes concepts visible:
 *
 *  GET /hello              - basic greeting (shows pod identity)
 *  GET /info               - pod info: hostname, env vars from ConfigMap & Secret
 *  GET /load?iterations=N  - CPU burn to trigger HPA scale-out
 *  GET /counter            - shared AtomicLong counter (shows pod is stateless)
 *  GET /config             - values injected via ConfigMap environment variables
 *  GET /secret             - shows secret was injected (value masked)
 *  GET /storage            - reads/writes a file to demonstrate PersistentVolume
 */
@RestController
public class DemoController {

    // Injected from ConfigMap via env var APP_GREETING
    @Value("${APP_GREETING:Hello from Kubernetes!}")
    private String greeting;

    // Injected from ConfigMap via env var APP_VERSION
    @Value("${APP_VERSION:1.0.0}")
    private String appVersion;

    // Injected from ConfigMap via env var APP_ENV
    @Value("${APP_ENV:local}")
    private String appEnv;

    // Injected from Secret via env var DB_PASSWORD (masked in output)
    @Value("${DB_PASSWORD:not-set}")
    private String dbPassword;

    // Injected from Secret via env var API_KEY
    @Value("${API_KEY:not-set}")
    private String apiKey;

    // Shared in-memory counter — resets on pod restart, showing stateless pods
    private final AtomicLong counter = new AtomicLong(0);

    // Path for PersistentVolume demo — mounted via volume in deployment.yml
    private static final String STORAGE_FILE = "/data/visits.txt";

    // -------------------------------------------------------------------------
    // GET /hello
    // -------------------------------------------------------------------------
    @GetMapping("/hello")
    public Map<String, Object> hello() throws UnknownHostException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", greeting);
        response.put("podName", InetAddress.getLocalHost().getHostName());
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /info   — shows everything about this pod's identity
    // -------------------------------------------------------------------------
    @GetMapping("/info")
    public Map<String, Object> info() throws UnknownHostException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("podName",    InetAddress.getLocalHost().getHostName());
        response.put("podIP",      InetAddress.getLocalHost().getHostAddress());
        response.put("nodeName",   System.getenv().getOrDefault("NODE_NAME",   "unknown"));
        response.put("podNamespace", System.getenv().getOrDefault("POD_NAMESPACE", "unknown"));
        response.put("appVersion", appVersion);
        response.put("appEnv",     appEnv);
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("timestamp",  Instant.now().toString());
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /config  — values from ConfigMap
    // -------------------------------------------------------------------------
    @GetMapping("/config")
    public Map<String, String> config() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("APP_GREETING", greeting);
        response.put("APP_VERSION",  appVersion);
        response.put("APP_ENV",      appEnv);
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /secret  — confirms secret injection without leaking values
    // -------------------------------------------------------------------------
    @GetMapping("/secret")
    public Map<String, String> secret() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("DB_PASSWORD", dbPassword.equals("not-set") ? "NOT INJECTED" : "******* (injected, " + dbPassword.length() + " chars)");
        response.put("API_KEY",     apiKey.equals("not-set")     ? "NOT INJECTED" : "******* (injected, " + apiKey.length() + " chars)");
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /counter  — in-memory counter, resets on pod restart
    // -------------------------------------------------------------------------
    @GetMapping("/counter")
    public Map<String, Object> counter() throws UnknownHostException {
        long value = counter.incrementAndGet();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count",   value);
        response.put("podName", InetAddress.getLocalHost().getHostName());
        response.put("note",    "Counter resets to 0 when this pod is replaced — pods are ephemeral!");
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /load?iterations=50000   — burns CPU to trigger HPA
    // -------------------------------------------------------------------------
    @GetMapping("/load")
    public Map<String, Object> load(@RequestParam(defaultValue = "50000") int iterations) throws UnknownHostException {
        long start = System.currentTimeMillis();
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i);
        }
        long elapsed = System.currentTimeMillis() - start;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("podName",    InetAddress.getLocalHost().getHostName());
        response.put("iterations", iterations);
        response.put("elapsedMs",  elapsed);
        response.put("result",     result);
        response.put("tip",        "Hit this endpoint repeatedly to trigger HPA scale-out. Watch: kubectl get hpa -w");
        return response;
    }

    // -------------------------------------------------------------------------
    // GET /storage  — reads/writes to a PersistentVolume mount at /data
    // -------------------------------------------------------------------------
    @GetMapping("/storage")
    public Map<String, Object> storage() {
        Map<String, Object> response = new LinkedHashMap<>();
        java.io.File file = new java.io.File(STORAGE_FILE);
        try {
            // Read current count
            long visitCount = 0;
            if (file.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
                if (!content.isEmpty()) visitCount = Long.parseLong(content);
            }
            visitCount++;

            // Write updated count
            file.getParentFile().mkdirs();
            java.nio.file.Files.write(file.toPath(), String.valueOf(visitCount).getBytes());

            response.put("persistentVisitCount", visitCount);
            response.put("storageFile",          STORAGE_FILE);
            response.put("note",                 "This count persists across pod restarts when a PersistentVolume is mounted at /data");
        } catch (Exception e) {
            response.put("persistentVisitCount", "N/A — /data not mounted");
            response.put("error",                e.getMessage());
            response.put("note",                 "Mount a PersistentVolumeClaim at /data to see persistence in action");
        }
        return response;
    }
}
