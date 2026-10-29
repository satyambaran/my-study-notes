package com.coverforce.quote;

/**
 * Result of a single carrier quote call.
 */
public class QuoteResponse {

    private final Carrier carrier;
    private final QuoteType quoteType;
    private final double premium;
    private final String message;

    public QuoteResponse(Carrier carrier, QuoteType quoteType, double premium, String message) {
        this.carrier = carrier;
        this.quoteType = quoteType;
        this.premium = premium;
        this.message = message;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public QuoteType getQuoteType() {
        return quoteType;
    }

    public double getPremium() {
        return premium;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format(
                "QuoteResponse{carrier=%s, quoteType=%s, premium=%.2f, message='%s'}",
                carrier, quoteType, premium, message);
    }
}
