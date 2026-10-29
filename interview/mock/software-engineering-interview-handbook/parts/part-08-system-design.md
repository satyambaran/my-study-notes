# Part VIII — System Design

## About This Part

System design interviews and real system design work share one skill:
turning an ambiguous goal into a working architecture under real
constraints of traffic, money, failure, and time. This part builds that
skill in two layers: four cross-cutting properties —
**Scalability**, **Availability**, **Consistency**, **Caching** —
giving a shared vocabulary for trade-offs, then five applied case
studies — **Rate Limiter**, **URL Shortener**, **Chat System**,
**Notification System**, **File Storage** — that apply those properties
under concrete numbers, APIs, and failure scenarios. It opens with
**Design Fundamentals**, the loop used to approach any of the above, and
closes with **Logging** and **Monitoring**, the operational layer that
keeps a design honest after it ships.

Read Design Fundamentals first. Beginners: focus on "What is it?"
through "Visualization" for a correct mental model. Intermediate
readers: study "Trade-offs," "Alternatives," "Performance Analysis" to
see why a design won. Advanced readers: focus on "Production
Engineering Perspective," where the concept meets real failure, scale,
and on-call reality.

## Interview Framework

Every system design conversation — interview or real RFC — runs the
same loop at different scales. Treat it as a checklist, not a script:
skip a step already answered, but never skip one silently.

1. **Clarify requirements.** Separate functional (what it must do) from
   non-functional (latency, availability, consistency, durability)
   requirements; state assumptions and confirm them.
2. **Estimate capacity.** Convert user-facing numbers (DAUs,
   actions/day) into QPS, storage, and bandwidth via back-of-envelope
   math — this decides single database versus sharded fleet.
3. **Define interfaces and data model.** Sketch the API contract and
   core entities/schema to force precision about what is stored.
4. **Sketch high-level architecture.** Draw boxes (client, load
   balancer, service, cache, database, queue) and arrows between them —
   correct and simple beats impressive and broken.
5. **Deep-dive and find the bottleneck.** Pick the component most likely
   to fail or saturate first, and redesign around it — sharding,
   caching, async processing, replication.
6. **Discuss trade-offs and evolution.** Explain what changes at 10x and
   100x scale, what was deliberately left unsolved, and why.

```text
0            5           15                 35            45   min
|--clarify---|--estimate--|-----design-------|--deep dive--|-wrap-|
   (step 1)     (step 2)     (steps 3-4)        (step 5)   (step 6)
```

The time budget is a default, not a law: interviewers often cut steps
1-2 short once a candidate shows fluency, spending most remaining time
in steps 4-6.

| Chapters in this part | Role in the loop |
|---|---|
| Design Fundamentals | Teaches the loop itself and how interviewers grade it |
| Scalability, Availability, Consistency, Caching | Cross-cutting concepts used during steps 4-6 of every case study |
| Rate Limiter, URL Shortener, Chat System, Notification System, File Storage | Applied case studies exercising the full loop end to end |
| Logging, Monitoring | The operational layer keeping any of the above alive after launch |

Communicate the loop rather than silently executing it: say what step
you are on, why you are estimating a number, and what you are trading
away. An interviewer cannot grade thinking they cannot hear.

## Requirements-to-Operations Roadmap

A whiteboard design compresses a much longer real lifecycle. Production
systems move through the same stages repeatedly, widening in scope as
traffic and requirements grow.

```text
Requirements --> Capacity --> Interface/Data --> Architecture --> Reliability
     ^              Planning      Modeling         & Scaling      & Security
     |                                                                 |
     |                                                                 v
     +------------------- Evolution <-- Operations <-- Observability --+
                        (10x / 100x)      (on-call)     (logs, metrics,
                                                             traces)
```

| Stage | Key questions | Primary chapters |
|---|---|---|
| Requirements & scoping | What must it do, for whom, under what SLA? | Design Fundamentals |
| Capacity planning | What is QPS, storage, and bandwidth today and at peak? | Scalability, and capacity estimation in every case study |
| Interface & data modeling | What APIs and schemas does the system own? | Rate Limiter, URL Shortener, Chat System, Notification System, File Storage |
| Architecture & scaling | How do components split load without a choke point? | Scalability, Caching |
| Reliability & correctness | What fails, how do we detect it, what does a stale read mean? | Availability, Consistency |
| Security & compliance | Who can call what, and how do we stop abuse? | Woven into every case study's Production Engineering Perspective |
| Observability & operations | How do we know it is healthy, and debug it when not? | Logging, Monitoring |
| Evolution | What breaks first at 10x traffic, and what would we rebuild? | Production Engineering Perspective in every chapter |

