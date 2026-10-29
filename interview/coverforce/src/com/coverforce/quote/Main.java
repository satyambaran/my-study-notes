package com.coverforce.quote;

import java.util.Arrays;
import java.util.List;

/**
 * Demo entrypoint: wires up the registry, registers handlers, and routes a few
 * sample requests.
 */
public class Main {

    public static void main(String[] args) {
        QuoteHandlerRegistry registry = QuoteHandlerRegistry.getInstance();
        registry.register(new HiscoxQuoteHandler());
        registry.register(new CoterieQuoteHandler());

        QuoteRouter router = QuoteRouter.getInstance();

        try {
            // WC request:
            // - HISCOX writes BOP/GL -> line of business not supported
            // - COTERIE writes BOP/WC -> success
            // - LIBERTY has no handler -> carrier not available
            QuoteRequest request = new QuoteRequest(
                    Arrays.asList(Carrier.HISCOX, Carrier.COTERIE, Carrier.LIBERTY),
                    LineOfBusiness.WC);

            long start = System.currentTimeMillis();
            List<QuoteResponse> responses = router.route(request);
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("Quotes for request " + request + ":");
            responses.forEach(r -> System.out.println("  " + r));
            System.out.println("Elapsed: " + elapsed + " ms (parallel)");
        } finally {
            router.shutdown();
        }
    }
}
/*
 * QuoteHandler getQuotehandler(carrier){
 * switch(carrier){
 * case Carrier.Hiscox:
 * new CoterieQuoteHandler()
 * 
 * 
 * }
 * }
 * 
 * 
 * for(var carrier in carriers){
 * registry.register(factory.getQuotehandler(carrier))
 * }
 * 
 */
