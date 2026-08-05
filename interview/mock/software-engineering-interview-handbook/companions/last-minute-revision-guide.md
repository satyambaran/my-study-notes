# Last-Minute Revision Guide

This is the compression layer of the handbook: every part reduced to the
tables, decision rules, invariants, traps, and diagrams you actually recall
under pressure. It stands alone — usable even before reading the full nine
parts, and detailed enough to re-derive deeper reasoning during the
interview itself.

**How to use it:** in the final week, work through it once a day alongside
targeted practice. In the final 24 hours, read it end to end and stop — open
no new material. In the final hour, skim only the tables and the self-test.
Every self-test answer sits directly beneath its question — cover it,
answer aloud, then check.

## Contents

- [7-Day Study Plan](#7-day-study-plan)
- [Final 24-Hour Plan](#final-24-hour-plan)
- [Interview-Day Checklist](#interview-day-checklist)
- [Part 1: Data Structures and Algorithms](#part-1-data-structures-and-algorithms)
- [Part 2: Operating Systems](#part-2-operating-systems)
- [Part 3: Computer Networking](#part-3-computer-networking)
- [Part 4: Databases](#part-4-databases)
- [Part 5: Distributed Systems](#part-5-distributed-systems)
- [Part 6: Linux, Containers and Kubernetes](#part-6-linux-containers-and-kubernetes)
- [Part 7: Java and Concurrency](#part-7-java-and-concurrency)
- [Part 8: System Design](#part-8-system-design)
- [Part 9: Behavioral Preparation](#part-9-behavioral-preparation)
- [Rapid Self-Test](#rapid-self-test)
- [Final Confidence Checklist](#final-confidence-checklist)

---

## 7-Day Study Plan

| Day | Focus | Do | Self-check before moving on |
|---|---|---|---|
| 1 | DSA foundations | Arrays, strings, hashing, two pointers, sliding window, binary search; 8-10 easy/medium problems | Can state the binary-search and sliding-window invariants from memory |
| 2 | DSA advanced | Trees, graphs (BFS/DFS), heaps, DP families (knapsack, LIS, LCS), backtracking | Solve one medium DP and one graph problem without hints |
| 3 | OS + Networking | Processes/threads, scheduling, memory, deadlocks; TCP/UDP, HTTP versions, DNS, TLS, load balancing | Explain the 3-way handshake and a page-fault path out loud, unaided |
| 4 | Databases + Distributed Systems | ACID, isolation levels, indexing, replication, sharding; CAP/PACELC, Raft/Paxos, 2PC/Saga | Justify an isolation-level choice and a CAP trade-off for a given scenario |
| 5 | Linux/Containers/K8s + Java | Namespaces, cgroups, Docker internals; K8s objects and probes; JVM memory, GC, concurrency primitives | Draw the Kubernetes architecture and JVM memory layout from memory |
| 6 | System design | Estimation drills, one read-heavy design, one write-heavy design end-to-end | Complete a 45-minute mock design with trade-offs stated aloud |
| 7 | Behavioral + full mock + rest | Prepare 6-8 STAR stories; one full mixed mock interview; light review only, sleep early | Deliver three STAR stories in under three minutes each |

**Rule:** every day ends with active recall (say answers out loud or write
them closed-book), not re-reading. Re-reading feels like progress and isn't.

## Final 24-Hour Plan

| Window before the interview | Activity |
|---|---|
| 24h - 16h | Normal wind-down; review only topics you flagged as weak this week; no new material |
| 16h - 8h | Protect 7-8 hours of sleep — sleep consolidates memory better than extra cramming |
| 8h - 4h | Wake, light movement, food; one pass over this guide's tables (skim, don't re-derive) |
| 4h - 1h | Logistics check (below); re-read your 3 strongest STAR stories once; skim the Rapid Self-Test, flag only misses |
| 1h - 0h | No new material; one already-solved easy problem to warm up recall; hydrate; join/arrive 10 minutes early |

**Rule:** if you don't know it by T-24h, a fact crammed at T-1h rarely survives
contact with interview stress. Spend the last hour on retrieval warm-up, not
acquisition.

## Interview-Day Checklist

**Before**

- Environment: quiet room, charged laptop + backup charger, backup internet
  (phone hotspot ready), webcam/mic tested, familiar IDE open, notepad and pen.
- Materials: resume open, project talking points ready, water nearby.
- Mindset: one warm-up problem solved, 60 seconds of box breathing (4-4-4-4)
  if anxious, arrive/join early rather than exactly on time.

**During**

- Restate the problem in your own words before writing anything.
- Clarify constraints, inputs, edge cases, and scale out loud.
- State a brute-force approach and its complexity first, then optimize.
- Narrate your thinking continuously — silence reads as being stuck.
- Test your solution against a concrete example and at least one edge case.
- State final time/space complexity without being asked.
- For design rounds: requirements before boxes, boxes before deep dives.
- For behavioral rounds: STAR structure, quantify the result.
- Ask thoughtful questions at the end — this is scored, not a formality.

**After**

- Write down the questions you were asked while memory is fresh.
- Note what you fumbled — feed it back into tomorrow's weak-topic review.
- Do not post-mortem obsessively; one pass, then disengage.

---

## Part 1: Data Structures and Algorithms

*Full depth: `parts/part-01-data-structures-and-algorithms.md`*

**Scale-to-complexity rule of thumb**

| Input size n | Complexity you need |
|---|---|
| n ≤ ~12 | Exponential/backtracking is fine |
| n ≤ ~1,000 | O(n²) or O(n³) is fine |
| n ≤ ~10⁵-10⁶ | Need O(n log n) or O(n) |
| n ≤ ~10⁹ | Need O(log n), O(1), or an offline/streaming trick |

**Data structure complexity (Java-flavored)**

| Structure | Search | Insert | Delete | Notes |
|---|---|---|---|---|
| ArrayList | O(n) | O(1) amortized at end | O(n) | Contiguous, cache-friendly |
| ArrayDeque | O(n) | O(1) at both ends | O(1) at both ends | Prefer over `Stack`/`LinkedList` |
| HashMap/HashSet | O(1) avg | O(1) avg | O(1) avg | O(log n) worst case after treeify (bucket ≥ 8, capacity ≥ 64) |
| TreeMap/TreeSet | O(log n) | O(log n) | O(log n) | Red-black tree, sorted iteration, floor/ceiling |
| PriorityQueue | O(n) | O(log n) | O(log n) peek O(1) | Binary heap over an array |
| Union-Find | ~O(α(n)) amortized | - | - | Path compression + union by rank/size |

**Pattern recognition table**

| Signal in the prompt | Pattern | Complexity |
|---|---|---|
| Sorted array, find target/boundary | Binary search | O(log n) |
| Contiguous subarray/substring + constraint | Sliding window | O(n) |
| Pair/triplet sum on sorted input | Two pointers | O(n) |
| "Top K" / "kth largest" | Bounded heap | O(n log k) |
| Merge K sorted lists/streams | Heap | O(n log k) |
| Overlapping subproblems, optimal substructure | Dynamic programming | O(n) to O(n²) |
| Shortest path, unweighted | BFS | O(V+E) |
| Shortest path, weighted, non-negative | Dijkstra + heap | O((V+E) log V) |
| Shortest path, negative edges | Bellman-Ford | O(V·E) |
| All-pairs shortest path | Floyd-Warshall | O(V³) |
| "Are these connected/grouped" | Union-Find | ~O(1) amortized/op |
| Next greater/smaller element | Monotonic stack | O(n) |
| Sliding window max/min | Monotonic deque | O(n) |
| Subsets/permutations/combinations | Backtracking | O(2ⁿ) / O(n!) |
| Interval merge/overlap/scheduling | Sort + greedy | O(n log n) |
| Cycle detection, linked list | Floyd's tortoise/hare | O(n) |
| Range sum/update queries | Fenwick tree / segment tree | O(log n) per op |
| Minimum spanning tree | Kruskal+DSU / Prim+heap | O(E log E) |
| Topological order | Kahn's / DFS postorder | O(V+E) |

**Decision rules**

- Need ordering (floor/ceiling, sorted iteration) → TreeMap; only fast
  lookup → HashMap.
- Recursion calling the same `(state)` more than once → add memoization
  before any other optimization.
- Weighted graph, non-negative edges, n up to ~10⁵ → Dijkstra with a heap,
  never Bellman-Ford unless negative edges are possible.
- "Kth" anything → bounded heap of size K, never a full sort.

**Key invariants**

- Binary search: the answer always lies in `[lo, hi]`; compute mid as
  `lo + (hi - lo) / 2` to dodge overflow; fix inclusive/exclusive bounds once
  and never mix them mid-solution.
- Sliding window: the right pointer only grows, the left only shrinks — each
  moves ≤ n times, so total work is O(n), not O(n²).
- Backtracking: choose → recurse → un-choose; undo state before returning up
  the call stack.
- Union-Find: path compression *and* union by rank/size together give the
  near-constant amortized bound; either alone gives only O(log n).

**Common traps**

- Off-by-one at binary-search boundaries (`<` vs `<=`).
- Integer overflow summing large arrays in Java — accumulate into `long`.
- Mutating a collection while iterating it (`ConcurrentModificationException`).
- BFS marking visited at *dequeue* instead of *enqueue* time — causes
  duplicate work and wrong shortest-path counts.
- Treating DFS preorder as a topological order instead of reversed postorder.
- A `Comparator` that isn't transitive throws at runtime in Java's sort.

**Memory aid**

```text
Two pointers (sorted array, target sum):        Min-heap array mapping:
[1  3  4  6  8  11]                                     1
 L              R     sum<target -> L++              /   \
                       sum>target -> R--             3     5
                                                     / \   /
Sliding window:                                    4  6  8
s = a b c a b c b b                          array: [1,3,5,4,6,8]
    [i.....]                                 parent(i)=(i-1)/2
        j->    grow j; shrink i on violation left=2i+1 right=2i+2
```

---

## Part 2: Operating Systems

*Full depth: `parts/part-02-operating-systems.md`*

**Process vs thread**

| Aspect | Process | Thread |
|---|---|---|
| Memory | Own address space | Shares address space with siblings |
| Creation | Heavy (`fork`/`exec`) | Light |
| Communication | IPC (pipes, sockets, shared memory) | Shared variables (needs sync) |
| Crash isolation | One crash doesn't kill others | Can take down the whole process |
| Context switch | Expensive (MMU/TLB change) | Cheaper (same address space) |

**Scheduling**

| Algorithm | Idea | Weakness |
|---|---|---|
| FCFS | Arrival order | Convoy effect |
| SJF/SRTF | Shortest job/remaining time first | Starves long jobs |
| Priority | Highest priority runs first | Starvation without aging |
| Round Robin | Fixed time quantum, cyclic | Too small = overhead, too large = FCFS |
| Multilevel feedback queue | Several queues, demote CPU-bound jobs | Hard to tune |
| CFS (Linux default) | Red-black tree ordered by vruntime; run the least-served task | Not hard real-time |

**Memory management:** paging uses fixed-size frames and a page table (the
TLB caches recent translations; a miss triggers a page-table walk; a missing
page triggers a page fault). Segmentation uses variable-size logical units
and can externally fragment. Virtual memory gives each process a private
address space via demand paging; thrashing occurs when the combined working
set exceeds physical RAM.

**Concurrency primitives**

| Primitive | Ownership | Blocking | Use case |
|---|---|---|---|
| Mutex | Yes | Yes | Protect one critical section |
| Semaphore (counting) | No | Yes | Bound access to N resources |
| Spinlock | Yes | No (busy-wait) | Very short critical sections |
| Monitor (mutex + condition var) | Yes | Yes | Wait/notify coordination |

**Deadlock:** requires all four Coffman conditions simultaneously — mutual
exclusion, hold-and-wait, no preemption, circular wait. Prevention breaks one
condition; avoidance uses the Banker's algorithm (only grant a request if the
resulting state is still safe); detection builds a wait-for graph and looks
for cycles; recovery kills or preempts a process.

**Decision rules**

- Critical section is a handful of instructions, low contention → spinlock;
  may block on I/O or take a while → mutex/monitor.
- N interchangeable resources (e.g., a connection pool of 10) → semaphore(10),
  not a plain mutex.
- High throughput with low page-fault activity but poor overall progress →
  suspect thrashing, not CPU starvation.

**Key invariants**

- Only one thread holds a mutex at a time; the section between lock/unlock is
  atomic with respect to other lockers.
- A "safe state" in the Banker's algorithm guarantees at least one execution
  order lets every process finish.
- TLB and page table always agree on the current mapping; a miss falls back
  to a slower walk, never to stale data.

**Common traps**

- Conflating concurrency (structure/interleaving) with parallelism
  (simultaneous execution).
- Believing visibility alone fixes a check-then-act race — compound
  operations still need a lock or CAS.
- Assuming more threads always means more throughput once CPU-bound work
  exceeds core count.

**Memory aid**

```text
Registers ~0.5ns  L1 ~1ns  L2 ~7ns  L3 ~20ns  RAM ~100ns  SSD ~100us  HDD ~10ms
(closer to the CPU = smaller, faster, pricier per byte)

Virtual Addr -> TLB hit? --yes--> Physical Addr
                   |no
                   v
             Page Table Walk -> update TLB -> Physical Addr
             (page missing -> Page Fault -> load from disk)
```

---

## Part 3: Computer Networking

*Full depth: `parts/part-03-computer-networks.md`*

**OSI vs TCP/IP**

| OSI layer | TCP/IP layer | Examples |
|---|---|---|
| 7 Application / 6 Presentation / 5 Session | Application | HTTP, DNS, TLS, gRPC |
| 4 Transport | Transport | TCP, UDP, QUIC |
| 3 Network | Internet | IP, ICMP, routing |
| 2 Data Link / 1 Physical | Link | Ethernet, ARP, switches, cabling |

**TCP vs UDP**

| | TCP | UDP |
|---|---|---|
| Connection | Connection-oriented, handshake | Connectionless |
| Reliability | Ordered, guaranteed, retransmits | Best-effort |
| Overhead | Higher (ACKs, congestion control) | Lower |
| Typical use | HTTP, DB connections, file transfer | DNS, media, QUIC/HTTP3 |

**Connection lifecycle**

```text
Handshake (3-way)                  Teardown (4-way)
Client         Server              Client         Server
  SYN     ---->                      FIN     ---->
         <---- SYN-ACK                       <---- ACK
  ACK     ---->                              <---- FIN
  [connection open]                   ACK    ---->
                                     [TIME_WAIT ~2*MSL]
```

TCP reliability: sequence numbers + cumulative ACKs, retransmit on timeout,
sliding-window flow control, and congestion control phases — slow start
(exponential growth) → congestion avoidance (linear/AIMD) → on loss, fast
retransmit + fast recovery (timeout instead resets to slow start).

**HTTP versions**

| Version | Transport | Key feature | Limitation |
|---|---|---|---|
| HTTP/1.1 | TCP | Persistent connections | Head-of-line blocking per connection |
| HTTP/2 | TCP | Multiplexed streams, HPACK, server push | One lost TCP packet stalls all streams |
| HTTP/3 | QUIC/UDP | Streams multiplexed over QUIC, TLS 1.3 built in | Needs UDP-friendly network path |

**DNS resolution:** browser cache → OS resolver cache → recursive resolver →
root server (→ TLD NS) → TLD server (→ authoritative NS) → authoritative
server (→ A/AAAA record); every hop caches per the record's TTL.

**Load balancing**

| Algorithm | Idea | Good for |
|---|---|---|
| Round robin | Cycle through servers | Uniform, stateless capacity |
| Weighted round robin | Cycle proportioned by capacity | Heterogeneous servers |
| Least connections | Fewest active connections wins | Variable request duration |
| IP hash / consistent hash | Same client → same server | Session affinity, cache locality |

TLS 1.3 handshake: ClientHello (ciphers + key share) → ServerHello (chosen
cipher + key share + certificate + Finished) → client verifies and sends
Finished → application data after 1 RTT (2 RTT in TLS 1.2); session
resumption via PSK can reach 0-RTT.

**Decision rules**

- Need ordered, reliable delivery → TCP; need low latency and can tolerate or
  handle loss at the app layer → UDP/QUIC.
- Clients need cache-locality/session stickiness → IP hash/consistent hash at
  the LB; otherwise round robin/least-connections is simpler and fairer.
- A call must be safely auto-retried → make it idempotent first (GET, PUT,
  DELETE are idempotent by spec; POST is not).

**Key invariants**

- TCP guarantees an in-order, once-delivered byte *stream* — not message
  boundaries.
- A sender's effective window is `min(congestion window, receiver window)` —
  congestion control and flow control are independent constraints.

**Common traps**

- Treating "stateless HTTP" as "no state anywhere" — cookies/sessions/JWTs
  reintroduce state at the application layer.
- Believing HTTP/2 multiplexing removes all head-of-line blocking — a single
  lost TCP packet still stalls every stream on that connection (HTTP/3 fixes
  this by moving multiplexing below TCP's ordering guarantee).

---

## Part 4: Databases

*Full depth: `parts/part-04-databases.md`*

**ACID**

| Property | Meaning |
|---|---|
| Atomicity | Transaction is all-or-nothing |
| Consistency | DB moves between states valid per its own constraints |
| Isolation | Concurrent transactions appear to run serially |
| Durability | Committed data survives a crash (WAL + fsync) |

**Isolation levels and anomalies**

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| Read Uncommitted | Possible | Possible | Possible |
| Read Committed | Prevented | Possible | Possible |
| Repeatable Read | Prevented | Prevented | Possible (InnoDB's MVCC largely avoids this in practice) |
| Serializable | Prevented | Prevented | Prevented |

Defaults: PostgreSQL and Oracle → Read Committed; MySQL InnoDB → Repeatable
Read.

**Indexing**

| Structure | Best for | Weak for | Used by |
|---|---|---|---|
| B-Tree/B+Tree | Range queries, sorted scans | Write amplification at extreme scale | Postgres, MySQL InnoDB |
| LSM-Tree | Write-heavy, sequential ingestion | Read amplification, needs compaction | Cassandra, RocksDB, LevelDB |
| Hash index | Exact-match equality | No range queries | In-memory KV stores |

**Normalization:** 1NF (atomic columns), 2NF (1NF + no partial dependency on
part of a composite key), 3NF (2NF + no transitive dependency), BCNF (every
determinant is a candidate key). Denormalization trades write complexity and
redundancy for read speed.

**Replication and sharding:** leader-follower replication is sync (durable,
higher latency) or async (lower latency, risk of loss on failover);
multi-leader needs conflict resolution (last-write-wins, CRDTs); leaderless
(Dynamo-style) uses quorum reads/writes. Sharding is range-based (good range
scans, hotspot risk), hash-based (even distribution, poor range scans), or
directory-based (flexible, extra lookup hop).

**Concurrency control:** 2PL acquires locks in a growing phase and releases
in a shrinking phase, guaranteeing serializability at deadlock risk; MVCC
gives readers a versioned snapshot so writers never block readers (Postgres,
InnoDB, Oracle).

**Decision rules**

- Write-heavy, sequential ingestion (logs, metrics) → LSM-tree engine;
  read-heavy with range scans → B+Tree engine.
- Only need to avoid dirty data → Read Committed; need a frozen snapshot for
  the whole transaction → Repeatable Read/Snapshot Isolation; need full
  correctness across ranges (ledgers) → Serializable, accept lower throughput.
- Frequent cross-shard range queries → range/directory sharding over hash
  sharding.

**Key invariants**

- 2PL: once a transaction releases its first lock, it may never acquire a new
  one — this is exactly what guarantees serializability.
- MVCC: conflicts are only writer-writer; a reader never blocks a writer and
  vice versa.
- Quorum systems: `R + W > N` guarantees any read quorum and write quorum
  intersect in at least one node.

**Common traps**

- Confusing ACID "Consistency" (application invariants) with CAP
  "Consistency" (all nodes agree on the latest value).
- Assuming Serializable is the default isolation level — most engines default
  to Read Committed or Repeatable Read for performance.
- The N+1 query problem: one query per row instead of a single join/batch.
- Missing an index on a foreign key, turning every join or cascade into a
  full table scan.

**Memory aid**

```text
B+Tree (in place):                 LSM-Tree (append-only):
     [10 | 20]                     write -> WAL -> MemTable (in-mem)
    /    |    \                    memtable full -> flush -> SSTable (L0)
  <10  10-20  >20                  background compaction merges SSTables
  leaves hold sorted rows          reads check memtable -> L0 -> L1 -> ...
```

---

## Part 5: Distributed Systems

*Full depth: `parts/part-05-distributed-systems.md`*

**CAP / PACELC**

| Theorem | Statement |
|---|---|
| CAP | During a network Partition, choose Consistency or Availability, not both |
| PACELC | ...Else (no partition), trade Latency against Consistency |

**Rule:** partition tolerance is not optional on a real multi-node network, so
CAP is really "C or A when a partition happens," not "pick any two of three."

**Consensus**

| Algorithm | Roles | Key idea | Used by |
|---|---|---|---|
| Paxos | Proposer, Acceptor, Learner | Majority agreement in Prepare/Promise + Accept/Accepted phases | Chubby, Spanner (variants) |
| Raft | Leader, Follower, Candidate | Leader election by term + log replication; easier to reason about | etcd, Consul, CockroachDB |

Raft essentials: at most one leader per term; the leader replicates entries
via `AppendEntries` + heartbeats; an entry commits once on a majority of
logs; a candidate can only win if its log is at least as up to date as a
majority (election restriction) — this keeps committed entries from being
lost across leader changes (Leader Completeness).

**Delivery semantics**

| Semantics | Meaning | How you actually get it |
|---|---|---|
| At-most-once | Delivered 0 or 1 times | Fire-and-forget, no retry |
| At-least-once | Delivered 1+ times | Retry + ack; consumer must dedupe |
| Exactly-once | Delivered/processed exactly once | Approximated: idempotency keys + at-least-once + dedup store |

**Consistent hashing:** nodes and keys hash onto the same ring; a key belongs
to the first node clockwise from its hash; adding/removing one node remaps
only ~K/N keys; virtual nodes smooth load imbalance across physical nodes.

**Distributed transactions:** 2PC has a coordinator collect prepare votes
then broadcast commit/abort — simple but blocks participants if the
coordinator dies mid-protocol. Sagas run a sequence of local transactions,
each with a compensating action to undo it on failure; orchestration (central
coordinator) trades off against choreography (event-driven).

**Clocks:** wall clocks drift and jump (NTP), so never use them to order
cross-node events. Lamport timestamps give a causally consistent partial
order; vector clocks additionally detect concurrent/conflicting writes.

**Decision rules**

- Partition is possible (assume yes) and the system must keep serving →
  Availability (AP); it must refuse rather than risk stale data →
  Consistency (CP).
- Need strongly consistent coordination (leader election, locks, config) →
  a Raft/Paxos-backed store, not an eventually-consistent one.
- A multi-service operation is too slow/blocking for 2PC → use a Saga with
  idempotent, compensable steps.

**Key invariants**

- Raft safety: at most one leader per term; committed entries survive every
  future leader.
- Idempotency invariant: applying an operation N times equals applying it
  once — this is what makes "at-least-once + dedup" behave like exactly-once
  to the caller.

**Common traps**

- Assuming the network is reliable, has zero latency, or never changes
  topology (the Fallacies of Distributed Computing).
- Split-brain: two nodes both believe they lead after a partition and both
  accept writes — prevented by requiring majority quorum to become/stay leader.
- Treating a message queue as exactly-once by default — most are at-least-once;
  exactly-once needs consumer-side idempotency.

**Memory aid**

```text
        N1                          Follower --(timeout)--> Candidate
     /       \                          ^                       |
  key_x       N2                        |            (wins majority vote)
     \       /                          |                       v
        N3                              +--------------------- Leader
hash(key) -> walk clockwise             (discovers higher term -> back to Follower)
-> first node found owns the key
```

---

## Part 6: Linux, Containers and Kubernetes

*Full depth: `parts/part-06-linux-and-infrastructure.md`*

**Linux namespaces**

| Namespace | Isolates |
|---|---|
| PID | Process IDs (own PID 1 inside) |
| NET | Interfaces, routing, ports |
| MNT | Filesystem mount points |
| UTS | Hostname/domain name |
| IPC | Message queues, shared memory, semaphores |
| USER | User/group IDs (root inside, unprivileged outside) |
| CGROUP | View of the cgroup hierarchy itself |

cgroups limit/measure CPU, memory, and I/O per process group (v2 unifies the
hierarchy into one tree). A **container** = namespaces (isolation) + cgroups
(limits) + a union filesystem (OverlayFS: read-only image layers plus one
writable layer) + an OCI image format. Docker's path: `docker` CLI →
`dockerd` → `containerd` (lifecycle) → `runc` (OCI runtime, creates the
actual namespaces/cgroups).

**Kubernetes architecture**

| Component | Location | Job |
|---|---|---|
| kube-apiserver | Control plane | REST front door; only component that talks to etcd |
| etcd | Control plane | Raft-backed key-value store holding all cluster state |
| kube-scheduler | Control plane | Assigns unscheduled Pods to Nodes |
| kube-controller-manager | Control plane | Runs reconciliation loops |
| kubelet | Node | Ensures containers match the PodSpec |
| kube-proxy | Node | Implements Service virtual IPs (iptables/IPVS) |
| Container runtime | Node | Runs containers (containerd, CRI-O) |

**Kubernetes objects**

| Object | Purpose | Notes |
|---|---|---|
| Pod | Smallest deployable unit | Ephemeral; gets a new IP on recreation |
| Deployment | Rollout/rollback of stateless ReplicaSets | Rolling updates, revision history |
| StatefulSet | Stable identity + storage per replica | Databases, brokers |
| DaemonSet | One Pod per matching node | Log/metrics agents |
| Service | Stable virtual IP/DNS to matching Pods | ClusterIP, NodePort, LoadBalancer |
| Ingress | L7 HTTP(S) routing into the cluster | Needs an ingress controller |
| ConfigMap/Secret | Externalized config | Secrets are base64, not encrypted by default |
| PV/PVC | Storage / a claim on it | Decouples provisioning from Pod spec |
| Job/CronJob | Run-to-completion / scheduled | Retries per backoff policy |
| HPA | Auto-scales replicas on metrics | Needs metrics-server |

Probes: liveness failure → kubelet restarts the container; readiness failure
→ Pod is pulled from Service endpoints (no traffic) but not restarted.
`requests` drive scheduling; `limits` are enforced at runtime — CPU is
throttled, memory triggers an OOM kill.

**Decision rules**

- Stateless, interchangeable → Deployment; stable identity/storage per
  replica → StatefulSet; exactly one per node → DaemonSet.
- External HTTP routing by host/path → Ingress; simple internal traffic →
  ClusterIP Service.
- Process alive but hung → needs a real liveness probe, not just a process
  check.
- Random OOM kills under load → check memory `limits` first; unresponsive
  under load → check CPU `limits` (throttling) before blaming app code.

**Key invariants**

- A Pod's IP is not stable across restarts — anything needing a stable
  address must go through a Service.
- Controllers are level-triggered reconciliation loops (observe → diff →
  act) — self-healing by design, not one-shot scripts.

**Common traps**

- Writing to a container's filesystem without a volume and expecting it to
  survive rescheduling.
- No `PodDisruptionBudget`, so a node drain can take down all replicas at once.
- Using `latest` image tags, breaking reproducible rollouts/rollbacks.
- `kubectl delete pod` on a Deployment-managed Pod "fixing" nothing — the
  ReplicaSet just recreates it from the same, possibly broken, spec.

**Memory aid**

```text
       Control Plane                            Worker Node
+--------------------------+           +---------------------------+
| kube-apiserver <-> etcd  |<--------->|   kubelet -> container     |
|  scheduler, controller   |  watches  | kube-proxy -> Service IP   |
+--------------------------+           +---------------------------+

Container = Namespaces (see only "yours") + cgroups (use only "your share")
          + OverlayFS (read-only layers + one writable layer)
```

---

## Part 7: Java and Concurrency

*Full depth: `parts/part-07-java.md`*

**JVM memory areas**

| Area | Per | Holds | GC'd? |
|---|---|---|---|
| Heap (Young: Eden+S0+S1, Old) | Process | Objects | Yes |
| Metaspace | Process | Class metadata (native memory, replaced PermGen since Java 8) | Yes, on class unload |
| Stack | Thread | Frames: locals, partial results, return address | No, popped on return |
| PC register / native stack | Thread | Current instruction / JNI calls | No |

**Garbage collectors**

| Collector | Style | Notes |
|---|---|---|
| Serial | Single-threaded, stop-the-world | Small heaps |
| Parallel | Multi-threaded, stop-the-world | Throughput-optimized |
| G1 (default since JDK 9) | Region-based, mostly concurrent | Predictable pause-time goal |
| ZGC / Shenandoah | Concurrent, region-based | Sub-ms to low-ms pauses on very large heaps |

**Concurrency toolkit**

| Tool | Category | Notes |
|---|---|---|
| `synchronized` | Intrinsic monitor lock | Reentrant, auto-released even on exception |
| `volatile` | Visibility + ordering | Not atomicity for compound ops like `count++` |
| `ReentrantLock` | Explicit lock | `tryLock`, timeouts, fairness; unlock in `finally` |
| `Atomic*` | Lock-free (CAS) | Counters/flags without blocking |
| `Semaphore` | Counting permits | Bound concurrent access to N resources |
| `CountDownLatch` | One-time gate | Wait for N events, not reusable |
| `CyclicBarrier` | Reusable gate | N threads rendezvous, together, repeatedly |
| `ExecutorService` | Thread pool | Prefer over manually created `Thread`s |
| `ConcurrentHashMap` | Concurrent map | Fine-grained locking/CAS, no global lock |
| `CompletableFuture` | Async composition | `.thenApply/.thenCompose/.allOf` pipelines |

Java Memory Model happens-before rules: program order within a thread; a
monitor unlock happens-before a later lock on the same monitor; a volatile
write happens-before a later volatile read of the same field; `Thread.start()`
happens-before the new thread's code; a thread's actions happen-before a
successful `join()` on it.

**Decision rules**

- Just need visibility of one flag/reference → `volatile`; need an atomic
  read-modify-write → `AtomicInteger`/`AtomicReference` or a lock.
- Critical section may block on I/O → `synchronized`/`ReentrantLock`;
  lock-free counter under high contention → prefer `LongAdder`/`Atomic*`.
- Bounded producer/consumer → a `BlockingQueue` implementation, not manual
  `wait`/`notify`.
- Very large heap, pause time critical → ZGC/Shenandoah over G1; throughput
  matters more than pause time → Parallel GC.

**Key invariants**

- `synchronized` is reentrant — the same thread can re-acquire its own
  monitor without self-deadlocking.
- `volatile` guarantees every read sees the latest happens-before write, but
  a compound `x++` on a volatile field can still race — visibility is not
  atomicity.
- The generational hypothesis (most objects die young) is why the heap
  splits into young/old generations with different collection frequencies.

**Common traps**

- Using `String` literals or cached boxed `Integer`s (-128..127) as lock
  objects — accidental shared locks across unrelated code.
- Mutating a non-concurrent collection during iteration →
  `ConcurrentModificationException`.
- Assuming `HashMap` is thread-safe — it is not; use `ConcurrentHashMap`.
- Forgetting `finally` around `lock.unlock()` — an exception leaves the lock
  held forever.

**Memory aid**

```text
+------------- Heap -------------+  +--- Metaspace ----+
| Young Gen: Eden + S0 + S1      |  | class metadata   |
| Old Gen (tenured)              |  +------------------+
+--------------------------------+
Per thread: Stack | PC Register | Native method stack
```

---

## Part 8: System Design

*Full depth: `parts/part-08-system-design.md`*

**Interview framework**

1. Clarify functional and non-functional requirements (scale, latency,
   consistency) — never skip this.
2. Back-of-envelope estimation: QPS, storage, bandwidth.
3. Define the API contract and core data model.
4. Draw the high-level architecture.
5. Deep-dive the one or two components that carry the hardest trade-off.
6. Call out bottlenecks and single points of failure, and how to harden them.
7. Mention monitoring, alerting, and rollout strategy.

**Latency numbers (order of magnitude, hardware-dependent)**

| Operation | Approx. latency |
|---|---|
| L1 cache reference | ~1 ns |
| Branch mispredict | ~5 ns |
| L2 cache reference | ~7 ns |
| Mutex lock/unlock | ~25 ns |
| Main memory reference | ~100 ns |
| Compress 1 KB | ~3 µs |
| Send 1 KB over 1 Gbps network | ~10 µs |
| Read 4 KB randomly from SSD | ~150 µs |
| Round trip within the same data center | ~0.5 ms |
| Disk seek | ~10 ms |
| Round trip cross-continent | ~100-150 ms |

**Rule of thumb:** memory is roughly 100x faster than SSD, and SSD is
roughly 100x faster than a cross-continent round trip — removing a network
hop usually beats micro-optimizing code on the hot path.

**Estimation quick math:** a day is ~10⁵ seconds (86,400 ≈ 10⁵); average
QPS ≈ daily requests / 10⁵; peak QPS ≈ 2-5x average; storage ≈ records ×
average size × replication factor.

**Scaling building blocks**

| Block | Solves | Watch out for |
|---|---|---|
| Load balancer | Distributes traffic, removes SPOF | The LB itself needs HA |
| Cache | Read latency, DB load | Invalidation, stampede on expiry |
| CDN | Static asset latency, origin load | Cache-Control/ETag correctness |
| Message queue | Decouples producer/consumer, absorbs spikes | Consumer lag, needs idempotent consumers |
| Read replicas | Scales reads | Replication lag → stale reads |
| Sharding | Scales writes/storage | Cross-shard queries/transactions get hard |
| Rate limiting | Protects from abuse/overload | Fairness across tenants |
| Circuit breaker/bulkhead | Contains cascading failure | Needs a sane fallback |

**Decision rules**

- Read-heavy workload → cache + read replicas first; write-heavy/high
  ingestion → queue + async processing + sharding first.
- Hard consistency requirement (payments, inventory) → single-leader/
  serializable paths even at higher latency; tolerable staleness (feeds,
  view counts) → cache aggressively, go eventually consistent.
- No scale given → state an assumption out loud and move on, don't stall.
- "How would this fail" → pick the component you just added and explain its
  failure mode and mitigation, not a generic answer.

**Key invariants**

- A cache is never the source of truth — it must always be reconstructable
  from the system of record.
- The one component you cannot horizontally scale (a single DB writer, a
  single queue partition) is the system's real ceiling, regardless of how
  many services sit in front of it.

**Common traps**

- Diving into schema/algorithm detail before agreeing on requirements and
  scale.
- Proposing "just add a cache" or "just shard it" without naming the
  specific invalidation/resharding problem it creates.
- Never mentioning monitoring or rollout — treating the design as done once
  boxes are drawn.

**Memory aid**

```text
Client -> CDN (static) -> LB -> App Servers -> Cache -> DB (leader+replicas)
                                     \-> Queue -> Workers -> DB / Search index
```

---

## Part 9: Behavioral Preparation

*Full depth: `parts/part-09-behavioral-preparation.md`*

**STAR**

| Letter | Meaning | Tip |
|---|---|---|
| S | Situation | 1-2 sentences of context, enough to convey stakes |
| T | Task | Your specific responsibility or goal |
| A | Action | What *you* did — "I," not just "we" |
| R | Result | Quantified impact, plus what you learned or changed after |

**Common categories**

| Category | Example prompt | Real signal being tested |
|---|---|---|
| Conflict | Disagreement with a teammate | Professional conflict resolution |
| Failure | A project that failed | Ownership, honesty, learning |
| Ambiguity | Requirements were unclear | Driving clarity and progress anyway |
| Leadership/influence | Led without authority | Aligning people without formal power |
| Tight deadline | Delivered under pressure | Prioritization, trade-off communication |
| Mistake | A mistake you made | Self-awareness and follow-through |
| Cross-team | A difficult stakeholder | Communication and empathy under friction |

**Decision rules**

- Prepare 6-8 stories that each flex to 2-3 prompts rather than memorizing
  20 rigid answers.
- No perfect story for a prompt → use the closest real one and be honest
  about scope; do not invent details.
- "What would you do differently" → always have a genuine answer.
- Match story scope to the level you're interviewing for: IC → depth of
  individual execution; senior/lead → influence and system-level trade-offs.

**Key invariants**

- Every story needs a measurable or observable result — "we shipped it" is
  not a result; "cut checkout p99 latency from 900ms to 220ms" is.
- The interviewer evaluates the decision process, not only the outcome — a
  sound decision with a bad-luck outcome can still be a strong answer.

**Common traps**

- Rambling without S-T-A-R structure, burying the actual action.
- Blaming teammates or management instead of owning your part.
- Reciting a resume bullet instead of telling a story with real tension.
- No quantifiable result, or one that doesn't map back to the stated task.
- Skipping questions at the end — treat it as a real evaluation signal.

**Memory aid**

```text
S -> T -> A -> R
(context) (goal) (YOUR actions) (measurable outcome + learning)
```

---

## Rapid Self-Test

Cover the answer, say it out loud, then check. Every question's answer is
the line immediately below it.

**DSA**

Q1. What loop invariant must hold at every step of binary search?
A1. The target, if it exists, always lies within the current `[lo, hi]` bounds.

Q2. Why is `lo + (hi - lo) / 2` preferred over `(lo + hi) / 2`?
A2. It avoids integer overflow when `lo` and `hi` are both large.

Q3. When should you reach for a heap instead of sorting the whole array?
A3. When you only need the top/bottom K elements — O(n log k) beats a full O(n log n) sort.

Q4. What is the amortized complexity of Union-Find with path compression and union by rank?
A4. ~O(α(n)) per operation — the inverse Ackermann function, effectively constant.

Q5. Why does BFS, not DFS, find shortest paths in an unweighted graph?
A5. BFS visits nodes in strictly increasing distance order, so the first visit to any node is via a shortest path.

Q6. When does Dijkstra's algorithm give the wrong answer?
A6. When the graph has negative edge weights — use Bellman-Ford instead.

Q7. Why can a recursive solution be exponential even when the recurrence looks simple?
A7. Overlapping subproblems get recomputed repeatedly without memoization (e.g., naive recursive Fibonacci).

Q8. What's the point of a monotonic stack for "next greater element" problems?
A8. Each element is pushed and popped at most once, giving O(n) total work instead of O(n²).

**Operating Systems**

Q9. What four conditions must all hold simultaneously for deadlock?
A9. Mutual exclusion, hold-and-wait, no preemption, and circular wait.

Q10. What's the practical difference between a mutex and a semaphore?
A10. A mutex has ownership and protects one critical section; a semaphore has no ownership and permits N concurrent holders.

Q11. Why is a spinlock sometimes faster than a mutex?
A11. For very short critical sections, busy-waiting avoids the cost of a context switch — but it wastes CPU if held too long.

Q12. What causes thrashing?
A12. The combined working set of running processes exceeds physical memory, so the system spends more time paging than computing.

Q13. What does Linux's CFS scheduler actually optimize for?
A13. It always runs the task with the least accumulated virtual runtime (vruntime), approximating a fair CPU share for all runnable tasks.

Q14. Why is a thread context switch cheaper than a process context switch?
A14. Threads share an address space, so the MMU/page tables and TLB don't need to be swapped — only registers and the stack pointer change.

**Networking**

Q15. What three packets make up the TCP handshake?
A15. SYN, SYN-ACK, ACK.

Q16. Why does TCP use a 4-way teardown?
A16. Each direction of the full-duplex connection is closed independently (FIN/ACK each way).

Q17. What problem does HTTP/2 multiplexing solve versus HTTP/1.1?
A17. It removes application-layer head-of-line blocking by running multiple streams over one TCP connection.

Q18. Why can HTTP/2 still stall on packet loss despite multiplexing?
A18. All streams share one TCP connection, and TCP guarantees in-order delivery, so one lost packet blocks every stream until retransmitted (HTTP/3 fixes this with QUIC).

Q19. Is an HTTP POST idempotent?
A19. No — GET, PUT, DELETE, and HEAD are idempotent by spec; POST is not, so it is unsafe to auto-retry blindly.

Q20. What determines how long a DNS answer can be cached?
A20. The record's TTL, set by the authoritative nameserver.

**Databases**

Q21. What isolation level do PostgreSQL and MySQL InnoDB default to, respectively?
A21. PostgreSQL defaults to Read Committed; MySQL InnoDB defaults to Repeatable Read.

Q22. What anomaly does Repeatable Read prevent that Read Committed does not?
A22. Non-repeatable reads — the same row read twice in a transaction returning different values.

Q23. Why are LSM-trees preferred for write-heavy workloads over B-trees?
A23. Writes append sequentially to an in-memory memtable/WAL instead of updating in place, which is far faster than random B-tree writes, at the cost of read/compaction overhead.

Q24. What quorum condition guarantees a read sees the latest write?
A24. R + W > N — the read and write quorums are guaranteed to overlap in at least one node.

Q25. What's the difference between ACID "Consistency" and CAP "Consistency"?
A25. ACID consistency means the database satisfies its own declared constraints; CAP consistency means every node returns the same latest value for a read.

Q26. What is the N+1 query problem?
A26. Issuing one query for a list, then one extra query per row for related data, instead of a single join or batch fetch.

**Distributed Systems**

Q27. What does CAP actually force you to choose, in practice?
A27. Because partitions happen on real networks, the real choice is Consistency vs Availability during a partition, not "any two of three."

Q28. What is split-brain, and how is it prevented?
A28. Two nodes each believing they are the leader after a partition, both accepting writes; prevented by requiring a majority quorum to become or remain leader.

Q29. In Raft, what guarantees a committed log entry is never lost?
A29. The election restriction — a candidate can only win if its log is at least as up to date as a majority, so every future leader already carries all committed entries.

Q30. Why shouldn't distributed systems order events using wall-clock timestamps?
A30. Clocks drift and can jump across machines, so wall-clock order doesn't reliably reflect causality; use Lamport timestamps or vector clocks instead.

Q31. What happens to keys when a node is added to a consistent hashing ring?
A31. Only the keys between the new node and its counter-clockwise neighbor move — roughly K/N keys, not the whole keyspace.

Q32. Why is two-phase commit considered blocking?
A32. If the coordinator crashes after participants vote yes but before commit/abort is sent, participants must hold their locks and wait indefinitely.

**Linux, Containers and Kubernetes**

Q33. What two kernel mechanisms make a "container" possible?
A33. Namespaces (isolate what a process can see) and cgroups (limit what resources it can use).

Q34. What's the difference between a liveness probe failure and a readiness probe failure?
A34. Liveness failure restarts the container; readiness failure removes the Pod from Service endpoints without restarting it.

Q35. When should you choose a StatefulSet over a Deployment?
A35. When replicas need stable network identities and/or stable per-replica storage that survives rescheduling.

Q36. What's the practical difference between Pod `requests` and `limits`?
A36. `requests` drive scheduling decisions; `limits` are enforced at runtime — CPU is throttled past the limit, memory past the limit triggers an OOM kill.

Q37. Why is etcd central to Kubernetes' correctness?
A37. It's the single Raft-backed source of truth for cluster state, and the API server is the only component allowed to read/write it.

Q38. What does deleting a Pod managed by a Deployment actually accomplish?
A38. Very little — the ReplicaSet immediately creates a new Pod from the same, possibly still-broken, spec.

**Java and Concurrency**

Q39. Does `volatile` make `count++` atomic?
A39. No — it only guarantees visibility and ordering, not atomicity of a compound read-modify-write; use `AtomicInteger` or a lock.

Q40. Is `HashMap` thread-safe?
A40. No — concurrent structural modification is unsafe; use `ConcurrentHashMap`.

Q41. What must you always do after acquiring a `ReentrantLock`?
A41. Release it in a `finally` block so an exception doesn't leave the lock held forever.

Q42. What is the generational hypothesis behind Java's default heap layout?
A42. Most objects die young, so a frequently-collected young generation and a less-frequently-collected old generation are more efficient than one uniform heap.

Q43. Why might you choose ZGC or Shenandoah over G1?
A43. When you need very low, near-constant pause times on very large heaps; G1 balances throughput and pause time with somewhat higher worst-case pauses.

Q44. Why is `synchronized` reentrant?
A44. So the same thread can call another synchronized method on a monitor it already holds without deadlocking itself.

**System Design**

Q45. What's the first thing you should do in a system design interview?
A45. Clarify functional and non-functional requirements — expected scale, latency, and consistency needs — before proposing architecture.

Q46. Roughly how much faster is a main-memory access than an intra-datacenter round trip?
A46. About 1,000-5,000x — memory is roughly 100 ns, an intra-datacenter round trip is roughly 0.5 ms.

Q47. When should you reach for a message queue in a design?
A47. To decouple a fast producer from a slower consumer and absorb spikes, when at-least-once processing with idempotent consumers is acceptable.

Q48. Why is "just add a cache" an incomplete answer?
A48. Because it ignores invalidation strategy, staleness tolerance, and cache-stampede risk on expiry.

Q49. What's the real throughput ceiling of ten load-balanced app servers in front of one database writer?
A49. The single database writer — no amount of app-tier horizontal scaling raises the ceiling past what that one writer can sustain.

Q50. What should you always mention before declaring a design "done"?
A50. How you would monitor, alert on, and safely roll it out in production.

**Behavioral**

Q51. What does the "R" in STAR require that many candidates skip?
A51. A measurable or observable result, not just "we shipped it."

Q52. What's a strong answer to "tell me about a failure," and what should you avoid?
A52. Own your part honestly, explain what you learned and changed; avoid blaming teammates or management.

Q53. How many STAR stories should you prepare, and why not more?
A53. Roughly 6-8 flexible stories — enough to cover the common categories without over-rehearsing brittle, hyper-specific answers.

Q54. Why does asking questions at the end of a behavioral round matter?
A54. It is a genuine evaluation signal of curiosity and engagement, not a courtesy.

---

## Final Confidence Checklist

- [ ] I can state the invariant behind binary search, sliding window, and
      two pointers without hesitating.
- [ ] I can draw the TCP handshake, the Kubernetes architecture, and the JVM
      memory layout from a blank page.
- [ ] I can justify an isolation level and a CAP trade-off for a concrete
      scenario, not just recite definitions.
- [ ] I have 6-8 STAR stories that each answer more than one likely prompt.
- [ ] I know my logistics are ready: environment, backup internet, water,
      resume, notepad.
- [ ] I know that a calm, structured "I don't know, but here's how I'd find
      out" beats a guessed, confident wrong answer.

You have already done the studying. This guide is for retrieval, not
discovery — trust the preparation, think out loud, and let the interviewer
see how you reason, not just what you recall.