Two habits separate a design that works from one that survives
production: treat every stage as continuously revisited (a monitoring
gap found during an incident becomes next quarter's requirement), and
size the architecture for the requirement actually given, not an
imagined future one — over-engineering for scale that never arrives
costs as much as under-engineering for the scale that did.

---

# Design Fundamentals

## What is it?

The reusable habits, vocabulary, and framework for turning an ambiguous
requirement into a concrete architecture — what to clarify, measure,
and trade off.

## Why do we need it?

Without it, conversations drift into technology choices before anyone
states the scale or non-functional requirements the architecture must
satisfy.

## What problem does it solve?

It solves the "blank page" problem: given only "design a chat system,"
fundamentals convert unbounded ambiguity into a small set of defensible
decisions.

## History (only if useful)

System design interviews grew alongside the scale-out web, as Google,
Amazon, and Meta needed engineers who could reason beyond one machine —
mirroring how real orgs write RFCs before building.

## How does it work?

### The Six-Step Loop

Clarify requirements, estimate capacity, define interfaces/data, sketch
architecture, deep-dive on bottlenecks, discuss trade-offs — the loop
introduced in this part's Interview Framework.

### Requirements

Split requirements into **functional** (what the system does) and
**non-functional** (latency, availability, consistency, cost) — the
latter usually drives the architecture.

### Capacity Estimation

Standard inputs: DAU, actions/user/day, payload size, read:write ratio,
peak multiplier (2-10x). Example: 50M DAU x 20 actions/day / 86,400s
~= 11,600 QPS average; at 5x peak, ~= 58,000 QPS.

### API and Data Model

A rough API (`POST /resource`, `GET /resource/{id}`) plus a rough schema
(three to five entities) forces precision about what the system stores
versus computes on demand.

## Internal Working

### Generic Layered Architecture

```text
Client -> DNS/GSLB -> Load Balancer -> Stateless App Tier -> Cache
                                            |                  |
                                            v                  v
                                        Primary DB  <---- Read Replicas
                                            |
                                            v
                                     Async Workers/Queue -> Object Storage
```

### Component Responsibilities

The load balancer distributes and health-checks traffic; the app tier
is stateless; the cache absorbs reads; the database is the source of
truth; queues decouple slow work.

### Request Flow

1. Client resolves the nearest endpoint.
2. Load balancer picks a healthy instance.
3. App checks cache; on a miss, reads the DB and populates the cache.
4. Non-critical side effects go to a queue.
5. Response returns within a low-double-digit millisecond budget.

## Step-by-Step Example

Apply the loop to a pastebin: requirements — create/retrieve a snippet
by ID; estimate — 1M snippets/day, mostly read-after-create; API —
`POST /paste {text, ttl}` -> `{id}`, `GET /paste/{id}` -> text;
architecture — app tier, key-value store, cache/CDN for reads;
trade-off — no relational database needed since pastes have no
relationships.

## Visualization

```text
        Requirements        Capacity          Architecture
        -----------         --------           ------------
  who & what?           how much load?       which boxes, arrows?
        |                     |                      |
        v                     v                      v
   +-----------+        +-----------+          +--------------+
   | Func/Non- |------->|  QPS,     |--------->| Boxes/arrows +|
   | func reqs |        |  storage  |          | one bottleneck|
   +-----------+        +-----------+          +--------------+
```

## Real-World Usage

Every large engineering org runs this loop in RFCs and design reviews;
staff engineers are graded on quantifying scale before proposing an
architecture.

## Advantages

- Forces requirements before architecture, preventing wrong-problem
  solutions.
- Gives reviewers shared vocabulary for specific critique.
- Scales from a 45-minute interview to a multi-week RFC.

## Disadvantages

- Can become a checklist recited without judgment.
- Encourages rigid ordering when conversations are iterative.
- Over-indexes on arithmetic when the problem is architectural.

## Trade-offs

A framework trades spontaneity for repeatability — comparable,
reviewable designs, at the risk that rigid adherence crowds out the one
creative decision that makes a design excellent.

## Alternatives

Event storming starts from business events, not a checklist; the C4
model emphasizes diagrams over questions; ADRs document decisions after
the fact rather than guiding the conversation.

## Performance Analysis

The loop allocates roughly 10-15% of interview time to
requirements/estimation, 40-50% to architecture and deep-dive, the rest
to trade-offs; over a third on requirements signals poor time
management.

## Complexity Analysis (if applicable)

An O(1) lookup on one node becomes O(log N) or O(N) once data is
sharded and a scatter-gather query is needed — recognizing this shift
is a core skill.

## Implementation

```java
enum DesignStep {
    CLARIFY_REQUIREMENTS("Functional vs non-functional, explicit scale"),
    ESTIMATE_CAPACITY("QPS, storage, bandwidth from back-of-envelope math"),
    DEFINE_INTERFACE("APIs and the core data model"),
    SKETCH_ARCHITECTURE("Boxes, arrows, request flow"),
    DEEP_DIVE("Bottlenecks, failure modes, one or two components in detail"),
    TRADE_OFFS("Alternatives, evolution at 10x/100x scale");

    final String focus;
    DesignStep(String focus) { this.focus = focus; }
}
```

This checklist is code, not prose — the same object a senior engineer
mentally iterates during a design review.

## Common Mistakes

- Proposing architecture before stating any requirement.

## Common Interview Questions

Q: How should you start a system design interview?

Answer:
Clarify functional and non-functional requirements and confirm scope
before drawing anything — naming assumptions turns a guess into a
checkable premise.

Q: What is the difference between functional and non-functional requirements?

Answer:
Functional requirements describe what the system does; non-functional
requirements describe required qualities (latency, availability,
consistency, cost) and usually drive the harder decisions.

## Follow-up Interview Questions

Q: How would you change your design if traffic increased 100x?

Answer:
Identify which layer saturates first — typically the database — then
apply the matching lever (replicas, sharding, a distributed cache,
horizontal scaling), re-running the capacity math.

## How the Interviewer Thinks

The interviewer is rarely grading a specific technology choice; they
are grading whether you convert ambiguity into a structured process and
adapt as constraints appear.

## Production Engineering Perspective

Real design docs follow the same loop on paper: an RFC states
requirements, includes a capacity section with real projections, and
gets reviewed for bottlenecks and failure modes.

## Common Misconceptions

- "There is one correct answer" — most designs are defensible if
  trade-offs are understood and stated.

## Revision Notes

- Loop: clarify -> estimate -> interface/data -> architecture ->
  deep-dive -> trade-offs.

## Summary

Design fundamentals are a repeatable loop — requirements, estimation,
interfaces, architecture, deep-dive, trade-offs — converting an
ambiguous prompt into a defensible design, mirroring how real orgs plan
systems.

---

# Scalability

## What is it?

A system's ability to handle growing load — more users, requests, data
— by adding resources, with cost and latency growing sub-linearly.

## Why do we need it?

Single machines have hard ceilings on CPU, memory, disk IOPS, and
network bandwidth; successful products outgrow those ceilings within
months.

## What problem does it solve?

It turns "10x more users" from an existential rewrite into an
operational exercise: add nodes, shards, or cache capacity.

## History (only if useful)

Early scaling meant buying a bigger mainframe (vertical). Google and
Amazon's move to commodity hardware and service-oriented architecture
popularized horizontal scaling; cloud elasticity made it on-demand.

## How does it work?

### Vertical vs Horizontal Scaling

Vertical scaling adds CPU/RAM/disk to one machine — simple, but bounded
by the largest available hardware and a single point of failure.
Horizontal scaling adds more machines and distributes load — effectively
unbounded, at the cost of coordination complexity.

### Statelessness

Horizontal scaling is easiest when the app tier is stateless: any
instance serves any request because session state lives in a shared
store, not server memory. Stateful services need partitioning instead.

### Capacity Estimation

Example: a service at 5,000 QPS average with a 4x peak factor must
sustain 20,000 QPS. If one instance handles 1,000 QPS, the fleet needs
20 instances plus 30-50% headroom — roughly 26-30 instances.

### Partitioning (Sharding)

When one database can no longer hold the throughput, data is split
across shards by a partition key (user ID, region, hash), each an
independent database handling a fraction of the load.

## Internal Working

### Scaled-Out Architecture

```text
                 +------------------+
Clients -------->|  Global LB/DNS   |
                 +---------+--------+
                           |
             +-------------+-------------+
             v                           v
      +-------------+            +-------------+
      | App Cluster |            | App Cluster |
      |  (Region A) |            |  (Region B) |
      +------+------+            +------+------+
             |                          |
      +------v------+            +------v------+
      |  Cache Tier |            |  Cache Tier |
      +------+------+            +------+------+
             |                          |
      +------v-------------------------v------+
      |   Sharded DB (consistent hashing)      |
      |  Shard 0 | Shard 1 | Shard 2 | Shard 3 |
      +-----------------------------------------+
```

### Consistent Hashing for Routing

Rather than `hash(key) % N` (which reshuffles almost every key when N
changes), consistent hashing places shards and keys on a ring; adding or
removing a shard only remaps the adjacent keys.

### Request Flow

1. The request hits the global load balancer, routed to the nearest
   healthy region.
2. The regional load balancer picks an app instance.
3. The instance hashes the partition key to find the owning shard/cache
   node.
4. The cache is checked first; on a miss, the shard is read and the
   cache populated.

## Step-by-Step Example

Scaling from 100 QPS to 100,000 QPS: at 100 QPS a single server and
database suffice. At 5,000 QPS, add a load balancer, read replicas, and
a cache. At 20,000 QPS the write path saturates a single primary, so
shard by user ID. At 100,000 QPS, add a CDN/edge layer and go
multi-region.

## Visualization

```text
Vertical:  [1 CPU]->[4 CPU]->[32 CPU]->(hardware ceiling)
Horizontal:[1 node]->[10 nodes]->[100 nodes]->(near-linear, coordination cost grows)

Autoscaling loop:
  metric (CPU/QPS) -> controller -> compare to target -> add/remove instances -> repeat
```

## Real-World Usage

Kubernetes' Horizontal Pod Autoscaler scales stateless services on
CPU/custom metrics; Instagram sharded Postgres by user ID early on;
DynamoDB and Cassandra add partitions transparently.

## Advantages

- Near-linear capacity growth by adding commodity nodes.
- Fault isolation: losing one node loses a fraction of capacity.
- Elastic cost: scale down during low traffic.

## Disadvantages

- Distributed complexity: partitions, partial failures, coordination
  overhead.
- Sharded data complicates cross-shard joins, transactions, and indexes.
- Rebalancing shards is operationally risky and can cause hot spots.

## Trade-offs

Horizontal scalability trades operational simplicity for near-unbounded
growth; over-provisioning wastes money while under-provisioning risks
outages.

## Alternatives

Vertical scaling still suits genuinely bounded workloads. Functional
decomposition (splitting by service) scales development velocity, a
different axis from data partitioning; most large systems need both.

## Performance Analysis

Amdahl's Law caps speedup from parallelism: a serial fraction *s* of
work bounds speedup at 1/s regardless of node count. The Universal
Scalability Law adds a coordination term, explaining why throughput can
decrease past a certain node count.

## Complexity Analysis (if applicable)

Naive modulo sharding remaps O(N) of N keys when shard count changes;
consistent hashing bounds remapping to O(N/M) for M shards.

## Implementation

```java
final class ConsistentHashRing<T> {
    private final SortedMap<Long, T> ring = new TreeMap<>();
    private final int virtualNodes;

    ConsistentHashRing(int virtualNodes) { this.virtualNodes = virtualNodes; }

    void addNode(T node) {
        for (int i = 0; i < virtualNodes; i++) {
            ring.put(hash(node.toString() + "#" + i), node);
        }
    }

    T route(String key) {
        long h = hash(key);
        SortedMap<Long, T> tail = ring.tailMap(h);
        Long nodeHash = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(nodeHash);
    }

    private long hash(String s) {
        return java.util.zip.CRC32.class.hashCode() ^ s.hashCode();
    }
}
```

Virtual nodes (multiple ring entries per physical node) smooth out load
that a single hash point per node would leave lumpy.

## Common Mistakes

- Scaling the app tier while the database remains a single bottleneck.

## Common Interview Questions

Q: What is the difference between vertical and horizontal scaling?

Answer:
Vertical scaling adds power to one machine — simple but hardware-bounded
and a single point of failure; horizontal scaling adds machines and
distributes load — unbounded but with coordination complexity.

Q: How do you scale a read-heavy system?

Answer:
Add read replicas and a caching layer, pushing cacheable content to a
CDN if public; the main challenge becomes keeping replicas and caches
acceptably fresh.

## Follow-up Interview Questions

Q: How does Amdahl's Law limit horizontal scalability in practice?

Answer:
Any serial portion of work — a shared lock, a single coordinator — caps
maximum speedup regardless of node count, so eliminating serial
bottlenecks matters more than adding nodes.

## How the Interviewer Thinks

The interviewer checks whether the candidate locates the actual
bottleneck rather than reflexively proposing "add more servers," and
understands specific mechanics rather than only the vocabulary.

## Production Engineering Perspective

Production teams autoscale on a validated metric (queue depth or
latency, not just CPU) and keep a 30-50% capacity buffer for failover
and deploy safety. Bottlenecks typically surface at the database long
after the app tier scales cleanly. The evolution path is monolith ->
replicas -> cache -> sharded data -> multi-region.

## Common Misconceptions

- "Adding more servers always fixes performance" — only if the
  bottleneck is instance count, not a shared database or lock.

## Revision Notes

- Vertical: simple, bounded. Horizontal: near-unbounded, more
  coordination.

## Summary

Scalability is about adding resources — vertically or horizontally — to
absorb growing load without degrading latency or cost; the central skill
is finding the true bottleneck and applying the matching lever.

---

# Availability

## What is it?

The proportion of time a system correctly responds to requests,
expressed as a percentage over a period (99.9%, 99.99%) — "nines."

## Why do we need it?

Any component — server, disk, network link, data center — fails
eventually, and unavailability directly costs revenue and trust.

## What problem does it solve?

It solves how to keep serving correct responses despite some component
failing at any moment, by removing single points of failure and
automating detection and recovery.

## History (only if useful)

"Five nines" (99.999%) originated as a telephone-network reliability
target. Cloud providers formalized availability as a contractual SLA in
the mid-2000s, turning an engineering goal into a credit-backed promise.

## How does it work?

### The Math of Nines

99% allows ~3.65 days/year downtime; 99.9% ~8.76 hours; 99.99% ~52.6
minutes; 99.999% ~5.26 minutes. Dependent components in series multiply
availabilities (two 99.9% services in sequence yield ~99.8% combined).

### Redundancy Math

Independent redundant components in parallel combine as `1 - (1-A)^n`:
two components each at 99% availability, active-passive, yield `1 -
(0.01)^2 = 99.99%` combined.

### Requirements

Functional: continue serving during a single-node, AZ, or region
failure. Non-functional: a target Recovery Time Objective (RTO) and
Recovery Point Objective (RPO — acceptable data loss).

## Internal Working

### Multi-AZ Active-Active Architecture

```text
                +-------------------+
                |  Global LB / DNS  |
                +---------+---------+
                          |
        +-----------------+-----------------+
        v                                   v
 +-------------+                     +-------------+
 |   AZ A      |                     |   AZ B      |
 | App + Cache |<---health checks--->| App + Cache |
 +------+------+                     +------+------+
        |                                   |
        +---------------+-------------------+
                         v
              +---------------------+
              | Replicated Database |
              | (sync or async)     |
              +---------------------+
```

### Failure Detection and Failover

Health checks mark an instance or AZ unhealthy; the load balancer stops
routing to it. Database failover promotes a replica to primary,
automatically (fast RTO, some risk) or with human confirmation.

### Request Flow During Failure

1. AZ A fails; health checks fail within seconds.
2. The load balancer evicts AZ A's instances.
3. Traffic shifts entirely to AZ B.
4. A replica in AZ B is promoted if the primary was in AZ A; clients
   reconnect and retry.

## Step-by-Step Example

A region hosting the primary database goes dark. Health checks fail
after three consecutive misses (~15 seconds). The global load balancer
stops routing there. A standby replica elsewhere is promoted, clients
with retry-with-backoff reconnect, and failover completes in under a
minute.

## Visualization

```text
Healthy --(failures detected)--> Degraded --(failover triggered)--> Failing Over
   ^                                                                      |
   +-----------------------(replica promoted, traffic verified)----------+
```

## Real-World Usage

Managed databases automate multi-AZ replica promotion; Netflix's chaos
engineering deliberately fails regions in production to prove failover
works; globally distributed databases replicate synchronously to
survive a full region loss.

## Advantages

- Survives hardware, AZ, and regional failures without a full outage.
- Builds trust; often a contractual SLA requirement.

## Disadvantages

- Redundancy costs 2-3x infrastructure for active-active designs.
- Failover logic is complex and can itself fail (split-brain, false
  positives).

## Trade-offs

Availability trades against consistency during partitions and against
cost: each additional "nine" is disproportionately more expensive than
the last.

## Alternatives

Graceful degradation (serve stale data or reduced functionality) avoids
full redundancy cost for non-critical paths. A single-region design
with a tested disaster-recovery plan accepts a slower RTO for lower
steady-state cost.

## Performance Analysis

Cross-region synchronous replication (needed for zero-loss failover)
adds real latency to every write, often tens of milliseconds;
asynchronous replication removes that cost but risks losing the last
few transactions during failover.

## Complexity Analysis (if applicable)

Not algorithmic; overall availability is the product/parallel-
combination of every dependency's availability, so the weakest,
most-depended-upon component dominates the achievable ceiling.

## Implementation

```java
final class CircuitBreaker {
    enum State { CLOSED, OPEN, HALF_OPEN }
    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private long openedAtMillis;
    private final int failureThreshold;
    private final long resetTimeoutMillis;

    CircuitBreaker(int failureThreshold, long resetTimeoutMillis) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
    }

    boolean allowRequest() {
        if (state == State.OPEN
                && System.currentTimeMillis() - openedAtMillis > resetTimeoutMillis) {
            state = State.HALF_OPEN;
        }
        return state != State.OPEN;
    }

    void recordSuccess() { consecutiveFailures = 0; state = State.CLOSED; }

    void recordFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            openedAtMillis = System.currentTimeMillis();
        }
    }
}
```

A circuit breaker protects availability by failing fast against a broken
dependency instead of letting callers queue on timeouts.

## Common Mistakes

- No automated health checks, so a dead instance keeps receiving
  traffic.

## Common Interview Questions

Q: What does "five nines" mean?

Answer:
99.999% availability, about 5.26 minutes of downtime per year; each
additional nine cuts allowed downtime roughly 10x and costs
disproportionately more effort.

Q: How do you achieve high availability?

Answer:
Eliminate single points of failure through redundancy across instances,
AZs, or regions, detect failures with automated health checks, and
automate failover so recovery does not depend on a human noticing.

## Follow-up Interview Questions

Q: How do you avoid split-brain in an active-active setup?

Answer:
Use a quorum-based mechanism (an odd number of voting members, or a
consensus service) so only one side can hold a majority during a
partition; fencing tokens ensure a demoted former primary cannot keep
writing.

## How the Interviewer Thinks

The interviewer probes whether the candidate treats failure as the
default state to design for, not an edge case, and can name concrete
mechanisms rather than only saying "we'll make it redundant."

## Production Engineering Perspective

Teams operationalize availability through error budgets, runbooks, and
regular game days rehearsing failover. Bottlenecks often hide in shared
control-plane components (one config service or DNS provider) that
undermine otherwise redundant data planes. The evolution path is single
instance -> multi-AZ -> multi-region active-passive -> active-active.

## Common Misconceptions

- "The cloud provider guarantees 100% uptime" — SLAs offer credits for
  breach, not a guarantee of zero downtime.

## Revision Notes

- Nines math: 99.9% ~8.76h/year down; 99.99% ~52.6 min; 99.999% ~5.26
  min.

## Summary

Availability engineering assumes failure is constant and designs
redundancy, detection, and automated failover around that assumption;
the real cost is the complexity of failover logic itself, which must be
tested regularly to be trusted.

---

# Consistency

## What is it?

What value a read is allowed to return relative to prior or concurrent
writes, especially when data is replicated or cached across nodes.

## Why do we need it?

Replication and caching improve availability and latency but create
multiple copies of the same data; without an explicit model, an
application cannot reason about whether a read reflects the latest
write.

## What problem does it solve?

It maps business meaning to engineering guarantee: a bank balance and a
"like" count both live in a distributed store but tolerate staleness
very differently.

## History (only if useful)

Single-node relational databases guaranteed strong consistency for
decades via ACID. Amazon's Dynamo (2007) popularized tunable, eventually
consistent stores; Brewer's CAP theorem (2000), proven by Gilbert and
Lynch (2002), named the trade-off.

## How does it work?

### The Consistency Spectrum

From strongest to weakest: linearizable/strict -> sequential -> causal
(causally related operations seen in the same order by everyone) ->
read-your-writes/session -> eventual (replicas converge only once writes
stop). Stronger guarantees cost more coordination and latency.

### Quorum Systems

For N replicas, a write is acknowledged by W of them and a read queries
R of them. Choosing `W + R > N` guarantees every read overlaps with the
most recent write on at least one replica.

### Requirements and Data Model

Functional: define per-field consistency needs (strong for balance,
eventual for view count). Non-functional: acceptable staleness window
and latency budget per read/write.

## Internal Working

### Quorum Read/Write Flow

```text
Write(key, v):  client -> coordinator -> replicas {1,2,3}
                 waits for W=2 acks -> success
Read(key):      client -> coordinator -> replicas {1,2,3}
                 waits for R=2 responses -> returns newest by version/timestamp
```

### Conflict Detection

Vector clocks attach a per-replica counter to each write, letting the
system detect concurrent writes rather than silently overwriting with
last-write-wins.

### Read Repair

When a quorum read finds replicas disagreeing, the coordinator writes
the newest version back to the stale replica(s) in the background.

## Step-by-Step Example

N=3, W=2, R=2. A write of `x=5` is acknowledged by replicas 1 and 2
(replica 3 is slow). A read queries replicas 2 and 3: replica 2 returns
`5`, replica 3 the older `x=3`. The coordinator picks the higher version
and issues a read-repair write of `5` to replica 3.

## Visualization

```text
Strong <----------------------------------------------> Eventual
linearizable   sequential   causal   read-your-writes   eventual
   |higher latency, lower availability     lower latency, higher availability|
```

## Real-World Usage

DynamoDB and Cassandra expose tunable consistency per request; Google
Spanner uses synchronized clocks (TrueTime) for external consistency
across regions; session systems implement read-your-writes by routing a
user's reads to the replica that served their last write.

## Advantages

Strong consistency simplifies application logic since a read always
reflects the latest write. Eventual consistency delivers lower latency
and higher availability, especially across regions.

## Disadvantages

Strong consistency costs latency and availability (must reach
quorum/leader). Eventual consistency pushes correctness work into the
application (conflict resolution, tolerating stale reads).

## Trade-offs

This is CAP/PACELC in practice: during a partition, choose consistency
or availability; the right choice is per-field, not per-system — a
single app can mix strong consistency for payments with eventual
consistency for feeds.

## Alternatives

Causal consistency is a practical middle ground: cheaper than
linearizability but preserves cause-and-effect ordering. CRDTs
(conflict-free replicated data types) avoid conflict resolution for
specific data shapes (counters, sets) via well-defined merges.

## Performance Analysis

Every replica required for a quorum adds a network round trip to the
critical path; cross-region quorum reads/writes can add tens to hundreds
of milliseconds versus a single-region eventual read.

## Complexity Analysis (if applicable)

Vector clocks require O(number of replicas) space per stored version.
Consensus protocols (Raft, Paxos) need O(N) messages per agreed value
among N nodes, which is why they suit control-plane decisions, not
high-throughput data paths.

## Implementation

```java
final class VectorClock {
    private final Map<String, Long> counters = new HashMap<>();

    void increment(String replicaId) {
        counters.merge(replicaId, 1L, Long::sum);
    }

    // true if `this` happened-before `other` (every counter <=, at least one <)
    boolean happensBefore(VectorClock other) {
        boolean strictlyLess = false;
        for (String id : union(this.counters.keySet(), other.counters.keySet())) {
            long a = counters.getOrDefault(id, 0L);
            long b = other.counters.getOrDefault(id, 0L);
            if (a > b) return false;
            if (a < b) strictlyLess = true;
        }
        return strictlyLess;
    }

    private Set<String> union(Set<String> a, Set<String> b) {
        Set<String> u = new HashSet<>(a);
        u.addAll(b);
        return u;
    }
}
```

If neither `happensBefore` holds in either direction, the writes are
concurrent — a real conflict that last-write-wins would silently hide.

## Common Mistakes

- Assuming a NoSQL label means no consistency is needed anywhere.

## Common Interview Questions

Q: What is the CAP theorem in one sentence?

Answer:
During a network partition, a system must choose between consistency
(every node sees the same, latest data) and availability (every
non-failing node still responds), since it cannot guarantee both while
nodes cannot communicate.

Q: What is the difference between strong and eventual consistency?

Answer:
Strong consistency guarantees a read always returns the most recent
write, typically via a quorum or leader; eventual consistency allows
temporarily stale reads, guaranteeing only that replicas converge once
writes stop.

## Follow-up Interview Questions

Q: How does Google Spanner achieve strong consistency across regions?

Answer:
It uses TrueTime, a globally synchronized clock with bounded
uncertainty, letting Spanner assign globally meaningful timestamps and
wait out the uncertainty window before committing.

## How the Interviewer Thinks

The interviewer wants to see whether a candidate maps a business
requirement to the correct consistency guarantee instead of reciting
"CAP theorem" as a slogan, and knows concrete mechanisms rather than
only the trade-off's name.

## Production Engineering Perspective

Production systems implement session consistency via sticky routing or
"read from primary for N seconds after a write," and monitor replication
lag continuously as the leading indicator of staleness. The evolution
path is single database -> primary-replica async replication ->
multi-region quorum or consensus-backed store.

## Common Misconceptions

- "Eventual consistency means eventually always correct" — it only
  guarantees convergence; without a sound conflict rule, replicas can
  converge to a wrong or lost-update value.

## Revision Notes

- Spectrum: linearizable > sequential > causal > read-your-writes >
  eventual.

## Summary

Consistency models give a precise vocabulary for what a distributed read
is allowed to return, ranging from linearizable to eventual; the skill
is matching each piece of data to the weakest model that is still safe
for its business meaning.

---

# Caching

## What is it?

Storing a copy of frequently accessed or expensive-to-compute data in a
faster-access layer (memory) so later requests skip the original,
slower work.

## Why do we need it?

Backing stores — disks, databases, external APIs — are orders of
magnitude slower than memory and often rate-limited, yet most workloads
repeat the same reads far more often than they change data.

## What problem does it solve?

Caching reduces read latency and offloads load from the source of
truth, letting a system serve far higher read throughput than the
backing store could sustain alone.

## History (only if useful)

Hardware caching (CPU L1/L2/L3) established the core idea: keep hot
data close to where it is used. Memcached (2003, for LiveJournal) and
Redis (2009) brought general-purpose distributed caching to backends.

## How does it work?

### Caching Patterns

Cache-aside: the app checks the cache first, and on a miss reads the
store and populates the cache itself. Read-through: the cache loads on
a miss, hiding the store from the app. Write-through: writes go to
cache and store synchronously. Write-behind: writes hit the cache
immediately and flush asynchronously, risking loss on a crash.

### Eviction Policies

LRU (evict least recently used) fits recency-biased access; LFU (least
frequently used) suits stable popularity distributions; TTL bounds
staleness regardless of access pattern.

### Capacity Estimation

Access patterns are usually skewed: if the top 20% of keys serve 80% of
reads, sizing the cache to that hot set — not the entire dataset —
captures most of the benefit at a fraction of the memory cost.

## Internal Working

### Cache-Aside Architecture

```text
Client -> App -> Cache (hit?) --yes--> return
                    |
                    no
                    v
                  Database -> populate cache -> return
```

### Distributed Cache Sharding

A cache cluster spreads keys across nodes with consistent hashing, so
one node's failure or addition only disturbs a small key fraction.

### Request Flow

1. The app computes the cache key.
2. The client checks the local/distributed cache.
3. On a hit, return immediately (sub-millisecond).
4. On a miss, query the database, store the result with a TTL, then
   return.

## Step-by-Step Example

A product page misses cache key `product:123`. The app queries the
database (12ms), stores the result with a 60-second TTL, and returns
it. The next 500 requests within that window hit the cache (under 1ms
each) until the TTL expires.

## Visualization

```text
LRU eviction (HashMap + doubly linked list):
MRU [C]<->[B]<->[A] LRU     get(B): move B to MRU end
     put(D) when full: evict LRU (A), insert D at MRU end
```

## Real-World Usage

Facebook's TAO caches the social graph in front of MySQL; CDNs (Akamai,
Cloudflare) cache content at the network edge; InnoDB's buffer pool
caches database pages in memory in front of disk.

