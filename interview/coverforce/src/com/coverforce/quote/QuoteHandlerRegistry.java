package com.coverforce.quote;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry mapping carriers to their handlers. New carriers plug in by
 * registering a handler; the router never needs to change.
 *
 * <p>
 * Singleton: there is exactly one registry per application. Backed by a
 * thread-safe map because the (parallel) router reads it concurrently.
 */
public class QuoteHandlerRegistry {

    private static final QuoteHandlerRegistry INSTANCE = new QuoteHandlerRegistry();

    private final Map<Carrier, QuoteHandler> handlers = new ConcurrentHashMap<>();

    private QuoteHandlerRegistry() {
    }

    public static QuoteHandlerRegistry getInstance() {
        return INSTANCE;
    }

    public void register(QuoteHandler handler) {
        handlers.put(handler.getCarrier(), handler);
    }

    public Optional<QuoteHandler> get(Carrier carrier) {
        return Optional.ofNullable(handlers.get(carrier));
    }
}
