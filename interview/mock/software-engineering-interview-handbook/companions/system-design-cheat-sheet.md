# System Design Cheat Sheet

A standalone, fast-reference companion for system design interviews. It does
not assume you have read any other part of the handbook. Every figure below
is a rough, order-of-magnitude planning estimate, not a benchmark guarantee —
say that out loud in the interview, then keep reasoning from it anyway. No
option in any table is presented as universally correct; the right answer
always depends on the requirements you gather in the first five minutes.

**What's inside:** a repeatable interview flow, requirement-gathering
questions, capacity formulas, latency/throughput estimates, component
decision tables, data modeling, API design, caching, partitioning,
replication, consistency, messaging, rate limiting, reliability, security,
observability, deployment, cost trade-offs, six compact architecture
blueprints, common pitfalls, and rapid-fire Q&A.

## 1. Repeatable Interview Flow

Run the same seven-phase loop for every prompt, familiar or not. A repeatable
process prevents panic on an unfamiliar system and signals seniority more
reliably than any single clever idea.

```text
+-----------+    +------------+    +--------------+    +--------------+
| 1 Clarify |--> | 2 Estimate |--> | 3 API & Data |--> | 4 High-Level |
|   Scope   |    |   Scale    |    |  Contracts   |    | Architecture |
+-----------+    +------------+    +--------------+    +--------------+
                                                              |
                                                              v
+----------------+    +-----------------------+    +-----------+
| 5 Deep-Dive on |--> | 6 Stress-Test Failure |--> | 7 Wrap-Up |
|   Hard Part    |    |  Modes & Scale Limits |    |           |
+----------------+    +-----------------------+    +-----------+
```

| Phase | Time (of ~45–60 min) | Goal | Exit signal |
|---|---|---|---|
| 1. Clarify scope | ~5 min | Pin functional scope, actors, explicit non-goals | You can restate the problem in two sentences |
| 2. Estimate scale | ~5 min | Rough QPS, storage, bandwidth | You have 3–4 numbers you'll reuse later |
| 3. API & data contracts | ~5 min | Define core endpoints/messages and entities | 3–6 endpoints and a first-pass schema exist |
| 4. High-level architecture | ~10 min | Boxes for clients, gateway, services, stores, cache, queue | One coherent request path on the board |
| 5. Deep-dive | ~15 min | Follow the interviewer's interest | One hard trade-off discussed in real depth |
| 6. Stress-test | ~5 min | Failure modes, hot spots, scale limits | You named the first thing to break and its fix |
| 7. Wrap-up | ~5 min | Trade-offs, security/cost/observability recap | A closing summary, not a trailing-off |

Notes:
- Scale time proportionally for 30- or 60-minute formats; never skip phase 1
  or phase 7 to buy time for the middle.