## Advantages

Cuts read latency from milliseconds to sub-millisecond; removes read
load from the backing store; often the cheapest order-of-magnitude
throughput improvement available.

## Disadvantages

Introduces staleness risk; adds an operational component to monitor;
invalidation is famously hard, and a cold or failed cache can dump full
load back onto the database at once.

## Trade-offs

Longer TTLs improve hit rate but increase staleness; shorter TTLs
reduce staleness but raise backend load. Write-through keeps the cache
consistent but adds write latency; write-behind is faster but risks
data loss.

## Alternatives

Read replicas spread read load without a separate cache technology, at
the cost of full-row storage. Materialized views precompute expensive
query results inside the database. CDN edge caching suits public,
cacheable content better than an application-level cache.

## Performance Analysis

Effective latency = hitRate x cacheLatency + missRate x (cacheLatency +
backendLatency). At a 95% hit rate with a 1ms cache and 50ms backend,
effective latency is about 3.5ms — a 14x improvement over always
hitting the backend.

## Complexity Analysis (if applicable)

A hash map plus a doubly linked list gives O(1) get and put for an LRU
cache, since both structures support constant-time lookup, removal, and
re-insertion at the head/tail.

## Implementation

```java
final class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LruCache(int capacity) {
        super(capacity, 0.75f, true); // accessOrder = true
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

`LinkedHashMap` with `accessOrder = true` already maintains recency
order; overriding `removeEldestEntry` turns it into a bounded LRU cache
in a few lines.

## Common Mistakes

- Caching everything with no TTL, so stale data lingers indefinitely.

## Common Interview Questions

Q: What is cache invalidation and why is it considered hard?

Answer:
Removing or updating cached data when the source changes; it is hard
because the cache and source of truth are separate systems updated
separately, so staying in sync under concurrent writes needs careful
design.

Q: What is the difference between write-through and write-back caching?

Answer:
Write-through writes to cache and store synchronously, keeping the
store current at the cost of write latency; write-back writes to the
cache immediately and flushes asynchronously, faster but risks losing
the update before the flush.

## Follow-up Interview Questions

Q: How do you keep a cache consistent across multiple regions?

Answer:
Use a short TTL as a safety net, publish invalidation events to all
regions via a message bus on change, or accept bounded regional
staleness where a few seconds of lag is acceptable.

## How the Interviewer Thinks

The interviewer is testing judgment about latency/cost trade-offs and
whether the candidate anticipates subtle failure modes — stampedes,
staleness, cold starts — rather than treating "add a cache" as a
universal, risk-free fix.

## Production Engineering Perspective

Caches are warmed proactively after deploys to avoid a cold-cache
spike; hit ratio and eviction rate are the key undersizing indicators.
An outage can stampede traffic onto the database, so serving stale data
and rate-limiting backend calls matters. Evolution: no cache -> single
node -> sharded cluster -> multi-tier (local, distributed, CDN).

## Common Misconceptions

- "Adding a cache is always safe" — it adds new failure modes (stampede,
  staleness) that must be designed for.

## Revision Notes

- Patterns: cache-aside, read-through, write-through, write-behind.

## Summary

Caching trades a small amount of staleness and operational complexity
for a large reduction in latency and backend load; the craft is
choosing the right pattern, eviction policy, and TTL for each data
shape, and defending against the stampede and staleness failure modes
caching itself introduces.

---

# Rate Limiter

## What is it?

A component that caps how many requests a client, user, or service can
make within a time window, rejecting or delaying requests beyond that
cap.

## Why do we need it?

Backends have finite capacity; a single misbehaving client, a retry
storm, or an attacker can otherwise consume all of it.

## What problem does it solve?

It solves fair resource allocation and overload protection: no single
caller monopolizes shared capacity, and backends are shielded from
abusive or accidental traffic spikes.

## History (only if useful)

Token bucket and leaky bucket algorithms originate in 1980s telecom
traffic shaping, designed to smooth bursty data onto fixed-capacity
links. Public web APIs adopted the same ideas once third-party
ecosystems grew large enough to overwhelm shared infrastructure.

## How does it work?

### Requirements

Functional: limit per user/API key/IP/route, return a clear rejection
(`HTTP 429`) with retry guidance, support different limits per tier.
Non-functional: sub-millisecond added latency, correctness under
concurrency across many servers.

### Capacity Estimation

100M users averaging 10 requests/day is ~12K requests/sec average; a 5x
peak factor means bursts near 60K checks/sec. Counters for 100M keys at
~100 bytes each need ~10GB, sharded across cache nodes.

### API Design and Data Model

No public API beyond the protected endpoints; responses include
`X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
headers. Each key maps to counter state — remaining tokens and last
refill timestamp — stored with a TTL in a fast key-value store.

