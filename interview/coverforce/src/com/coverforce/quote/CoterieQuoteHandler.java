package com.coverforce.quote;

import java.util.EnumSet;
import java.util.Set;

/**
 * Mock Coterie quote handler. Simulates a remote API call and returns a
 * successful quote. Writes BOP and WC.
 */
public class CoterieQuoteHandler implements QuoteHandler {

    @Override
    public Carrier getCarrier() {
        return Carrier.COTERIE;
    }

    @Override
    public Set<LineOfBusiness> supportedLinesOfBusiness() {
        return EnumSet.of(LineOfBusiness.BOP, LineOfBusiness.WC);
    }

    @Override
    public QuoteResponse getQuote(QuoteRequest request) {
        simulateRemoteCall();
        double premium = 980.50;
        return new QuoteResponse(Carrier.COTERIE, QuoteType.SUCCESS, premium, "Quote issued by Coterie");
    }

    private void simulateRemoteCall() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
