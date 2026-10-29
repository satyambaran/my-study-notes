package com.coverforce.quote;

import java.util.Set;

/**
 * Contract for a per-carrier quote handler.
 */
public interface QuoteHandler {

    /** The carrier this handler is responsible for. */
    Carrier getCarrier();

    /** Lines of business this carrier writes. */
    Set<LineOfBusiness> supportedLinesOfBusiness();

    /** Simulates a remote quote API call for this carrier. */
    QuoteResponse getQuote(QuoteRequest request);
}