### Algorithms

Fixed window: simplest, allows up to 2x the limit at boundaries. Sliding
window log: exact, memory-heavy. Sliding window counter: approximates
the log cheaply. Token bucket: allows bursts up to bucket size while
enforcing an average rate. Leaky bucket: smooths bursts into a strictly
constant output rate.

## Internal Working

### Architecture

```text
Client -> Edge/Gateway (local limiter) -> Distributed counter (Redis)
                                                |
                                                v
                                          Backend service
```

### Token Bucket Mechanics

A bucket holds up to `capacity` tokens and refills at `rate`
tokens/second. Each request consumes one token; an empty bucket rejects
or delays the request.

### Request Flow

1. A request arrives with an identifying key.
2. The limiter atomically fetches/updates the key's counter (e.g. a
   Redis Lua script).
3. If under the limit, forward the request and decrement tokens.
4. If over, return `429` with `Retry-After`.

## Step-by-Step Example

A bucket has capacity 10, refilling 1 token/second. A burst of 10
requests succeeds instantly, draining it. An 11th request one second
later finds 1 refilled token and succeeds; a 12th in the same second is
rejected until refill, smoothing the burst into a sustainable rate.

## Visualization

```text
Token bucket:  [*][*][*][*][ ][ ]  <- capacity 6, 2 tokens used
                refill: +1/sec, request: -1 token, empty => reject

Leaky bucket:  in -> [ bucket ] -> out at fixed rate
               (excess above capacity overflows / is dropped)
```

## Real-World Usage

API gateways (Envoy, Kong) implement rate limiting as a standard filter;
Stripe and GitHub publish per-endpoint limits via response headers; CDNs
rate-limit at the edge to absorb volumetric attacks before origin.

## Advantages

Protects backend capacity from single-client or attack traffic; enables
predictable, fair multi-tenant usage and monetized tiers.

## Disadvantages

Adds latency and an operational component; distributed correctness is
genuinely hard; can legitimately reject valid bursty traffic from a
well-behaved client.

## Trade-offs

Exact algorithms (sliding window log) cost more memory for precision;
approximate ones (token bucket) cost less at the price of small
inaccuracy. Local per-node counting is fast but allows N times
over-admission across N nodes; centralized counting is accurate but adds
a round trip.

