# Java Backend Engineer — Study Roadmap

## 1. Core Java Mastery
- OOP principles (SOLID, DRY, KISS)
- Generics, Lambda expressions, Functional interfaces
- Java Streams API (map/reduce, collectors)
- Java Collections framework (List, Set, Map, Queue internals)
- Java Reflection API
- Exception handling (checked vs unchecked, custom exceptions)

## 2. Multithreading & Concurrency
- Thread synchronization, Executors, Locks
- Fork/Join framework
- Race conditions, deadlocks, and thread pools
- `java.util.concurrent` (ConcurrentHashMap, CountDownLatch, CyclicBarrier, Semaphore)
- CompletableFuture for async programming
- **See:** `concurrency/` folder for implementations

## 3. Design Patterns & Architecture
- Creational: Singleton, Factory, Builder, Prototype
- Structural: Adapter, Decorator, Proxy, Facade
- Behavioral: Strategy, Observer, Command, Template Method
- Architectural: MVC, Microservices, Event-Driven Architecture
- Dependency Injection (DI), Inversion of Control (IoC)
- **See:** `design-patterns/` folder for implementations

## 4. Java Memory Management
- Garbage Collection (G1, CMS, ZGC)
- JVM heap (Young Gen, Old Gen) and stack management
- Profiling tools (JProfiler, VisualVM, jcmd)
- Analyzing memory leaks, thread dumps, and heap dumps
- Strong, Weak, Soft, Phantom references

## 5. Classloaders and Reflection
- Bootstrap, Extension, Application classloaders
- Custom class loaders
- Dynamic class loading
- Reflection for runtime behavior manipulation

## 6. Spring Framework & Spring Boot
- Spring Core (DI, AOP, Bean lifecycle)
- Spring Boot (Auto-configuration, starters, actuator)
- Spring Security (OAuth2, JWT, session management)
- Spring Data (JPA, Hibernate integration, query methods)
- Spring Cloud (service discovery, config server, circuit breakers)
- **See:** `spring-boot/` folder for notes

## 7. Microservices Architecture
- Service discovery (Eureka, Consul)
- Load balancing (Ribbon, Spring Cloud LoadBalancer)
- Distributed tracing (Zipkin, Jaeger)
- Circuit breaking (Resilience4j)
- API Gateway (Spring Cloud Gateway, Kong)
- Async communication (Kafka, RabbitMQ)

## 8. RESTful Web Services
- REST principles, Richardson Maturity Model
- Building APIs with Spring Web
- JSON/XML handling (Jackson, JAXB)
- API versioning strategies
- OpenAPI/Swagger documentation

## 9. Java I/O and NIO
- Blocking vs non-blocking I/O
- NIO: Channels, Buffers, Selectors
- Asynchronous I/O (NIO2)
- File handling, serialization/deserialization

## 10. Reactive Programming
- Project Reactor (Mono, Flux)
- Spring WebFlux
- Backpressure handling
- Reactive Streams specification

## 11. JPA/Hibernate
- ORM principles, entity relationships (@OneToMany, @ManyToMany)
- Lazy vs Eager loading (N+1 problem)
- First/Second-level caching
- Query optimization (JPQL, Criteria API, native queries)

## 12. Database Optimization
- SQL optimization, indexing strategies (B-tree, Hash)
- Transaction isolation levels (READ COMMITTED, SERIALIZABLE, etc.)
- NoSQL databases (MongoDB, Cassandra, Redis)
- ACID principles, CAP theorem

## 13. Distributed Systems
- Consistency, Availability, Partitioning (CAP)
- Event Sourcing, CQRS
- Distributed caching (Redis, Hazelcast)
- Consensus algorithms (Raft, Paxos)
- Tools: Apache ZooKeeper, Consul, etcd

## 14. Testing & TDD/BDD
- Unit testing (JUnit 5, Mockito)
- Integration testing (@SpringBootTest, Testcontainers)
- BDD (Cucumber)
- Code coverage (JaCoCo)

## 15. CI/CD & DevOps
- Continuous Integration (GitHub Actions, Jenkins)
- Containerization (Docker, multi-stage builds)
- Orchestration (Kubernetes, Helm)
- Git branching strategies (GitFlow, trunk-based)

## 16. Performance Tuning
- JVM tuning (-Xmx, -Xms, GC flags)
- Profiling and monitoring (Prometheus, Grafana, Micrometer)
- Thread dump analysis
- Database connection pooling (HikariCP)
