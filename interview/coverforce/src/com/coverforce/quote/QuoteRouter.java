package com.coverforce.quote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Routes a quote request to the correct per-carrier handler and fetches quotes
 * from all requested carriers in parallel.
 */
public class QuoteRouter {

    /** Hard upper bound on worker threads, regardless of core count. */
    private static final int MAX_POOL_SIZE = 8;

    private static final QuoteRouter INSTANCE = new QuoteRouter(QuoteHandlerRegistry.getInstance());

    private final QuoteHandlerRegistry registry;
    private final ExecutorService executor;

    private QuoteRouter(QuoteHandlerRegistry registry) {
        this.registry = registry;
        this.executor = Executors.newFixedThreadPool(poolSize());
    }

    public static QuoteRouter getInstance() {
        return INSTANCE;
    }

    /**
     * Bounded pool size: never uses all processors (leaves at least one free)
     * and never exceeds {@link #MAX_POOL_SIZE}.
     */
    private static int poolSize() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Math.max(2, Math.min(MAX_POOL_SIZE, cores - 1));
    }

    /**
     * Fetches a quote from every requested carrier concurrently. Carriers
     * without a registered handler yield a DECLINE response.
     */
    public List<QuoteResponse> route(QuoteRequest request) {
        List<CompletableFuture<QuoteResponse>> futures = new ArrayList<>();

        for (Carrier carrier : request.getCarriers()) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> quoteFor(carrier, request), executor));
        }

        List<QuoteResponse> responses = new ArrayList<>();
        for (CompletableFuture<QuoteResponse> future : futures) {
            responses.add(future.join());
        }
        return responses;
    }

    private QuoteResponse quoteFor(Carrier carrier, QuoteRequest request) {
        return registry.get(carrier)
                .map(handler -> quoteFromHandler(handler, request))
                .orElseGet(() -> new QuoteResponse(
                        carrier, QuoteType.DECLINE, 0.0,
                        "Carrier not available: no handler registered for " + carrier));
    }

    private QuoteResponse quoteFromHandler(QuoteHandler handler, QuoteRequest request) {
        LineOfBusiness lob = request.getLineOfBusiness();
        if (!handler.supportedLinesOfBusiness().contains(lob)) {
            return new QuoteResponse(
                    handler.getCarrier(), QuoteType.DECLINE, 0.0,
                    "Line of business not supported: " + handler.getCarrier() + " does not write " + lob);
        }
        return handler.getQuote(request);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