## Alternatives

Client-side throttling shifts responsibility to well-behaved clients but
cannot stop malicious ones. Queue-based admission control processes
requests at a sustainable rate instead of rejecting outright. Adaptive
limits (AIMD, like TCP congestion control) adjust the cap to observed
backend health.

## Performance Analysis

An atomic Redis `INCR` plus `EXPIRE`, or a single Lua script, typically
completes in well under a millisecond, making centralized rate limiting
practical at tens of thousands of checks/sec per node.

## Complexity Analysis (if applicable)

Token bucket and window counters are O(1) time and space per key.
Sliding window log is O(n) space per key (n = requests in window) and
requires pruning old entries on every check.

## Implementation

```java
final class TokenBucket {
    private final long capacity;
    private final double refillPerNano;
    private double tokens;
    private long lastRefillNanos;

    TokenBucket(long capacity, double refillPerSecond) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillPerNano = refillPerSecond / 1_000_000_000.0;
        this.lastRefillNanos = System.nanoTime();
    }

    synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) { tokens -= 1; return true; }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        double added = (now - lastRefillNanos) * refillPerNano;
        tokens = Math.min(capacity, tokens + added);
        lastRefillNanos = now;
    }
}
```

`synchronized` keeps this correct for one JVM; a distributed limiter
replaces the in-memory fields with an atomic Redis script executing the
same refill-then-consume logic.

## Common Mistakes

- Using a naive fixed window, allowing up to double the intended limit
  at boundaries.

## Common Interview Questions

Q: Design a rate limiter for an API. What are the key requirements?

Answer:
Clarify scope (per user, IP, or key; global or per-endpoint) and whether
bursts are allowed; core requirements are low added latency, correctness
under concurrency, and a clear rejection with retry guidance.

Q: What is the difference between token bucket and leaky bucket?

Answer:
Token bucket allows bursts up to its capacity as long as the average
rate is respected; leaky bucket enforces a strictly constant output
rate regardless of how bursty the input is.

## Follow-up Interview Questions

Q: How do you rate limit per user and per IP simultaneously?

Answer:
Check both limiters independently and reject if either is exceeded,
catching a single abusive account and many accounts sharing one
abusive source at the cost of two checks per request.

## How the Interviewer Thinks

The interviewer checks algorithmic knowledge (token bucket versus fixed
window), distributed-systems judgment about shared-counter races, and
practical API sense (status codes, headers, tiering).

## Production Engineering Perspective

Rate limiting is enforced as early as possible (edge or gateway) so
rejected traffic never reaches backend logic. A hot key can bottleneck
one storage shard; hierarchical limiting (local check backed by a
periodic global sync) mitigates this. Fail-open versus fail-closed on
limiter-storage outage is a deliberate, endpoint-specific choice.
Evolution: no limiting -> in-memory counters -> centralized counters ->
hierarchical local-plus-global limiting.

## Common Misconceptions

- "A rate limiter is just a counter" — correctness under concurrency and
  distribution is the actual hard part.

## Revision Notes

- Algorithms: fixed window (boundary bug), sliding log (exact, costly),
  sliding counter (cheap), token bucket (bursts), leaky bucket (constant
  rate).

## Summary

A rate limiter bounds how much load any single caller can place on a
system, using an algorithm chosen to balance burst tolerance, memory
cost, and precision; the hardest part in production is making the
counter correct and fast under concurrent, distributed access.

---

# URL Shortener

## What is it?

A service that maps a long URL to a short, unique alias and redirects
visitors from the alias back to the original URL.

## Why do we need it?

Long URLs are hard to share, especially in space-constrained contexts
(SMS, printed material, character-limited platforms), and a short alias
is easier to remember, track, and brand.

## What problem does it solve?

It solves compact, trackable sharing: compressing an arbitrarily long
URL into a short, stable identifier while adding click analytics,
expiry, and abuse control that raw links do not have.

## History (only if useful)

TinyURL (2002) introduced the concept broadly; bit.ly (2008) grew
rapidly alongside Twitter's original 140-character limit, later adding
analytics as a core product feature.

## How does it work?

### Requirements

Functional: shorten a URL, redirect a code to its target, support custom
aliases and expiry. Non-functional: very low redirect latency, high read
availability, unique codes, and a read:write ratio often 100:1+.

### Capacity Estimation

500M new URLs/month is ~200 writes/sec average; at 100:1 read:write that
is ~20,000 reads/sec. 500M records/year at ~500 bytes each is ~250GB/
year, small enough to keep entirely cacheable.

### API Design

`POST /shorten {longUrl, customAlias?, ttl?}` returns `{shortUrl}`. `GET
/{code}` returns an HTTP 301/302 redirect to the stored long URL.

### Data Model

A single table: `code` (primary key), `long_url`, `created_at`,
`expires_at`, `owner_id`, `click_count` — no relationships to other
entities are required.

### Code Generation

Base62 encoding of an auto-incrementing ID is collision-free but reveals
creation order; hashing and truncating the URL needs collision handling
but hides sequence; a pre-generated key service allocates code ranges
ahead of time, decoupling generation from the write path.

## Internal Working

### Architecture

```text
Client -> LB -> App Servers -> Cache (code -> longUrl)
                    |               |
                    v               v
              Key Generation    Database
              Service (KGS)     (source of truth)
```

### Request Flow: Shorten

1. The app requests a pre-allocated code (or computes one).
2. It writes `{code, longUrl}` to the database.
3. It returns the short URL to the client.

### Request Flow: Redirect

1. A request for `/{code}` arrives.
2. The app checks the cache; on a hit, returns the redirect immediately.
3. On a miss, reads the database, populates the cache, returns the
   redirect.
4. Click metadata is published asynchronously to an analytics queue.

## Step-by-Step Example

Auto-increment ID `125` is encoded in base62 (62-symbol alphabet):
`125 = 2x62 + 1`, giving digits `[2, 1]`, mapping to code `"21"`.
Decoding reverses it: `"21"` -> `2x62 + 1 = 125` -> the original ID.

## Visualization

```text
Base62 alphabet: 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz
ID 125  -> base62 "21"  -> shorturl.example/21
"21" -> base62 decode -> 125 -> DB lookup -> long URL
```

## Real-World Usage

bit.ly and TinyURL are the canonical examples; Twitter's t.co wraps every
link automatically, doubling as a shortener and security scanning layer;
Firebase Dynamic Links adds deferred deep linking into mobile apps.

## Advantages

Conceptually simple core feature; extremely cacheable since mappings are
immutable once created; horizontally scalable on reads and writes
independently.

## Disadvantages

Sequential IDs can leak creation order and volume; hash-based codes need
collision handling; custom aliases contend for a shared namespace.

## Trade-offs

Counter-based generation is collision-free but needs a centrally
coordinated counter (or pre-allocated ranges); hash-based generation
parallelizes trivially but must handle the rare collision explicitly.

## Alternatives

A raw auto-increment column works at small scale but becomes a write
bottleneck; a distributed ID generator (Snowflake-style) avoids that; a
plain UUID is simple but far too long to be "short."

## Performance Analysis

The redirect path should be cache-dominant, targeting well above 99%
hit rate, since reads vastly outnumber writes and sit on the user's
critical path; the write path has much looser latency requirements.

## Complexity Analysis (if applicable)

Base62 encode/decode is O(log N) in digits relative to the ID.
Collision probability follows the birthday bound: a 6-character code
(62^6 ~= 56 billion possibilities) stays rare past tens of millions of
entries but must still be checked deterministically.

## Implementation

```java
final class Base62 {
    private static final String ALPHABET =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    static String encode(long id) {
        if (id == 0) return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }

    static long decode(String code) {
        long id = 0;
        for (char c : code.toCharArray()) {
            id = id * 62 + ALPHABET.indexOf(c);
        }
        return id;
    }
}
```

Encoding and decoding are pure functions with no shared state, so any
app server can perform them independently once it owns a unique ID.

## Common Mistakes

- Exposing raw sequential IDs, enabling enumeration of every link.

## Common Interview Questions

Q: How would you design a URL shortener?

Answer:
Clarify scale and features, estimate the read:write ratio (usually
read-heavy), define a simple API and single-table model, choose a
code-generation strategy, and cache the redirect path aggressively.

Q: How do you generate short, unique codes at scale?

Answer:
Base62-encode a uniquely assigned ID from a coordinated counter or
pre-allocated ranges, or hash the URL and truncate it while explicitly
handling the rare collision by retrying with a salt.

## Follow-up Interview Questions

Q: How do you prevent the service from being used for phishing or malware distribution?

Answer:
Check destination URLs against malicious-URL blocklists at creation and
periodically afterward, and show an interstitial warning for links
flagged suspicious rather than an instant redirect.

## How the Interviewer Thinks

This is typically a warm-up design testing whether a candidate reasons
about read/write asymmetry, justifies an ID-generation strategy, and
reaches for caching before anything more elaborate.

## Production Engineering Perspective

Analytics are handled asynchronously via a queue so redirect latency
never depends on the analytics pipeline. Bottlenecks appear at
ID-generation coordination if not pre-allocated, and at the database for
popular codes unless caching absorbs traffic. Evolution: a single
auto-increment database -> a key generation service -> a fully cached,
CDN-fronted redirect path.

## Common Misconceptions

- "A URL shortener is a trivial CRUD app" — read-scaling, ID-generation
  coordination, and abuse prevention make it a genuine distributed
  design problem at scale.

## Revision Notes

- Code generation options: counter+base62, hash+collision handling,
  pre-allocated key service.

## Summary

A URL shortener's simplicity is deceptive: the real design problems are
generating unique codes without a write bottleneck, caching an
overwhelmingly read-heavy path, and preventing abuse — not the redirect
mechanism itself, which is a single database lookup.

---

# Chat System

## What is it?

A system that delivers real-time text (and often media) messages
between two users (1:1) or a group over a persistent connection, with
delivery guarantees, ordering, and offline support.

## Why do we need it?

Users expect near-instant delivery of messages and status updates;
request-response HTTP alone cannot push updates without the client
repeatedly polling, which wastes resources and adds latency.

## What problem does it solve?

