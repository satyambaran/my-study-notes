package com.coverforce.quote;

import java.util.EnumSet;
import java.util.Set;

/**
 * Mock Hiscox quote handler. Simulates a remote API call and returns a
 * successful quote. Writes BOP and GL.
 */
public class HiscoxQuoteHandler implements QuoteHandler {

    @Override
    public Carrier getCarrier() {
        return Carrier.HISCOX;
    }

    @Override
    public Set<LineOfBusiness> supportedLinesOfBusiness() {
        return EnumSet.of(LineOfBusiness.BOP, LineOfBusiness.GL);
    }

    @Override
    public QuoteResponse getQuote(QuoteRequest request) {
        simulateRemoteCall();
        double premium = 1250.00;
        return new QuoteResponse(Carrier.HISCOX, QuoteType.SUCCESS, premium, "Quote issued by Hiscox");
    }

    private void simulateRemoteCall() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