- Narrate phase transitions out loud ("Let's estimate scale before we design
  the API") — it keeps the interviewer oriented and doubles as a time check.
- If the interviewer redirects you mid-flow, follow them; the loop is a
  default path, not a script to defend.

## 2. Requirement-Gathering Questions

Ask before you draw. Skipping this phase is the most common way candidates
lose control of an interview, because every later decision (storage,
consistency, cost) depends on answers gathered here.

| Dimension | Sample question to ask | Signal it gives you |
|---|---|---|
| Functional scope | Which actions must ship in v1 vs. later? | Bounds the MVP, prevents scope creep |
| Users & scale | Total users, DAU, peak requests/sec? | Feeds every capacity formula |
| Read/write mix | Read-heavy, write-heavy, or balanced? | Drives caching, replica count, DB choice |
| Latency vs. freshness | Is a stale read acceptable, or must reads be current? | Chooses the consistency model |
| Data shape & retention | Structured rows, documents, blobs, streams? How long retained? | Chooses storage tier and schema |
| Consistency needs | Can two users briefly disagree, or must all reads agree instantly? | CAP/PACELC trade-off |
| Growth trajectory | New product at 1K users, or scaling to 100M? | Sets how much headroom to design for |
| Constraints | Team size, existing infra, budget, compliance region? | Decides build vs. buy |

Never assume "internet scale" by default. "Design a chat app" can mean a
50-person internal tool or a billion-user product, and the correct
architecture differs by orders of magnitude in both cost and complexity for
each.

## 3. Capacity Estimation Formulas

Rough formulas, not exact math — round aggressively and say the rounding out
loud.

- Average QPS ≈ (Daily Active Users × Actions per User per Day) ÷ 86,400 s
- Peak QPS ≈ Average QPS × peak factor (~2×–5×; use ~3× if unsure)
- Storage per day ≈ New records per day × average record size
- Storage for retention ≈ Storage per day × retention period, +~30–50% for
  indexes/replication overhead
- Egress bandwidth ≈ Peak QPS × average response size
- Servers needed ≈ Peak QPS ÷ sustainable QPS per server (a mid-size
  stateless app node: roughly ~500–2,000 req/s)
- Cache size ≈ working-set estimate; ~10–20% of daily active data often
  covers ~80% of reads (Pareto-shaped hot set)
- Read replicas ≈ ceil(read QPS ÷ max QPS per replica) + 1 spare

**Worked example — a news-feed-style product:**
~50M DAU, each loads the feed ~10×/day → average read QPS ≈
50,000,000 × 10 ÷ 86,400 ≈ ~5,800 QPS. Apply a ~3× peak factor → peak ≈
~17,000 QPS. If each user posts ~0.2×/day, write QPS ≈ ~115 — roughly a
50:1 read-heavy skew, so invest in read replicas and caching rather than
over-provisioning writes. At ~1 KB per post, storage growth ≈
50M × 0.2 × 1 KB/day ≈ ~10 GB/day, or ~3.6 TB/year before replication
overhead — comfortably inside a single sharded store for years.

## 4. Latency and Throughput Reference Values

All numbers below are order-of-magnitude planning estimates gathered from
typical commodity infrastructure, not vendor benchmarks. Use them to compare
options relatively, then validate with real load tests before committing to
an SLA.

| Operation | Approx. latency |
|---|---|
| L1/L2 CPU cache reference | ~1–10 ns |
| Main memory (RAM) access | ~100 ns |
| Uncontended mutex lock/unlock | ~20–30 ns |
| Serialize a small payload in-process | ~1–10 µs |
| Read 1 MB sequentially from RAM | ~5–10 µs |
| Random read, SSD | ~50–150 µs |
| Read 1 MB sequentially from SSD | ~1 ms |
| Disk seek, spinning HDD | ~10 ms |
| Round trip, same availability zone | ~0.5 ms |
| Round trip, cross-region same continent | ~30–60 ms |
| Round trip, cross-continent | ~100–200 ms |
| Cold DNS lookup | ~20–120 ms |

| Component (single mid-size node) | Rough sustainable throughput |
|---|---|
| In-memory KV cache (Redis-class) | ~50K–100K ops/sec |
| Relational DB, simple indexed query | ~1K–5K QPS |
| Relational DB, heavy join/aggregation | tens to low hundreds QPS |
| Stateless HTTP API node (light handler) | ~1K–10K req/sec |
| Log-structured queue partition (Kafka-class) | ~10K–100K msgs/sec |
| Object storage, aggregate, horizontally scaled | effectively unbounded; per-request latency is the real limit |

Rule of thumb: a network hop costs roughly as much as thousands of memory
accesses, and a cross-region hop costs roughly as much as thousands of
same-zone hops. Minimize hops on the hot path before micro-optimizing
anything else.

## 5. Component Decision Tables

**Database family**

| Family | Pick when | Avoid when |
|---|---|---|
| Relational (SQL) | Strong consistency, transactions, joins | Constant schema churn, extreme single-table write scale |
| Document store | Flexible/nested schema, per-entity access | Heavy cross-document joins/transactions |
| Key-value store | Point lookups, extreme read/write throughput | Range queries, complex filtering |
| Wide-column store | Huge write volume, time-ordered access | Ad hoc queries on non-key columns |
| Graph database | Relationship traversal (social graph, fraud rings) | Simple CRUD, bulk analytics scans |
| Search index | Full-text search, faceted filtering | Source of truth for transactional data |
| Time-series DB | Metrics/telemetry, append-only, time-range queries | Random-access updates to old records |

**Compute model**

| Model | Pick when | Avoid when |
|---|---|---|
| VM | Full OS control, long-lived stateful workloads | Rapid autoscale needs, small ops budget |
| Containers + orchestrator | Many services, need portability and fine-grained scaling | Small team without platform investment |
| Serverless/FaaS | Spiky/event-driven traffic, low ops overhead | Long-lived connections, strict cold-start budgets |

**Load balancing**

| Layer/algorithm | Pick when |
|---|---|
| L4 (transport) LB | Raw TCP/UDP, minimal overhead, protocol-agnostic |
| L7 (application) LB | Routing by path/header, TLS termination, retries |
| Round robin | Backends roughly uniform in capacity and request cost |
| Least connections | Request durations vary widely |
| Consistent hashing | Need session affinity or per-key cache locality |

**Storage tier**

| Tier | Latency | Relative cost | Use for |
|---|---|---|---|
| Hot (in-memory/SSD block) | Sub-ms to low ms | Highest | Active working set, indexes |
| Warm (standard object storage) | ~10s of ms | Moderate | Recent files, backups |
| Cold/archive | Minutes to hours | Lowest, often ~5–20x cheaper | Compliance retention, rarely-read data |

## 6. Data Modeling

- Start from access patterns, not entities alone — the query shape often
  dictates the partition key, especially in NoSQL.
- Normalize by default for OLTP correctness (avoid update anomalies);
  denormalize deliberately for read-heavy scale, accepting sync cost.
- Pick a partition/primary key with high cardinality, even distribution, and
  a match to the dominant query — a poor key creates a hot partition that no
  cache can fix.
- Model cardinality explicitly (1:1, 1:N, N:M): it decides whether you embed,
  reference, or use a join/edge table.
- Prefer additive schema evolution (new optional fields) over destructive
  change; use expand-migrate-contract for breaking changes under live
  traffic.
- Partition time-ordered data by a time bucket plus entity key so range
  scans stay local and old partitions archive independently.
- Keep one authoritative source per fact; if a field is denormalized
  elsewhere for read speed, define exactly which write path refreshes it.

| Access pattern | Recommended approach |
|---|---|
| Point lookup by ID | Key-value store or indexed relational row |
| Range query by time | Wide-column/time-series, partitioned by time bucket |
| Complex joins/reporting | Relational store; offload heavy analytics to a warehouse |
| Flexible/nested attributes | Document store |
| Relationship traversal (N hops) | Graph database |
| Full-text/faceted search | Search index, fed asynchronously from the source of truth |

## 7. API Design

- Model resources as nouns, actions as HTTP verbs; reserve RPC-style
  endpoints for true non-CRUD actions (e.g., `POST /payments/{id}/refund`).
- Return consistent status codes and a machine-readable error envelope
  (code, message, correlation ID) on every endpoint, not just some.
- Paginate every list endpoint from day one. Offset pagination is simple but
  drifts under concurrent writes; cursor/keyset pagination (an opaque token
  referencing the last seen key) stays correct at scale and under churn.
- Version explicitly (URI path or header) from the first release —
  versioning cannot be retrofitted painlessly once clients depend on
  undocumented behavior.
- Make unsafe operations idempotent: accept a client-generated idempotency
  key on `POST` so retries after a timeout don't double-create resources.
- Prefer additive, backward-compatible changes; deprecate old fields on a
  published timeline instead of removing them abruptly.
- Choose the interaction style deliberately: request/response for
  synchronous needs, webhooks for long-running work, streaming
  (WebSocket/SSE) for continuous updates.

| Style | Strength | Weakness |
|---|---|---|
| REST/JSON | Simple, cacheable, human-debuggable | Over/under-fetching, chatty for graphs |
| GraphQL | Client controls exact shape, one round trip | Harder to cache/rate-limit, query-cost risk |
| gRPC | Compact binary, strongly typed, fast | Less browser-friendly, harder to debug by hand |
| Webhook/callback | No polling, efficient for rare events | Needs reliable delivery/retry on both ends |
| WebSocket/SSE | Real-time push, low per-message overhead | Stateful connection complicates load balancing |

## 8. Caching

- Cache-aside (lazy load): app checks cache, falls back to the store on a
  miss and populates the cache. Simplest; first request after eviction pays
  full store latency.
- Read-through: the cache layer itself owns fetch-on-miss, transparent to
  the app.
- Write-through: writes hit cache and store together — always fresh, extra
  write latency.
- Write-behind (write-back): writes land in cache and flush to the store
  async — fastest writes, risks loss if the cache node dies pre-flush.
- Layer caches close to the client: browser → CDN/edge → API
  gateway/reverse proxy → app in-memory cache → distributed cache
  (Redis/Memcached) → DB buffer pool.
- Eviction policy: LRU for recency-biased access (default good choice), LFU
  when popularity is stable but bursty, TTL to bound staleness simply.
- Invalidation is the hardest part: prefer short TTL + explicit
  invalidate-on-write over precise downstream tracking; version cache keys
  (embed an entity version) to dodge stale-write races entirely.
- Guard against cache stampede on a hot key's expiry with per-key locks,
  request coalescing, or jittered TTLs so concurrent misses don't all hit
  the store at once.

| Pattern | Freshness | Write latency | Failure risk |
|---|---|---|---|
| Cache-aside | Can be stale until next miss | Normal (store only) | Low |
| Write-through | Always fresh | Higher (cache + store) | Low |
| Write-behind | Always fresh in cache | Lowest | Data loss before flush |

## 9. Partitioning and Sharding

- Shard only once a single node can't hold the data or absorb the request
  rate — confirm with Section 3's numbers first; sharding adds real
  operational complexity.
- Range-based: contiguous key ranges per shard; great for scans, but skewed
  keys (sequential IDs, recent timestamps) create hot shards.
- Hash-based: `hash(key) mod N` spreads load evenly but loses range-scan
  locality and complicates resharding when N changes.
- Consistent hashing: nodes and keys sit on a hash ring; adding/removing a
  node remaps only a fraction of keys — the standard fix for painful
  resharding.
- Directory/lookup-based: a mapping service tracks which shard owns which
  range; most flexible, at the cost of an extra hop and a new critical
  service to protect.
- Mitigate hot partitions by salting keys that would cluster (append a
  random/hashed suffix) or isolating high-fanout entities onto dedicated
  shards.
- Cross-partition joins and multi-key transactions are expensive or
  impossible atomically; design access patterns to stay single-partition
  wherever requirements allow.

| Strategy | Pro | Con |
|---|---|---|
| Range | Efficient range scans | Hot spots on sequential keys |
| Hash | Even load distribution | Loses range locality, harder resharding |
| Consistent hashing | Minimal remap on scale change | More moving parts to operate |
| Directory-based | Flexible, custom rebalancing | Extra hop; lookup service is critical infra |

## 10. Replication

- Single-leader: all writes to one leader, followers replicate reads.
  Simple to reason about; the leader is a write bottleneck and failover
  target.
- Multi-leader: multiple nodes accept writes (often one per region); better
  write availability and latency across regions, but needs conflict
  resolution for concurrent edits.
- Leaderless/quorum: any replica accepts a write; a coordinator waits for
  acknowledgment from a quorum, trading strict ordering for high
  availability.
- Synchronous replication: leader waits for follower ack before confirming —
  stronger durability, higher latency, a slow follower can stall writes.
- Asynchronous replication: leader confirms immediately, replicates in the
  background — lower latency, but a crash before replication can lose the
  newest writes.
- Read replicas scale reads but add lag; a client reading its own recent
  write from a lagging replica can violate "read-your-writes" unless routed
  back to the leader or a caught-up replica.
- Failover has three real-time phases: detect the leader is truly down (not
  just slow), elect a new leader, promote and re-point clients — expect a
  brief unavailability window, not an instant switch.

| Style | Consistency | Availability | Typical use |
|---|---|---|---|
| Single-leader, sync | Strong | Lower (leader-bound) | Financial/ledger writes |
| Single-leader, async | Eventual on followers | Higher | Read-scaled web apps |
| Multi-leader | Eventual + conflict resolution | High, multi-region writes | Geo-distributed collaboration |
| Leaderless/quorum | Tunable via W/R | Very high | Highly available KV stores |

## 11. Consistency Models

- CAP theorem: during a network partition, choose Consistency or
  Availability — not both. Outside a partition, CAP says nothing, which is
  why PACELC exists.
- PACELC: if Partitioned, choose Availability or Consistency; Else (normal
  operation), choose Latency or Consistency — most real design trade-offs
  actually live in the "Else" branch.
- Strong/linearizable: every read sees the latest completed write, as if one
  copy existed. Easiest to reason about, priciest across regions.
- Eventual consistency: replicas converge once writes stop. Cheap and highly
  available; callers must tolerate temporarily stale reads.
- Causal consistency: causally related operations (a reply after a comment)
  are seen in order; unrelated ones may reorder — a practical middle ground.
- Read-your-writes / monotonic reads: narrower guarantees layered on
  eventual consistency so a user never sees their own action disappear or
  time-travel backward.
- Quorum formula for tunable consistency: with N replicas, W writes
  acknowledged and R reads consulted, `W + R > N` guarantees every read
  overlaps at least one up-to-date replica.

| Model | Guarantee | Example behavior |
|---|---|---|
| Strong/linearizable | Reads always reflect latest write | Single-leader sync replication |
| Sequential | All nodes agree on one global order | Coordinated distributed logs |
| Causal | Related events stay ordered | Comment threads, chat |
| Eventual | Converges once writes stop | DNS, most caches, leaderless stores |

## 12. Messaging

- Queue (point-to-point): each message consumed by exactly one worker in a
  pool — the natural fit for distributing work items.
- Publish/subscribe (fan-out): each message delivered to every interested
  subscriber — the natural fit for broadcasting events to independent
  services.
- Delivery semantics: at-most-once (fire and forget, can lose messages),
  at-least-once (safe default; consumers must dedupe), exactly-once
  (achieved practically via at-least-once delivery plus an idempotent
  consumer, not a transport guarantee).
- Ordering: most brokers guarantee order only within a partition/shard —
  pick a partition key (e.g., entity ID) so related events stay ordered.
- Backpressure: when producers outpace consumers, buffer with a bounded
  queue and a clear overflow policy, slow the producer, or shed low-priority
  load — never let an unbounded queue grow until the broker falls over.
- Dead-letter queues route repeatedly failing messages to a separate queue
  for inspection instead of retrying forever or silently dropping them.

| System | Model | Strength |
|---|---|---|
| Kafka-class log | Pub/sub, persistent log | Replay, very high throughput, per-partition order |
| RabbitMQ-class broker | Queue + flexible routing | Rich routing rules, per-message ack |
| Managed queue (SQS-class) | Queue | Fully managed, simple at-least-once delivery |
| Managed pub/sub (SNS-class) | Fan-out | Simple multi-subscriber broadcast |

## 13. Rate Limiting

- Purpose: protect shared infrastructure from abuse/overload, enforce fair
  usage across tenants, and cap third-party/API costs.
- Token bucket: tokens refill at a fixed rate up to a cap; a request
  consumes one — allows short bursts while capping sustained rate. Common
  default.
- Leaky bucket: requests queue and drain at a fixed output rate — smooths
  bursts into a constant rate, adds latency for bursty clients.
- Fixed window counter: count requests per fixed window; simple, but allows
  up to ~2x the limit right across a window boundary.
- Sliding window log/counter: tracks timestamps or a weighted blend of
  windows for accurate limiting without the boundary-burst problem, at
  higher memory cost.
- Enforce at multiple layers: edge/CDN (cheapest to reject early), gateway
  (per API key/tenant), and per-service (protect one expensive call).
- Distributed rate limiting needs a shared, low-latency counter store (e.g.,
  Redis with an atomic increment/Lua script) so gateway instances agree.
- Always respond with a clear contract: HTTP 429 plus a `Retry-After` header
  so well-behaved clients back off correctly.

| Algorithm | Handles bursts | Memory cost | Precision |
|---|---|---|---|
| Token bucket | Yes, up to bucket size | Low | Good |
| Leaky bucket | Smooths, no burst passthrough | Low | Good |
| Fixed window | Poorly (edge bursts) | Lowest | Weak |
| Sliding window | Yes | Higher | Best |

## 14. Reliability

- Eliminate single points of failure: every critical component needs at
  least one redundant instance (N+1) in an independently failing domain
  (rack, zone, region).
- Retries: exponential backoff with jitter so a transient failure doesn't
  become a synchronized retry storm; cap total retry time and make the
  operation idempotent first.
- Circuit breaker: after enough failures to a dependency, stop calling it
  for a cool-down and fail fast — protects the caller and gives the
  dependency room to recover.
- Bulkhead isolation: partition resources (thread/connection pools) per
  dependency so one slow downstream can't exhaust resources needed
  elsewhere.
- Timeouts: every network call needs an explicit, tight timeout; an
  unbounded call is a resource leak waiting to cascade.
- Graceful degradation: define a reduced-functionality fallback (stale
  cache, default value) instead of failing the whole request when a
  non-critical dependency is down.
- Health checks: liveness (alive? restart if not) vs. readiness (can serve
  traffic now? remove from rotation if not) are different checks with
  different consequences.
- SLI/SLO/SLA: an SLI is a measured indicator (e.g., p99 latency), an SLO is
  your internal target for it, an SLA is the external contract with a
  consequence for missing it — keep the SLO tighter than the SLA for
  error-budget room.
- Disaster recovery: define RTO (how fast you must recover) and RPO (how
  much data loss is tolerable); active-passive is cheaper but slower to
  fail over, active-active costs more but recovers near-instantly.

| Pattern | Failure it mitigates |
|---|---|
| Redundancy (N+1) | Single node/zone failure |
| Retry + backoff + jitter | Transient errors, brief blips |
| Circuit breaker | Cascading failure from a struggling dependency |
| Bulkhead | Resource exhaustion spreading across features |
| Timeout | Hung calls consuming resources indefinitely |

## 15. Security

- Separate authentication (who you are — password + MFA, OAuth2/OIDC,
  short-lived tokens) from authorization (what you may do — enforced per
  resource, at the smallest useful granularity).
- Encrypt in transit (TLS everywhere, including internal traffic in
  zero-trust setups) and at rest (volume encryption plus field-level
  encryption for the most sensitive data, keys managed by a dedicated KMS).
- Practice defense in depth: WAF/edge filtering, network segmentation
  (private subnets, security groups), and least-privilege IAM are
  independent layers — a failure in one shouldn't expose the whole system.
- Validate and sanitize all external input; never build queries or commands
  by concatenating untrusted input.
- Guard against broken object-level access control (the most common
  real-world break-in point): always verify the authenticated caller owns
  or may act on the specific resource ID requested, not just that they are
  logged in.
- Manage secrets in a dedicated vault/KMS with rotation — never in source
  code, images, or plaintext config committed to a repository.
- Rate limiting and input size limits double as security controls, capping
  the blast radius of malicious and accidental abuse alike.

| Risk | Mitigation |
|---|---|
| Credential theft | MFA, short-lived tokens, rotation |
| Injection (SQL/command) | Parameterized queries, strict input validation |
| Broken object-level access control | Per-request ownership/permission checks |
| Data exposure in transit/at rest | TLS everywhere; encryption at rest; field-level encryption |
| Secret leakage | Vault/KMS-managed secrets, no secrets in code |

## 16. Observability

- Three pillars: metrics (cheap, aggregated, great for trends/alerting),
  logs (detailed, per-event, great for root cause), traces (per-request path
  across services, great for finding where latency accumulates).
- Four golden signals per service: latency, traffic, errors, saturation —
  this short list catches most incidents.
- Structured logging (consistent key-value/JSON fields) beats free text for
  machine parsing; propagate a correlation/trace ID through every hop so
  logs and traces can be joined.
- Distributed tracing shows a request's full path and timing across
  services — indispensable once one user request touches more than two or
  three services.
- Alert on symptoms that affect users (elevated error rate, breached
  latency SLO) rather than every internal cause; symptom-based alerting cuts
  noise and alert fatigue, and root cause is found via the linked
  trace/dashboard once paged.
- Track SLO error-budget burn rate, not just a raw threshold, to distinguish
  a brief blip from a real trend.

## 17. Deployment

- Rolling deployment: replace old instances gradually; no extra
  infrastructure cost, but a bad release reaches a growing share of traffic
  before it's caught.
- Blue-green: run a full parallel environment, switch traffic at once —
  instant rollback, at roughly double the infra cost during the switch.
- Canary: send a small traffic slice to the new version, watch metrics, then
  ramp; slowest full rollout, smallest blast radius for a bad release.
- Shadow/dark traffic: mirror real traffic to a new version without
  returning its response, validating behavior under real load with zero
  user-facing risk.
- Feature flags decouple "deployed" from "released": ship dark, enable per
  cohort independent of the deploy pipeline, and get an instant kill-switch
  without a redeploy.
- Every strategy needs a rehearsed rollback and immutable, versioned build
  artifacts — "roll forward with a fix" is not a rollback plan under
  pressure.
- Keep environments close to identical (same infra-as-code definitions) so a
  passing staging run is actually predictive of production.

| Strategy | Rollback speed | Blast radius | Extra infra cost |
|---|---|---|---|
| Rolling | Moderate | Grows during rollout | None |
| Blue-green | Fast (instant switch) | All-or-nothing | High (temporary) |
| Canary | Fast for the canary slice | Small, controlled | Low |

## 18. Cost Trade-offs

- Compute: serverless costs more per unit of work but nothing when idle —
  cheapest for spiky/low-volume load; reserved VMs/containers are cheaper at
  sustained high utilization but billed whether traffic arrives or not.
- Storage tiering: moving rarely-read data from hot block storage to
  cold/archive is often an order of magnitude cheaper per GB, at the cost of
  minutes-to-hours retrieval latency — a strong default for logs, backups,
  old media.
- Build vs. buy: a managed service costs more per unit than self-hosting the
  open-source equivalent, but removes operational burden (patching, on-call,
  scaling expertise) — usually the right trade for small teams.
- Autoscaling vs. fixed capacity: fixed over-provisioning is simple and
  avoids scaling-lag risk but wastes money off-peak; autoscaling saves money
  but needs headroom and warm-up time for sudden spikes.
- Commit to a baseline you're confident you'll use for reserved-pricing
  discounts, and cover bursts with on-demand capacity on top.
- Data transfer (egress) and cross-region/cross-AZ traffic are frequently
  the most under-estimated cost line — minimize unnecessary hops and prefer
  same-zone traffic on the hot path.

| Decision axis | Cheaper option | More flexible/resilient option |
|---|---|---|
| Compute | Reserved/fixed capacity | Autoscaling / serverless |
| Storage | Cold/archive tier | Hot/replicated block storage |
| Ops model | Self-hosted OSS | Managed service |
| Regions | Single region | Multi-region active-active |

## 19. Architecture Blueprints

Six compact reference designs. Each diagram shows the primary request path;
notes below cover the parts left out of the picture (async paths, capacity,
and the key trade-off).

### 19.1 URL Shortener

```text
+--------+     +-----------------+     +-----------------------+
| Client | --> |  API / Redirect | --> |     Cache (Redis)     |
               |     Service     |
+--------+     +-----------------+     +-----------------------+
                  write |                          | miss
                        v                          v
               +-----------------+     +-----------------------+
               |   ID Generator  | --> | KV Store: code -> url |
               | (Snowflake/KGS) |     | (replicated, sharded) |
               +-----------------+     +-----------------------+

Async (fire-and-forget): API Service -> Analytics Queue -> Analytics DB
```

- Read:write ratio is heavily skewed (100:1+) — cache aggressively, serve
  redirects from the edge/CDN where possible.
- Storage is small (~500 bytes/record); a few hundred GB/year fits a KV
  store for a long time without sharding.
- Collision handling: base62-encode a Snowflake/counter ID, or hash + retry
  on collision.
- Trade-off: random codes resist enumeration but need a uniqueness check;
  sequential counters are simpler but predictable without extra obfuscation.

### 19.2 Chat System

```text
+--------+     +----------------+     +--------------+
| Client |<--> |   WS Gateway   | --> | Chat Service |
               | (holds socket) |
+--------+     +----------------+     +--------------+
              presence |
                       v
               +--------------+
               | Presence Svc |
               |   (Redis)    |
               +--------------+
                                             |
                                             v
                                   +------------------+
                                   |  Message Store   |
                                   | (partitioned by  |
                                   | conversation_id) |
                                   +------------------+
                                     fan-out |
                                             v
                                 +-----------------------+
                                 |  Other WS Gateways /  |
                                 | Push Notification Svc |
                                 |    (offline peers)    |
                                 +-----------------------+
```

- Sticky routing: presence maps a user to the gateway node holding their
  socket; cross-gateway fan-out uses a pub/sub layer for group chats.
- Order messages by a monotonic per-conversation sequence, not wall-clock
  time, so ordering survives clock skew.
- Offline delivery: store-and-forward with unread counters, falling back to
  push notification (APNs/FCM).
- Trade-off: at-least-once delivery plus client-side dedup by message ID is
  far simpler than chasing exactly-once over the wire.

### 19.3 Notification System

```text
+----------+     +----------------------+     +-------+
| Producer | --> | Notification Service | --> | Queue |
| Services |     |  (templates, prefs,  |
                 |     rate limits)     |
+----------+     +----------------------+     +-------+
                                                  |
                                      +-----------+-----------+
                                      |           |           |
                                      v           v           v
                                  +--------+  +--------+  +--------+
                                  |  Push  |  | Email  |  |  SMS   |
                                  | Worker |  | Worker |  | Worker |
                                  +--------+  +--------+  +--------+
                                      v           v           v
                                  APNs/FCM      SMTP     SMS Gateway

Failed sends -> Dead-Letter Queue -> retry with backoff -> alert after N tries
```

- Idempotency key per (user, event) avoids duplicate notifications on
  retries.
- Per-channel rate limiting respects provider quotas (APNs/FCM/SMS gateways
  all throttle independently).
- Priority queues split transactional sends (OTP, security alerts) from
  best-effort marketing batches.

### 19.4 File Storage

```text
+--------+     +--------------+     +-------------------------+
| Client | --> | Metadata Svc | --> |       Metadata DB       |
               | (tree, ACL,  |     | file_id, path, version, |
               |  versions)   |     |  owner, chunk_hashes[]  |
+--------+     +--------------+     +-------------------------+
    | chunk + hash
    v
+---------------+     +---------------------+     +-----+
| Block Storage | --> |     Object Store    | --> | CDN | --> Client (download)
|    Service    |     | (content-addressed, |
                      |    dedup by hash)   |
+---------------+     +---------------------+     +-----+
```

- Split large files into content-hashed chunks (e.g., ~4 MB) for dedup,
  resumable transfer, and delta sync.
- Metadata (small, needs transactions) is separated from blob bytes (large,
  immutable, cheap object storage).
- Trade-off: strong consistency on metadata (who owns the latest version)
  vs. eventually-consistent blob replication across regions.

### 19.5 Logging Pipeline

```text
+-----------+     +-------------+     +------------------+
| App Hosts | --> | Local Agent | --> |   Ingest Queue   |
|    (log   |     |   (buffer,  |     | (Kafka topic per |
|   files)  |     |    batch)   |     |   log stream)    |
+-----------+     +-------------+     +------------------+
                                               |
                                               +-----------------------------+
                                               v                             v
                                      +------------------+          +-----------------+
                                      | Stream Processor |          |   Cold Storage  |
                                      | (parse, enrich)  |          |  (object store, |
                                                                    | long retention) |
                                      +------------------+          +-----------------+
                                               |
                                               v
                                    +--------------------+
                                    | Search/Index Store |  <-- Query UI / Alerting
                                    |  (hot, short TTL)  |
                                    +--------------------+
```

- Two-tier retention: hot indexed store for fast recent search (expensive),
  cold object storage for compliance/audit (cheap, slow retrieval).
- Backpressure: agents buffer and drop lowest-priority logs first if the
  queue saturates — never block the application.
- Sample very high-volume debug logs; always keep 100% of error/warn
  levels.

### 19.6 Monitoring System

```text
+------------+     +-------------------+     +----------------+
|  Services  | <-- | Metrics Collector | --> | Time-Series DB |
| (/metrics) |                               | (downsampled)  |
+------------+     +-------------------+     +----------------+
                                                     |
                                                     +-----------------------------+
                                                     v                             v
                                             +----------------+          +--------------------+
                                             | Alerting Rules |          |     Dashboards     |
                                             |     Engine     |          | (latency, traffic, |
                                                                         |   errors, sat.)    |
                                             +----------------+          +--------------------+
                                                     |
                                                     v
                                           +-------------------+
                                           |  On-call Paging / |
                                           | Chat Notification |
                                           +-------------------+
```

- Pull-based scraping simplifies service discovery; push-based (via a local
  agent) suits short-lived/serverless jobs better.
- Store raw metrics briefly, downsample for long-term trend storage to
  control cost.
- Alert on symptoms (the four golden signals) rather than every internal
  cause to reduce noise.

## 20. Common Pitfalls

- Diagramming before clarifying requirements — locks in assumptions the
  interviewer never validated.
- Treating every prompt as "internet scale" regardless of stated user
  count — over-engineering wastes time and signals poor judgment about cost.
- Skipping back-of-envelope math entirely, or chasing false precision no
  interviewer expects — a rough number said out loud beats silence.
- Drawing a single instance of a critical component (one DB, one load
  balancer) without acknowledging it's a single point of failure.
- Saying "we'll use eventual consistency everywhere" without checking
  whether the specific use case (payments, inventory) actually tolerates it.
- Presenting one design as the only correct answer instead of naming the
  rejected alternative and why.
- Forgetting security, observability, and deployment until the interviewer
  asks directly — mention them briefly even in a time-boxed answer.
- Reaching for microservices, Kafka, or Kubernetes because they sound
  senior, not because the stated requirements demand them.
- Choosing a partition/shard key without checking for hot keys (celebrities,
  viral content, sequential IDs).
- Treating the cache as a database — no invalidation strategy, no plan for
  a cache-wide flush.
- Never discussing what happens when a specific component fails; the
  interview wants your failure-mode reasoning, not just the happy path.
- Ignoring the interviewer's hints about where to go deeper, and instead
  re-explaining the whole system at the same shallow depth.
- Losing track of time and leaving no minutes for a wrap-up summary.
- Designing the "perfect" system while ignoring the stated team size,
  budget, or timeline constraints.
- Not stating assumptions out loud — if you must guess a number, say the
  guess and its impact on the design.

## 21. Rapid-Fire Q&A

Q: SQL or NoSQL for a social media news feed?

Answer:
Usually both: a relational or document store as the source of truth for
posts and relationships, with a denormalized, precomputed feed cached in a
fast key-value store, since feed reads vastly outnumber writes.

Q: How do you avoid a single point of failure at your load balancer?

Answer:
Run at least two load balancer instances in different failure domains
behind a floating IP or DNS-based failover, so losing one instance does not
take down ingress traffic.

Q: When would you choose a document store over a relational database?

Answer:
When the schema is flexible or nested per entity, access is mostly
per-document, and you don't need multi-record transactions or joins across
entities.

Q: How do you avoid the thundering herd problem on a popular cache key?

Answer:
Coalesce requests so only one caller recomputes the value while others
wait, add jitter to TTLs so keys don't expire in lockstep, and consider a
short stale-while-revalidate window.

Q: What's the difference between horizontal and vertical scaling?

Answer:
Vertical scaling adds capacity to one node and is simple but capped and
still a single point of failure; horizontal scaling adds more nodes and
scales further but requires the system to be partition- and
coordination-aware.

Q: How would you design idempotent APIs for payments?

Answer:
Require a client-generated idempotency key per logical operation, store the
key with the resulting response, and return the stored response on any
retry with the same key instead of re-executing the charge.

Q: Why prefer cursor-based pagination over offset-based for a live feed?

Answer:
Offset pagination shifts results when rows are inserted or deleted between
pages, causing skipped or duplicated items; a cursor referencing the last
seen sort key stays stable under concurrent writes.

Q: What causes replication lag, and why does it matter?

Answer:
Network delay and follower load in propagating a write from leader to
followers; it matters because a client can read stale data from a lagging
replica right after writing, violating read-your-writes expectations.

Q: How do you pick between a message queue and a pub/sub topic?

Answer:
Use a queue when exactly one worker should process each message (task
distribution); use pub/sub when multiple independent services must each
react to the same event.

Q: What is the practical way to achieve "exactly-once" processing?

Answer:
Combine at-least-once delivery from the broker with an idempotent consumer
that deduplicates by a unique message ID, since no distributed transport
guarantees exactly-once delivery on its own.

Q: How do you detect and handle a hot partition?

Answer:
Monitor per-key request rates, then mitigate by salting the key with a
random or hashed suffix, splitting the hot entity across multiple keys, or
isolating known-hot entities onto dedicated shards.

Q: What's the difference between RTO and RPO?

Answer:
RTO (Recovery Time Objective) is how long you can be down before recovery
must complete; RPO (Recovery Point Objective) is how much recent data you
can afford to lose, which sets your backup/replication frequency.

Q: Why use a CDN even for a mostly dynamic application?

Answer:
It still offloads static assets, TLS termination, and DDoS absorption from
your origin, and can cache even short-TTL dynamic responses at the edge,
cutting both latency and origin load.

Q: How do you rate-limit fairly across many tenants sharing one API?

Answer:
Key the limiter by tenant or API key rather than globally, store counters
in a shared low-latency store like Redis so all gateway instances agree,
and return 429 with Retry-After so clients back off correctly.

Q: What's the risk of synchronous service-to-service calls in a request path?

Answer:
Each added synchronous hop adds its own latency and failure probability
directly to the caller's response time and availability; a single slow or
failing dependency can cascade into a full outage without timeouts and
circuit breakers.

Q: When is strong consistency worth its latency cost?

Answer:
Whenever a stale read causes real harm — inventory counts that can oversell,
financial balances, authorization checks — the correctness requirement
outweighs the extra coordination latency.

Q: How do you version an API without breaking existing clients?

Answer:
Add new fields and endpoints additively, never repurpose or remove an
existing field's meaning, and introduce a new version path only for
genuinely breaking changes, with a published deprecation timeline for the
old one.

Q: What's a circuit breaker protecting against, exactly?

Answer:
It protects the caller from wasting resources on an already-failing
dependency, and protects that dependency from being overwhelmed by
continued retries, by failing fast during a cool-down window.

Q: How would you shrink storage costs for years of log data?

Answer:
Keep a short hot window in an indexed, searchable store for active
debugging, then move older data to cheap cold/archive object storage under
a defined retention and deletion policy.

Q: What is a bulkhead, and why does it matter?

Answer:
A resource isolation boundary — separate thread pools, connection pools, or
service instances per dependency — that keeps one slow or failing
downstream from exhausting resources needed by unrelated features.

Q: How do you keep a WebSocket-based chat system horizontally scalable?

Answer:
Keep gateway nodes stateless about business logic and holding only the
socket; maintain a shared presence registry mapping each user to their
current gateway node, and fan out messages between gateways via a pub/sub
layer.

Q: Why choose eventual consistency for a "likes" counter but not a bank balance?

Answer:
A slightly stale like count is invisible and harmless, while a stale
balance can allow overdraft or double-spend — the cost of being wrong, not
the technology, should decide the consistency model.

Q: What's the fastest way to reduce blast radius during a bad deploy?

Answer:
Ship behind a feature flag or as a small canary slice, watch the golden
signals, and keep a rehearsed, fast rollback path so a bad release affects
the fewest users for the shortest time.

Q: How do you estimate whether you need to shard a database?

Answer:
Compare projected peak QPS and total data size against a single well-tuned
node's realistic ceiling from your capacity formulas; shard only once you
are genuinely approaching that ceiling, since sharding adds real
operational and query complexity.

Q: What should you say when you don't know an exact number in the interview?

Answer:
State a clearly labeled rough estimate and its source, then keep reasoning
from it — silence or refusing to guess is worse than a transparent,
reasonable estimate.

Q: How do you keep notification delivery reliable across third-party providers?

Answer:
Enqueue with retries and exponential backoff per channel worker, route
persistently failing sends to a dead-letter queue for inspection, and track
per-provider success rates so a struggling provider doesn't silently drop
notifications.

## 22. Final Checklist

Before you call a system design interview finished, confirm out loud:

- Requirements restated in your own words, with explicit non-goals.
- At least one estimation number computed live, not asserted.
- API/data contract sketched with 3–6 concrete endpoints or messages.
- One clear end-to-end request path drawn on the board.
- At least one deep technical trade-off discussed, not just asserted.
- A named failure mode and its mitigation.
- A brief mention of security, observability, deployment, and cost.
- A closing summary sentence — never let the interview trail off in silence.