It solves reliable, low-latency, bidirectional communication at scale
across devices that connect and disconnect constantly, while preserving
ordering and eventually delivering messages sent while offline.

## History (only if useful)

IRC (1988) established many-to-many real-time text chat; XMPP/Jabber
generalized federated instant messaging in the 2000s; the WebSocket
standard (2011) gave browsers a persistent, full-duplex connection,
replacing long-polling hacks.

## How does it work?

### Requirements

Functional: 1:1 and group messaging, delivery/read receipts, presence,
offline delivery, media attachments. Non-functional: sub-second latency,
per-conversation ordering, at-least-once delivery, durable history.

### Capacity Estimation

50M daily active users sending 40 messages/day is 2B messages/day, ~23,000
messages/sec average and ~115,000/sec at 5x peak. At ~200 bytes/message
that is ~400GB of new message data per day before replication.

### Protocol and Data Model

A persistent WebSocket carries real-time push; REST/gRPC handles history
fetch and auth. A message envelope holds `messageId`, `conversationId`,
`senderId`, `timestamp`, `type`, `content`. Storage is a `conversations`
table plus a `messages` table partitioned by conversation and time, plus
a connection registry mapping each online user to their gateway server.

## Internal Working

### Architecture

```text
Clients <==WebSocket==> Gateway Servers <-> Message Service <-> Queue (Kafka)
                                                  |                  |
                                                  v                  v
                                             Storage (Cassandra)  Push Notification
                                                                  Service (offline)
                              Presence Service (Redis) <---------------+
```

### Connection Routing

Because a user's socket lives on exactly one gateway server at a time,
the message service looks up which server holds the recipient's
connection (via a registry, often Redis) before pushing directly.

### Request Flow

1. The sender's client sends a message over its WebSocket.
2. The gateway forwards it to the message service, which persists it and
   assigns an ordered ID.
3. The service looks up the recipient's connection; if online, it pushes
   directly through their gateway.
4. If offline, the message waits in storage and triggers a push
   notification; on reconnect, the client fetches missed messages.

## Step-by-Step Example

Alice sends "hello" to Bob, who is offline. The gateway persists the
message and finds no registry entry for Bob, so it enqueues a push
notification and marks the message undelivered. Later, Bob opens the
app, opens a new WebSocket, and fetches all messages since his last
known ID, receiving and then reading Alice's message.

## Visualization

```text
Alice --send--> Gateway --persist+route--> Bob online?
                                  |--yes--> push over Bob's socket --ack--> read receipt
                                  |--no ---> queue + push notification --> Bob reconnects --> fetch missed
```

## Real-World Usage

WhatsApp historically ran on Erlang for massive concurrent-connection
handling; Slack backs channels with sharded relational storage and a
message bus for fan-out; Discord stores messages in a wide-column store
chosen for high per-channel write throughput.

## Advantages

Decoupling send from deliver via a queue keeps the system responsive
even when a recipient is offline or a downstream store is slow;
persistent connections give genuinely low-latency delivery versus
polling.

## Disadvantages

Millions of long-lived connections are operationally heavy (memory,
careful load balancing); multi-device sync, ordering, and deduplication
add real complexity beyond simple send/receive.

## Trade-offs

At-least-once delivery (safe against loss) requires client-side
deduplication by message ID; at-most-once risks silent loss; true
exactly-once is costly enough that most systems choose at-least-once
plus dedup. Strict global ordering is unnecessary; per-conversation
ordering is the practical target.

## Alternatives

HTTP long-polling is simpler but adds latency and wastes connections
versus WebSockets. MQTT is lighter weight and suited to constrained
mobile/IoT clients. Server-sent events give one-way server-to-client
push where the client never needs to send back over the same channel.

## Performance Analysis

A single gateway node typically sustains tens of thousands to ~100,000
concurrent idle WebSocket connections depending on memory per
connection; fan-out cost for a message to a group of size G is O(G), so
very large groups need a batched fan-out path.

## Complexity Analysis (if applicable)

Message ID generation (Snowflake-style, embedding a timestamp) is O(1)
and gives roughly time-ordered IDs useful for pagination; delivering to
a group is O(G) unless fan-out is batched or deferred for large groups.

## Implementation

```java
final class MessageDeduplicator {
    private final int capacity;
    private final LinkedHashSet<String> seen = new LinkedHashSet<>();

    MessageDeduplicator(int capacity) { this.capacity = capacity; }

    synchronized boolean isDuplicate(String messageId) {
        if (seen.contains(messageId)) return true;
        seen.add(messageId);
        if (seen.size() > capacity) {
            seen.remove(seen.iterator().next()); // evict oldest
        }
        return false;
    }
}
```

A bounded, recency-ordered set of recently seen message IDs lets a
client or gateway safely ignore redelivered copies of an at-least-once
message without unbounded memory growth.

## Common Mistakes

- Storing all of a user's state in one hot database row, creating a hot
  partition for very active accounts.

## Common Interview Questions

Q: How would you design a chat system like WhatsApp?

Answer:
Clarify scale and features (1:1 vs group, media, offline delivery),
estimate throughput, choose WebSockets backed by a connection registry,
persist messages asynchronously through a queue, and add a push
fallback for offline recipients.

Q: How do you guarantee message ordering in a conversation?

Answer:
Assign each message a monotonically increasing, per-conversation
sequence number (or time-ordered ID) at persistence time, and render in
that order rather than relying on network arrival order.

## Follow-up Interview Questions

Q: How do you scale to millions of concurrent connections?

Answer:
Horizontally scale gateway servers behind a connection-aware load
balancer, keep per-connection state minimal, and maintain a shared
connection registry so any part of the system can find a user's socket.

## How the Interviewer Thinks

The interviewer is testing real-time systems knowledge (connection
state, fan-out cost), delivery-guarantee reasoning, and whether the
candidate separates the send path from the deliver path.

## Production Engineering Perspective

Connection draining during deploys avoids a reconnect storm; sticky
routing helps clients land on a warm session. A gateway crash drops
every socket it held, a storm absorbed with jittered client backoff.
Evolution: a single chat server -> a WebSocket gateway farm with a
shared registry and message bus -> geo-distributed edge gateways.

## Common Misconceptions

- "WebSockets alone solve the scaling problem" — the protocol is only
  the transport; fan-out, persistence, and the registry still need
  design.

## Revision Notes

- Persistent WebSockets plus a connection registry are the core
  real-time primitive.

## Summary

A chat system's core challenge is not sending one message but managing
millions of stateful, intermittent connections while guaranteeing
ordered, at-least-once delivery — solved by separating the real-time
transport, the durable message store, and the fan-out/notification
logic into independently scalable components.

---

# Notification System

## What is it?

A system that delivers messages (push, SMS, email, in-app) to users
through the appropriate channel, decoupled from the application logic
that decides one is needed.

## Why do we need it?

Every product needs to reach users outside the app across channels and
providers with different APIs, rate limits, and failure modes;
centralizing this avoids duplicating fragile integration logic.

## What problem does it solve?

It solves multi-channel, reliable fan-out at scale: accepting a
notification request once and handling provider routing, retries,
preferences, and rate limits so calling services do not each
reimplement delivery reliability.

## History (only if useful)

Apple Push Notification service (2009) and Google's GCM (2010, later
FCM) established the mobile push model of a persistent OS-level channel
apps publish to, rather than holding their own long-lived connections.

## How does it work?

### Requirements

Functional: send via push/SMS/email, respect preferences and quiet
hours, template rendering, delivery tracking. Non-functional: high
throughput, retries with backoff, idempotency, provider fan-out.

### Capacity Estimation

10M daily notifications at 3x peak is ~350/sec average and ~1,000/sec
peak — modest for a queue-backed system, though marketing campaigns can
spike far higher and should be rate-limited against provider quotas.

### API and Data Model

`POST /notify {userId, type, templateId, data}` enqueues a request.
Internally: a `notifications` table (`id`, `userId`, `channel`, `status`,
`attempts`), a `user_preferences` table (opt-in/out per channel/type),
and per-provider delivery logs.

## Internal Working

### Architecture

```text
Service -> Notification API -> Queue -> Workers -> Provider (APNs/FCM/SMS/Email)
                                            |
                                    Preference/Template Service
```

### Request Flow

1. A service calls the notification API with a user, type, and data.
2. The API checks preferences; if opted out, it drops the request.
3. It renders the template and enqueues one job per target channel.
4. A worker calls the provider, recording success/failure and retrying
   transient failures with backoff.

## Step-by-Step Example

An order-ships event triggers a notification call. The API finds the
user opted into push but not email, renders "Your order has shipped,"
and enqueues one job. A worker calls APNs; on a transient 5xx it retries
twice with backoff before marking delivered.

## Visualization

```text
Request -> [Preference filter] -> [Template render] -> Queue
              |no                                        |
              drop                                  Worker pool -> Provider
```

## Real-World Usage

Uber and Amazon send delivery/order updates across push, SMS, and email
from one internal service; providers like SendGrid and push aggregators
like OneSignal offer this as a managed product.

## Advantages

Centralizes retry/rate-limit/preference logic once instead of per
service; decouples business logic from flaky provider behavior; enables
consistent delivery tracking.

## Disadvantages

Adds a hop and potential delay between an event and delivery; becomes a
shared dependency whose outage affects every product surface.

## Trade-offs

Synchronous delivery gives immediate feedback but ties the caller's
latency to a provider's; asynchronous, queue-based delivery decouples
latency at the cost of not knowing delivery status immediately.

## Alternatives

Client-side polling avoids push infrastructure but adds latency and
battery cost. Each service integrating directly with providers avoids a
shared dependency but duplicates retry and preference logic everywhere.

## Performance Analysis

Because provider calls are the slow, unreliable step, throughput is
governed by worker pool size and per-provider rate limits, not the
queue; scaling workers (bounded by provider quota) is the primary lever.

## Complexity Analysis (if applicable)

Preference lookup and template rendering are O(1) per notification;
overall throughput is bounded by min(worker capacity, provider rate
limit) rather than any single algorithm's complexity.

## Implementation

