package com.coverforce.quote;

import java.util.Collections;
import java.util.List;

/**
 * Quote request: which carriers to try, for a single line of business.
 */
public class QuoteRequest {

    private final List<Carrier> carriers;
    private final LineOfBusiness lineOfBusiness;

    public QuoteRequest(List<Carrier> carriers, LineOfBusiness lineOfBusiness) {
        this.carriers = Collections.unmodifiableList(carriers);
        this.lineOfBusiness = lineOfBusiness;
    }

    public List<Carrier> getCarriers() {
        return carriers;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    @Override
    public String toString() {
        return String.format(
                "QuoteRequest{carriers=%s, lineOfBusiness=%s}", carriers, lineOfBusiness);
    }
}
