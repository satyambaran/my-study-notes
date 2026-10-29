# Plan: QuoteRouter (Java, plain, parallel, registry-based)

## Requirements (confirmed)
- Plain Java, no build tool. Compile with javac.
- QuoteRequest carries a `List<Carrier>` (minimal, nothing else besides list).
- Router fetches quotes for ALL requested carriers.
- Execution: CONCURRENT via ExecutorService/CompletableFuture.
- Unknown carrier (no registered handler) -> return a quote with quoteType=DECLINE.
- Registry/factory pattern so new carriers plug in without editing router.
- Two concrete handlers: HiscoxQuoteHandler, CoterieQuoteHandler (mock remote call).
- Each handler returns fake quote: quoteType (SUCCESS/DECLINE/FAILED) + premium number.
- main() demo prints results.

## Package: com.coverforce.quote

## Files (all under src/com/coverforce/quote/)
1. Carrier.java - enum: HISCOX, COTERIE, EMPLOYERS, CHUBB, LIBERTY
2. QuoteType.java - enum: SUCCESS, DECLINE, FAILED
3. QuoteRequest.java - holds List<Carrier> carriers; getter
4. QuoteResponse.java - carrier, quoteType, premium (double), message; toString
5. QuoteHandler.java - interface: Carrier getCarrier(); QuoteResponse getQuote(QuoteRequest)
6. HiscoxQuoteHandler.java - impl, simulate latency (Thread.sleep), SUCCESS + premium
7. CoterieQuoteHandler.java - impl, similar
8. QuoteHandlerRegistry.java - Map<Carrier,QuoteHandler>; register(handler); Optional<QuoteHandler> get(carrier)
9. QuoteRouter.java - holds registry + ExecutorService; route(QuoteRequest) -> List<QuoteResponse> using CompletableFuture.supplyAsync per carrier, join all. Unknown -> DECLINE response. shutdown().
10. Main.java - build registry, register handlers, create router, run sample requests, print.

## Design notes
- Handler simulates remote API: Thread.sleep(~small) then return fabricated QuoteResponse.
- Unknown carrier handled in router: if registry.get empty -> QuoteResponse(carrier, DECLINE, 0, "No handler").
- DECISION: both handlers return SUCCESS for now. FAILED path added later.
- DECISION: premium type = double.
- ExecutorService fixed thread pool; ensure shutdown in main.

## Verification
1. `javac -d out src/com/coverforce/quote/*.java`
2. `java -cp out com.coverforce.quote.Main`
3. Confirm output shows one QuoteResponse per requested carrier, unknown carrier -> DECLINE.