```java
final class RetryWithBackoff {
    static <T> T call(Callable<T> action, int maxAttempts) throws Exception {
        long delayMs = 200;
        for (int attempt = 1; ; attempt++) {
            try {
                return action.call();
            } catch (Exception e) {
                if (attempt >= maxAttempts) throw e;
                Thread.sleep(delayMs);
                delayMs *= 2; // exponential backoff
            }
        }
    }
}
```

Exponential backoff spaces out retries so a struggling provider is not
hammered immediately after each failure.

## Common Mistakes

- No idempotency key, causing duplicate notifications on retry.

## Common Interview Questions

Q: How would you design a notification system supporting push, SMS, and email?

Answer:
Build one API that checks user preferences, renders a template, and
enqueues one job per channel to a worker pool that calls the relevant
provider with retries, keeping channel-specific logic behind a common
interface.

Q: How do you avoid sending duplicate notifications?

Answer:
Assign an idempotency key to each request and have workers check it
before sending, so a retried job from an at-least-once queue does not
result in a duplicate.

## Follow-up Interview Questions

Q: How would you support user notification preferences across many notification types?

Answer:
Model preferences as a matrix of channel x notification-type, checked
before enqueueing, and default new types conservatively (opt-out for
marketing, opt-in for critical security notices).

## How the Interviewer Thinks

The interviewer checks whether a candidate treats providers as
unreliable dependencies requiring retries and isolation, and whether
preferences and idempotency are designed in from the start.

## Production Engineering Perspective

Per-provider circuit breakers and rate limiting protect the worker
pool from one bad provider; dead-letter queues capture notifications
that exhaust retries. Provider rate limits during bursts are the main
bottleneck; priority queues protect critical notifications. Evolution:
direct in-service provider calls -> a shared queue-backed notification
service -> multi-provider fan-out with priority routing.

## Common Misconceptions

- "Push notifications are guaranteed delivery" — OS-level push is
  best-effort, not guaranteed.

## Revision Notes

- Core flow: preference check -> template render -> per-channel enqueue
  -> worker delivers with retry/backoff.

## Summary

A notification system's value is centralizing unreliable, multi-provider
delivery behind one API with consistent retry, preference, and
prioritization logic, so every other service can "fire and forget" a
notification.

---

# File Storage

## What is it?

A system that stores, replicates, and serves binary objects (images,
videos, documents) at scale, separate from structured database data.

## Why do we need it?

Large binary blobs bloat a database and hurt its performance; a
dedicated object store scales storage and bandwidth independently.

## What problem does it solve?

Durable, scalable storage and delivery of unstructured data, with
redundancy, deduplication, and CDN-friendly delivery a relational
database is a poor fit for.

## History (only if useful)

Google's GFS paper (2003) established distributed file storage design;
Amazon S3 (2006) popularized object storage's bucket/key/object API
shape, which most systems since have converged on.

## How does it work?

### Requirements

Functional: upload, download, delete, and list objects; generate
shareable links. Non-functional: durability across disk/node failure,
high availability, and efficient handling of small and very large
files.

### Capacity Estimation

100M photos/day averaging 2MB is ~200TB/day of new data; at 3x
replication that is 600TB/day, making deduplication and lifecycle
policies (moving old data to cold storage) essential for cost.

### API and Data Model

`PUT /objects/{key}` uploads, `GET /objects/{key}` downloads, `DELETE
/objects/{key}` removes. Metadata (`key`, `size`, `contentHash`,
`storageLocation`, `createdAt`) is stored separately in a fast metadata
database, while object bytes live in the storage tier.

## Internal Working

### Architecture

```text
Client -> API/Gateway -> Metadata DB (key -> location)
              |
              v
        Storage Nodes (chunked, replicated) <-> CDN (for reads)
```

### Chunking and Replication

Large files are split into fixed-size chunks, each replicated across
multiple storage nodes (often 3x) or erasure-coded for lower storage
overhead at similar durability; each chunk's hash enables deduplication
across uploads.

### Request Flow

1. A client uploads a file; the gateway splits it into chunks and
   computes each chunk's hash.
2. Chunks are written to multiple storage nodes for redundancy.
3. Metadata (key, chunk locations, hash) is written to the metadata
   store.
4. On download, the gateway looks up chunk locations and streams them
   back, ideally from a CDN edge cache rather than origin storage.

## Step-by-Step Example

A 6MB video is split into 2MB chunks. Each is hashed with SHA-256; one
chunk matches a hash already in storage from another upload, so only
its reference count is incremented instead of storing duplicate bytes.
The other two chunks are written to three storage nodes each, and
metadata records `{key, chunks: [h1, h2, h3], size: 6MB}`.

## Visualization

```text
File -> [chunk1][chunk2][chunk3] -> hash each
  chunk1 hash matches existing -> reuse (dedup)
  chunk2, chunk3 -> write to 3 nodes each (replication)
```

## Real-World Usage

S3 and Google Cloud Storage are the canonical managed object stores;
Dropbox built its own chunked, deduplicated block-storage backend for
efficient sync; Instagram stores images in a blob store with metadata
in a separate index for fast lookup.

## Advantages

Scales storage and bandwidth independently of any database;
deduplication and tiered storage (hot/cold) reduce cost significantly;
CDN integration makes reads fast globally.

## Disadvantages

Consistency across chunk replicas requires careful coordination; small
files incur relatively higher metadata overhead per byte; deletion must
reconcile reference counts before reclaiming space.

## Trade-offs

Replication is simple, fast to recover, but costs 3x storage; erasure
coding cuts overhead to ~1.5x at similar durability but costs more CPU
to encode/decode and is slower to reconstruct.

## Alternatives

NFS offers POSIX semantics but scales far worse than an object store.
Storing blobs directly in a database (BLOB columns) is simple at small
scale but bloats backups and does not scale independently.

## Performance Analysis

Reads should be dominated by CDN cache hits, pushing effective latency
to tens of milliseconds at the edge; origin latency (tens to hundreds
of ms) only matters on cache misses.

## Complexity Analysis (if applicable)

Chunk hash lookup for deduplication and metadata lookup for a key's
chunk locations are both O(1) via hash/key-value indexes, independent
of file size.

## Implementation

```java
final class ContentChunker {
    private static final int CHUNK_SIZE = 4 * 1024 * 1024; // 4MB

    static List<String> chunkAndHash(byte[] data) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        List<String> hashes = new ArrayList<>();
        for (int offset = 0; offset < data.length; offset += CHUNK_SIZE) {
            int end = Math.min(offset + CHUNK_SIZE, data.length);
            byte[] hash = sha256.digest(Arrays.copyOfRange(data, offset, end));
            hashes.add(Base64.getEncoder().encodeToString(hash));
        }
        return hashes;
    }
}
```

Hashing each chunk independently lets identical chunks across different
files be detected and stored only once.

## Common Mistakes

- Storing large blobs directly in a relational database.

## Common Interview Questions

Q: How would you design a scalable file storage system like S3?

Answer:
Separate metadata (key, location, size) from the bytes; chunk large
files, replicate or erasure-code each chunk across storage nodes for
durability, hash chunks for deduplication, and put a CDN in front of
reads since files are read far more often than written.

Q: How do you ensure durability of stored files?

Answer:
Replicate each chunk across multiple nodes (ideally multiple
availability zones), verify integrity with checksums on read, and run
background repair processes that detect and re-replicate lost chunks.

## Follow-up Interview Questions

Q: How do you handle very large file uploads reliably over unreliable networks?

Answer:
Use multipart/resumable upload: split the file into chunks client-side,
upload each independently with its own retry, and let the client resume
from the last acknowledged chunk instead of restarting the whole upload.

## How the Interviewer Thinks

The interviewer wants chunking, replication/erasure-coding trade-offs,
and the metadata/data separation pattern, plus awareness that reads
should be CDN-dominant rather than always hitting origin.

## Production Engineering Perspective

Background integrity scanning and automatic re-replication of lost
chunks are essential since disks fail continuously at scale. The
metadata store is the main bottleneck at high object counts, mitigated
by sharding by key hash. Evolution: local disk -> single object store
-> chunked, tiered storage with CDN delivery. Security requires
per-object access control and signed, time-limited URLs.

## Common Misconceptions

- "Object storage is just a filesystem in the cloud" — it trades POSIX
  semantics for massive horizontal scalability and different
  consistency guarantees.

## Revision Notes

- Separate metadata store (key -> location) from chunked, replicated
  object storage.

## Summary

File storage systems earn their scale by separating small, fast metadata
lookups from large, chunked, replicated (or erasure-coded) object data,
deduplicating at the chunk level, and letting a CDN absorb the
read-heavy traffic that dominates real usage.

---

# Logging

## What is it?

The practice of recording discrete, timestamped events — errors,
requests, state changes — from a running system to durable storage for
later inspection.

## Why do we need it?

Distributed systems fail in ways that are hard to reproduce; logs are
the primary forensic record of what happened, when, and in what order,
across every service a request traversed.

## What problem does it solve?

After-the-fact observability: reconstructing a request's path and a
system's behavior when it cannot be observed directly at the moment of
failure.

## History (only if useful)

Unix syslog (1980s) established centralized system logging; distributed
systems made per-server log files insufficient, driving centralized
aggregation (Splunk, then the open-source ELK stack) in the 2000s-2010s.

## How does it work?

### Requirements

Functional: structured log emission, centralized aggregation, search,
retention policy. Non-functional: must not slow the request path, and
the pipeline must handle high volume without losing critical entries.

### Capacity Estimation

10,000 requests/sec each emitting 5 log lines at 200 bytes is ~10MB/sec,
or ~850GB/day, making tiered retention (hot for days, cold for months)
necessary for cost.

### Log Levels and Structure

Levels (`DEBUG`, `INFO`, `WARN`, `ERROR`) tune verbosity per
environment. Structured (JSON) logs with consistent fields
(`timestamp`, `level`, `service`, `traceId`, `message`) are
machine-parseable, and a `traceId` propagated across services makes a
distributed request reconstructable.

## Internal Working

### Architecture

```text
App (structured logs) -> Local agent (Fluentd/Filebeat) -> Queue (Kafka)
                                                                 |
                                                                 v
                                                    Indexer (Logstash) -> Store (Elasticsearch)
                                                                              |
                                                                              v
                                                                        Dashboard (Kibana)
```

### Request Flow

