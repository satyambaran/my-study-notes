/**
 * SPRING DEPENDENCY INJECTION (DI) — Concepts
 * ==============================================
 *
 * INVERSION OF CONTROL (IoC):
 *   Instead of a class creating its own dependencies, an external framework
 *   (Spring Container) creates and injects them. DI is the concrete
 *   implementation of IoC.
 *
 * === THREE TYPES OF DI ===
 *
 * 1. CONSTRUCTOR INJECTION (Recommended):
 *    Dependencies are provided via the constructor.
 *    + Immutable (fields can be final).
 *    + All required dependencies guaranteed at creation time.
 *    + Easy to test (just pass mocks in constructor).
 *
 *    @Service
 *    public class OrderService {
 *        private final PaymentGateway gateway;  // final = immutable
 *
 *        @Autowired  // Optional since Spring 4.3 (single constructor)
 *        public OrderService(PaymentGateway gateway) {
 *            this.gateway = gateway;
 *        }
 *    }
 *
 * 2. SETTER INJECTION:
 *    Dependencies provided via setter methods.
 *    + Allows optional dependencies.
 *    - Object can exist in partially initialized state.
 *
 *    @Service
 *    public class OrderService {
 *        private PaymentGateway gateway;
 *
 *        @Autowired
 *        public void setGateway(PaymentGateway gateway) {
 *            this.gateway = gateway;
 *        }
 *    }
 *
 * 3. FIELD INJECTION (Avoid in production):
 *    @Autowired directly on field. Looks clean but:
 *    - Can't make fields final.
 *    - Hard to test without Spring context.
 *    - Hides dependencies (not visible in constructor).
 *
 *    @Service
 *    public class OrderService {
 *        @Autowired
 *        private PaymentGateway gateway;  // BAD: hidden dependency
 *    }
 *
 * === BENEFITS OF DI ===
 *
 * - Loose Coupling: Depend on interfaces, not implementations.
 * - Testability: Easy to mock dependencies in unit tests.
 * - Flexibility: Swap implementations via configuration (e.g., @Profile).
 * - Maintainability: Change one implementation without touching dependents.
 *
 * === SPRING BEAN SCOPES ===
 *
 * @Scope("singleton")  — Default. One instance per Spring container.
 * @Scope("prototype")  — New instance every time it's requested.
 * @Scope("request")    — One per HTTP request (web apps).
 * @Scope("session")    — One per HTTP session (web apps).
 *
 * === COMMON ANNOTATIONS ===
 *
 * @Component       — Generic Spring-managed bean.
 * @Service         — Business logic layer (semantic alias for @Component).
 * @Repository      — Data access layer (adds exception translation).
 * @Controller      — Web layer (handles HTTP requests).
 * @Configuration   — Class that defines @Bean methods.
 * @Bean            — Method-level: registers return value as a bean.
 * @Autowired       — Inject dependency (by type).
 * @Qualifier("x")  — Disambiguate when multiple beans of same type exist.
 */
public class DependencyInjectionNotes {
    // This file contains conceptual notes, not executable code.
    // See the demo/ folder for a working Spring Boot application.
}
