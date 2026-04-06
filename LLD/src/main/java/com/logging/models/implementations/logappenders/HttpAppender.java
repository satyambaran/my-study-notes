package com.logging.models.implementations.logappenders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.logging.models.entities.LogMessage;
import com.logging.models.interfaces.LogAppender;
import com.logging.models.interfaces.LogFormatter;

/**
 * POSTs each formatted log message to a remote HTTP endpoint (e.g. ELK, Splunk, Datadog).
 * Uses Java 11+ HttpClient with configurable timeout.
 */
public class HttpAppender implements LogAppender {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final String endpoint;
    private final HttpClient httpClient;
    private LogFormatter logFormatter;

    public HttpAppender(String endpoint, LogFormatter logFormatter) {
        this(endpoint, logFormatter, DEFAULT_TIMEOUT);
    }

    public HttpAppender(String endpoint, LogFormatter logFormatter, Duration timeout) {
        this.endpoint = endpoint;
        this.logFormatter = logFormatter;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
    }

    @Override
    public void append(LogMessage logMessage) {
        String body = logFormatter.format(logMessage);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                System.err.println("HttpAppender received HTTP " + response.statusCode()
                    + " from " + endpoint);
            }
        } catch (Exception e) {
            System.err.println("HttpAppender failed to POST to " + endpoint + ": " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // HttpClient has no explicit close; GC handles it
    }

    @Override
    public LogFormatter getLogFormatter() { return logFormatter; }

    @Override
    public void setLogFormatter(LogFormatter logFormatter) { this.logFormatter = logFormatter; }
}