1. The app writes a structured log line to local disk or stdout.
2. A local agent tails and forwards it to a durable queue.
3. An indexer consumes from the queue, parses and enriches it, and
   writes to a searchable store.
4. Engineers query the store through a dashboard, filtered by service,
   time range, or `traceId`.

## Step-by-Step Example

A request fails with a 500. The engineer searches for its `traceId`,
finding one `INFO` entry per service touched, plus a single `ERROR`
entry from the payment service showing a downstream timeout —
reconstructing the fault without needing to reproduce it.

## Visualization

```text
App writes -> local agent buffers -> Kafka -> indexer -> Elasticsearch
                                                              |
                                             query by traceId, time, service
```

## Real-World Usage

The ELK/Elastic stack is a common self-hosted choice; Splunk is common
in enterprises; cloud-native equivalents (CloudWatch Logs, Datadog Logs)
provide the same pipeline as a managed service.

## Advantages

Centralized logs make cross-service debugging tractable; structured
fields enable filtering and aggregation free-text logs cannot support.

## Disadvantages

High-volume logging adds storage cost and, if synchronous, request
latency; excessive or poorly structured logging buries the signal
engineers need.

## Trade-offs

Verbose logging aids debugging but costs storage and can leak sensitive
data; sparse logging is cheap but may omit needed detail. Synchronous
writes guarantee capture before continuing but add latency;
asynchronous writes are faster but risk losing the last entries on a
crash.

## Alternatives

Metrics answer "how much/how often" cheaply; traces answer "where did
time go" across services for one request; logs answer "what exactly
happened" with full detail — most systems need all three.

## Performance Analysis

Asynchronous, buffered log writes add microseconds to the request path
versus milliseconds for synchronous writes to a remote store; batching
before shipping further reduces per-line overhead.

## Complexity Analysis (if applicable)

Writing a log line is O(1); searching unindexed free text is O(n) in
log volume, while an indexed field (like `traceId`) reduces lookup to
near O(log n) or O(1).

## Implementation

```java
final class RequestLogger {
    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    static void logRequest(String traceId, String path, int status, long durationMs) {
        MDC.put("traceId", traceId);
        try {
            log.info("request path={} status={} durationMs={}", path, status, durationMs);
        } finally {
            MDC.clear();
        }
    }
}
```

MDC (Mapped Diagnostic Context) attaches `traceId` to every log line
emitted within scope without threading it through every method
signature.

## Common Mistakes

- Logging free text instead of structured fields, making search
  impractical.

## Common Interview Questions

Q: How would you design a centralized logging system for microservices?

Answer:
Have every service emit structured logs with a shared `traceId`,
forward them via a local agent to a durable queue, index into a
searchable store, and expose a dashboard for filtering, with retention
tiers to control cost.

Q: Why is structured logging preferred over plain text?

Answer:
Structured (JSON) logs have named fields that can be filtered,
aggregated, and indexed programmatically, whereas free-text logs
require fragile regex parsing and cannot be reliably queried at scale.

## Follow-up Interview Questions

Q: How do you control logging cost at very high request volume?

Answer:
Sample high-volume, low-value log lines (log every Nth request at
INFO, but always log errors), apply short hot-tier retention with
cheaper cold storage for older logs, and avoid verbose DEBUG in
production.

## How the Interviewer Thinks

The interviewer wants structured logging, correlation IDs, and
awareness of cost/latency trade-offs at high volume, not just "call a
logging library."

## Production Engineering Perspective

Log shipping is always asynchronous and buffered so a logging outage
cannot back-pressure the application. Indexing is the main bottleneck
under high volume; a durable queue between agents and indexers absorbs
bursts. Evolution: local log files -> centralized aggregation ->
structured logs with trace correlation -> sampling and tiered retention.
Security requires redacting sensitive fields at the source.

## Common Misconceptions

- "More logging is always better" — excessive logging raises cost and
  buries the signal that matters during an incident.

## Revision Notes

- Structured (JSON) logs with a propagated `traceId` are the baseline
  for distributed debugging.

## Summary

Logging is the detailed, after-the-fact forensic record of what a
distributed system actually did; its value depends entirely on being
structured, correlated across services by trace ID, and shipped
asynchronously so it never becomes the bottleneck it exists to
diagnose.

---

# Monitoring

## What is it?

The continuous collection, aggregation, and visualization of
quantitative system metrics, paired with alerting when metrics cross
defined thresholds.

## Why do we need it?

Systems must be observed in real time to detect degradation before
users report it; without monitoring, the first signal is often a
customer complaint, far too late to prevent impact.

## What problem does it solve?

Proactive health visibility: continuously answering "is the system
healthy right now," and triggering a human or automated response when
it is not.

## History (only if useful)

Nagios (late 1990s) established threshold-based infrastructure
monitoring; Prometheus (SoundCloud, open-sourced 2012) popularized the
pull-based, dimensional time-series model underlying most modern
metrics systems.

## How does it work?

### Requirements

Functional: collect metrics, define alert rules, visualize dashboards.
Non-functional: low collection overhead, high cardinality tolerance,
low false-positive/negative alert rates.

### Capacity Estimation

1,000 hosts each exporting 500 metrics every 15 seconds is ~33,000 data
points/sec; at ~2 bytes per compressed sample that is manageable for a
time-series database with proper downsampling for retention.

### Metric Types and the Golden Signals

Counters (monotonically increasing, e.g., total requests), gauges
(point-in-time value, e.g., memory used), and histograms (distribution,
e.g., latency) are the core metric shapes. The four golden signals —
latency, traffic, errors, saturation — form the minimum dashboard for
any service.

## Internal Working

### Architecture

```text
App (exposes /metrics) <-- scrape -- Prometheus -- rule eval --> Alertmanager --> Page/Slack
                                          |
                                          v
                                      Grafana (dashboards)
```

### Request Flow

1. The app exposes current metric values on a `/metrics` endpoint.
2. The monitoring system scrapes or ingests these values on an
   interval.
3. Alert rules evaluate continuously against the stored time series.
4. A rule breach fires an alert, routed to the on-call engineer.

## Step-by-Step Example

Error rate crosses 5% for 5 minutes. The alert rule on
`http_errors_total / http_requests_total` fires, Alertmanager routes it
to the on-call engineer via page, and the dashboard shows the exact
endpoint and time the ratio started climbing.

## Visualization

```text
Counter:   requests_total  (only increases)
Gauge:     memory_used_bytes (goes up and down)
Histogram: request_duration_seconds{le="0.1"}, {le="0.5"}, {le="1.0"}...
```

## Real-World Usage

Prometheus plus Grafana is the dominant open-source stack; Datadog and
New Relic offer the same model as a managed service; Google's SRE
practice popularized golden signals and SLO-based alerting industry-wide.

## Advantages

Enables detecting and responding to degradation before users are
significantly affected; dashboards give a continuously available health
picture without manual investigation.

## Disadvantages

Poorly tuned alerts cause fatigue (too many) or blind spots (too few);
very high cardinality metrics (a label per user ID) can overwhelm a
time-series database's storage and query performance.

## Trade-offs

Fine-grained metrics (per-endpoint, per-user) give precise diagnosis
but cost more storage and query time; coarse metrics are cheap but hide
which case is failing. Threshold alerts are simple but static;
SLO-based (error budget) alerting adapts to actual user impact but is
more complex to set up.

## Alternatives

Manual health checks do not scale and miss fast-moving incidents.
Log-based alerting is more flexible for novel failure modes but far
more expensive to query at real-time alerting frequency.

## Performance Analysis

Metric collection overhead is typically well under 1% CPU versus the
value of early detection; the real cost driver is cardinality — unique
label combinations — which determines database memory and query cost
far more than raw sample volume.

## Complexity Analysis (if applicable)

Recording a counter/gauge increment is O(1); querying a time range
across many series is roughly O(series x points-per-series), which is
why high-cardinality labels are avoided in metrics and left to
logs/traces instead.

## Implementation

```java
final class RequestMetrics {
    private final Counter requests;
    private final Timer latency;

    RequestMetrics(MeterRegistry registry) {
        this.requests = registry.counter("http.requests.total");
        this.latency = registry.timer("http.request.duration");
    }

    void record(Runnable handler) {
        requests.increment();
        latency.record(handler); // times execution, records into histogram buckets
    }
}
```

A Micrometer-style registry lets application code emit dimensional
metrics without hand-building a time-series wire format.

## Common Mistakes

- Alerting on causes (CPU high) instead of user-facing symptoms
  (latency, error rate), causing noisy, low-value pages.

## Common Interview Questions

Q: How would you design a monitoring system for a large-scale service?

Answer:
Have every service expose metrics on a scrape endpoint, collect them
into a time-series database, build dashboards around the four golden
signals, and define alert rules on user-facing symptoms routed to
on-call engineers.

Q: What are the four golden signals?

Answer:
Latency, traffic, errors, and saturation — together they give a
minimal but sufficient picture of a service's health.

## Follow-up Interview Questions

Q: How do you monitor a system with highly variable, seasonal traffic?

Answer:
Use dynamic or relative thresholds (compare against the same time last
week, or a rolling baseline) instead of fixed absolute thresholds, since
a fixed number that works at 3am will be wrong at peak traffic.

## How the Interviewer Thinks

The interviewer wants the four golden signals, an understanding of
cardinality's cost impact, and judgment about symptom-based versus
cause-based alerting to avoid fatigue.

## Production Engineering Perspective

Dashboards are built around the golden signals first, with deeper
per-component metrics for drill-down during an incident. Unbounded
cardinality is the main bottleneck, capped by label guardrails before
data reaches the store. The monitoring system itself going down is a
failure mode needing an independent health check. Evolution: ad hoc
checks -> centralized dashboards -> SLO-based alerting with error
budgets. Security requires restricting who can silence alert rules.

## Common Misconceptions

- "More metrics are always better" — unbounded cardinality directly
  degrades the monitoring system's own performance and cost.

## Revision Notes

- Golden signals: latency, traffic, errors, saturation.

## Summary

Monitoring turns a system's internal state into continuously
observable, alertable signals; the discipline is choosing metrics and
alert thresholds tied to real user impact — golden signals and SLOs —
so engineers are paged for what matters and dashboards stay genuinely
useful during an incident.
