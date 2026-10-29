# Part V: Distributed Systems

## Roadmap: Failures and Time

A single machine has one clock and one failure mode: it works, or it stops.
A distributed system has many machines, many clocks that all disagree by a
little, and a network between them that can delay, drop, duplicate, or
reorder any message at any moment. Almost every idea in this part is a
response to one of exactly two enemies: **failure** (a process, disk, or link
can stop or misbehave at any instant) and **time** (there is no shared "now,"
so nodes must agree on order and truth without a global clock). Keep those
two enemies in mind and the twenty chapters ahead stop looking like a random
list of buzzwords and start looking like one connected argument.

The argument runs in six movements. First, **limits**: CAP Theorem and PACELC
describe what is mathematically achievable when the network fails, and what
you pay in latency even when it does not. Second, **spreading data safely**:
Replication and Consistent Hashing describe how copies of data survive node
loss and how those copies are distributed without a full reshuffle every time
the cluster changes shape. Third, **agreeing under failure**: Leader
Election, Consensus, Raft, and Paxos describe how a group of unreliable
machines picks one coordinator or one agreed value even when some of them
crash mid-conversation. Fourth, **ordering without a clock**: Lamport Clocks
and Vector Clocks describe how to reason about "before" and "after" — and
"neither" — without synchronized time. Fifth, **the infrastructure that
embodies all of the above**: Kafka, Redis, ZooKeeper, and etcd are real,
production systems built directly on replication, consensus, and hashing.
Sixth, **defensive engineering for when failures happen anyway**:
Idempotency, Retry Mechanisms, Exponential Backoff, Circuit Breakers,
Distributed Caching, and Service Discovery are the everyday patterns that
keep a system usable while individual parts are unhealthy.

A recurring warning threads through this part: CAP is frequently taught as
"pick two of three, forever," and that framing is wrong and will cost you
credibility in an interview. Partition tolerance is not optional in a real
network, and the Consistency/Availability trade-off is a **per-operation,
per-moment** decision made only while a partition is actually happening — not
a permanent label stamped on an architecture. Every chapter below returns to
this theme: distributed systems guarantees are tunable, contextual, and
time-bound, not slogans.

## Chapters in This Part

1. [CAP Theorem](#cap-theorem)
2. [PACELC](#pacelc)
3. [Replication](#replication)
4. [Consistent Hashing](#consistent-hashing)
5. [Leader Election](#leader-election)
6. [Consensus](#consensus)
7. [Raft](#raft)
8. [Paxos](#paxos)
9. [Lamport Clock](#lamport-clock)
10. [Vector Clock](#vector-clock)
11. [Kafka](#kafka)
12. [Redis](#redis)
13. [ZooKeeper](#zookeeper)
14. [etcd](#etcd)
15. [Idempotency](#idempotency)
16. [Retry Mechanisms](#retry-mechanisms)
17. [Exponential Backoff](#exponential-backoff)
18. [Circuit Breakers](#circuit-breakers)
19. [Distributed Caching](#distributed-caching)
20. [Service Discovery](#service-discovery)

---


# CAP Theorem

## What is it?

CAP states that during a network **partition**, a distributed system
can offer at most one of **Consistency** (linearizable reads/writes) or
**Availability** (every request gets a non-error response) — not both.

## Why do we need it?

Engineers once promised systems that were always consistent, available,
and partition-immune. CAP names precisely which guarantee bends when the
network breaks.

## What problem does it solve?

Forces a concrete design question: when a partition splits the cluster,
what does each isolated half do with an incoming write or read?

## History (only if useful)

Eric Brewer proposed CAP in 2000; Gilbert and Lynch proved it formally in
2002. Brewer's 2012 follow-up corrected the "pick two forever" myth.

## How does it work?

When nodes are cut off from each other, a node must either refuse to
answer (lose A) or answer with possibly-stale data (lose C).

## Internal Working

Two replicas hold `balance = 100`. The link severs and a write
`balance = 80` arrives at replica 1:

```text
Client -W(80)-> [Replica 1]  X (cut) X  [Replica 2] <-R(balance?)- Client
```

Replica 1 can block (CP) or accept locally while replica 2 serves `100`
(AP) — values reconcile once healed.

## Step-by-Step Example

1. Partition begins; replicas 1 and 2 lose contact.
2. CP path: replica 1 refuses the write until a majority is reachable.
3. AP path: replica 1 accepts `80`; replica 2 still returns `100`.
4. Partition heals; values reconcile.

## Visualization

```text
        Consistency
            /\
           /CP\  <- during partition
          /----\
         / AP   \
        /________\
 Availability   Partition Tolerance (mandatory, not a 3rd choice)
```

## Real-World Usage

CP-leaning: ZooKeeper, etcd. AP-leaning: Cassandra, DynamoDB. DNS is
classic AP: a stale answer beats no answer.

## Advantages

A falsifiable model turning hidden trade-offs into an explicit design
decision instead of a production surprise.

## Disadvantages

Says nothing about latency outside a partition (see PACELC); its "C" is
often confused with ACID consistency.

## Trade-offs

The real trade-off is per-operation, not per-system: one service can be
CP for balance writes and AP for a "recently viewed" feature.

## Alternatives

PACELC extends CAP to latency/consistency absent a partition. Tunable
quorums (`W + R > N`) dial consistency per request.

## Performance Analysis

Quorum acknowledgment adds the latency of the slowest required replica;
accepting one reply minimizes latency but risks staleness.

## Complexity Analysis (if applicable)

An impossibility proof, not an algorithmic result. A CP write costs
O(majority-of-N) round trips; an AP write costs O(1) plus repair.

## Implementation

```java
boolean write(String key, String value) {
    int acked = replicas.writeToAll(key, value, 200); // ms timeout
    return acked >= w;
}
```

## Common Mistakes

Treating CAP as a permanent 2-of-3 label; assuming "CA" systems exist in
multi-node deployments.

## Common Interview Questions

Q: What does CAP theorem actually say?

Answer:
During a partition, a system must choose a consistent-but-possibly-
failing response or an available-but-possibly-stale one.

Q: Can a system be both consistent and available?

Answer:
Yes, whenever there's no partition, which is most of the time.

Q: Give an example of a CP and an AP system.

Answer:
ZooKeeper is CP, it halts rather than serve stale data. Cassandra
defaults to AP, staying available and reconciling later.

## Follow-up Interview Questions

Q: How does quorum replication relate to CAP?

Answer:
`W + R > N` lets a system move along the consistency/availability
spectrum per operation instead of one fixed label.

Q: Why might you pick AP for a shopping cart service?

Answer:
Refusing an "add to cart" during a network blip is worse than briefly
risking a stale cart, since divergent carts can be merged later.

## How the Interviewer Thinks

The interviewer listens for scope: does the candidate know CAP binds
only during a partition, and can they map operations to CP/AP?

## Production Engineering Perspective

Production systems rarely wear one label — a payments path might use
quorum writes while recommendations serve from an eventually consistent
cache.

## Common Misconceptions

"Pick exactly two of three, forever" is the most damaging misreading —
Brewer disavowed it.

## Revision Notes

- CAP applies only during a partition, not as a permanent label.
- The real choice is C vs. A while partitioned.
- Real systems tune consistency per operation via quorums.

## Summary

CAP is a precise statement about an unavoidable trade-off during network
partitions, not a lifetime sentence to pick two of three properties
forever.

# PACELC

## What is it?

PACELC extends CAP: **if Partitioned**, choose **Availability** or
**Consistency**; **Else** (normal operation), choose **Latency** or
**Consistency**.

## Why do we need it?

CAP only describes the rare partition window; systems spend nearly all
their time in normal operation, where a different trade-off applies.

## What problem does it solve?

Explains why even a healthy, unpartitioned system still chooses between
waiting for strong consistency and responding fast with stale data.

## History (only if useful)

Daniel Abadi proposed PACELC in 2010, arguing CAP alone doesn't explain
why systems like DynamoDB stay eventually consistent even when healthy.

## How does it work?

Synchronous replication to all replicas maximizes consistency at the
cost of latency; acknowledging after one replica minimizes latency at
the cost of possible staleness.

## Internal Working

`W=N` is PC/EC-leaning: slow but always consistent. `W=1` is PA/EL-
leaning: fast but only eventually consistent.

## Step-by-Step Example

1. Client writes; system requires all 3 replicas to ack (EC choice).
2. Replica 3 is on a slow link, adding 40ms to every write.
3. Switching to `W=2` cuts latency but consistency now depends on which
   2 replicas answer.
4. The system now trades consistency for latency, PACELC's "else"
   branch.

## Visualization

```text
Partition?  --yes--> A or C   (CAP)
     |
     no
     v
Latency or Consistency   (PACELC's "else")
```

## Real-World Usage

DynamoDB and Cassandra are PA/EL by default (tunable). Spanner leans
PC/EC, paying latency for consistency even absent a partition.

## Advantages

Explains real-world tuning knobs CAP alone can't, applying to the common
case, not just the rare partition.

## Disadvantages

Less famous and more nuanced than CAP, so it's often skipped in
discussion.

## Trade-offs

PC/EC systems pay latency always for consistency; PA/EL systems stay fast
but tolerate staleness — most production systems land somewhere tunable
in between.

## Alternatives

Tunable consistency levels (Cassandra's `QUORUM`, `ONE`) express both
ends of PACELC per query instead of one fixed point.

## Performance Analysis

Every additional synchronous acknowledgment adds roughly one network
round trip; the cost grows as replicas span wider geography.

## Complexity Analysis (if applicable)

Not algorithmic; EC costs O(replicas required) round trips per write, EL
costs O(1).

## Implementation

```java
WriteResult write(String key, String value, int w) {
    return replicas.write(key, value, w); // w=N: EC, w=1: EL
}
```

## Common Mistakes

Discussing only CAP and ignoring the latency/consistency trade-off during
normal operation.

## Common Interview Questions

Q: How is PACELC different from CAP?

Answer:
CAP only describes the partition window; PACELC adds that even without
one, systems trade latency for consistency.

Q: Where does DynamoDB sit on PACELC?

Answer:
PA/EL by default, with tunable stronger consistency available per
request at a latency cost.

Q: Why does geographic replica spread matter for PACELC?

Answer:
Wider spread increases the round-trip cost of synchronous
acknowledgment, making the EC choice more expensive.

## Follow-up Interview Questions

Q: Can a single system be PC/EC for one operation and PA/EL for another?

Answer:
Yes; a payments write may require majority acknowledgment while a
"likes" counter accepts eventual consistency for speed.

Q: Why do CP systems in CAP terms often also lean EC in PACELC terms?

Answer:
The same mechanism, synchronous majority acknowledgment, produces both
the partition-time consistency and the normal-time latency cost.

## How the Interviewer Thinks

The interviewer checks whether the candidate treats PACELC as a genuine
extension explaining everyday tuning, not just CAP trivia.

## Production Engineering Perspective

Teams tune write/read concern per operation type and monitor p99 write
latency against replica count as geography or SLAs change.

## Common Misconceptions

"PACELC only matters during partitions" is false, its "else" branch
covers normal operation, nearly all the time.

## Revision Notes

- PACELC = Partition: A or C; Else: Latency or Consistency.
- Explains tunable consistency knobs CAP alone doesn't.
- Latency cost of consistency scales with replica count and geography.

## Summary

PACELC completes the picture CAP starts: even when nothing is broken,
distributed systems trade latency against consistency, often shaping
performance more than the rare partition ever does.

# Replication

## What is it?

Replication keeps copies of the same data on multiple nodes so the
system survives node failure and can serve reads from more than one
place.

## Why do we need it?

A single copy of data is a single point of failure and a throughput
ceiling; replicas provide durability and horizontal read scale.

## What problem does it solve?

Data loss on node failure, and read-throughput limits of a single
machine.

## History (only if useful)

Early databases used primary-backup replication; multi-leader and
leaderless designs (Dynamo, 2007) emerged as the web scaled globally.

## How does it work?

**Leader-follower**: one node accepts writes, propagates to followers.
**Multi-leader**: several nodes accept writes, conflicts reconcile
later. **Leaderless**: any replica accepts writes; quorums (`W+R>N`)
determine consistency.

## Internal Working

Leader-follower replication can be synchronous (leader waits for
follower ack) or asynchronous (leader acks immediately, riskier on
failover).

## Step-by-Step Example

1. Client writes to the leader.
2. Leader appends to its log and forwards to followers.
3. Synchronous mode: leader waits for at least one follower ack.
4. Leader crashes; a follower with the latest log becomes new leader.

## Visualization

```text
Client -> [Leader] --async--> [Follower A]
                    --sync ---> [Follower B] (acked before client confirm)
```

## Real-World Usage

PostgreSQL streaming replication, Cassandra (leaderless, tunable
quorums), multi-region active-active setups (multi-leader).

## Advantages

Durability against node loss, horizontal read scaling, and (leaderless)
high write availability during partial failures.

## Disadvantages

Synchronous replication adds write latency; asynchronous risks data loss
on failover; multi-leader designs require conflict resolution.

## Trade-offs

More synchronous replicas increase durability but increase write
latency.

## Alternatives

Erasure coding (space-efficient durability without full copies);
single-node with fast backup/restore for tolerant workloads.

## Performance Analysis

Synchronous latency is bounded by the slowest acknowledging replica;
read throughput scales roughly linearly with follower count.

## Complexity Analysis (if applicable)

Write cost is O(replicas acknowledged); leaderless reads/writes cost
O(W) or O(R) round trips.

## Implementation

```java
boolean write(String key, String value) {
    leader.append(key, value);
    return followers.waitForAck(minAcks, timeoutMs);
}
```

## Common Mistakes

Assuming async replication never loses data; ignoring replication lag
when reading from followers; multi-leader without conflict resolution.

## Common Interview Questions

Q: What's the difference between synchronous and asynchronous replication?

Answer:
Synchronous waits for follower acknowledgment before confirming a write,
trading latency for safety; asynchronous confirms immediately.

Q: What is replication lag?

Answer:
The delay between a write committing on the leader and becoming visible
on a follower, which can cause stale reads.

Q: When would you choose leaderless replication?

Answer:
When write availability during partial node failure matters more than
strict ordering, since quorums reconcile consistency.

## Follow-up Interview Questions

Q: How can asynchronous replication lose data during failover?

Answer:
If the leader acknowledges a write and crashes before forwarding it, a
promoted follower becomes the new source of truth without that write.

Q: How does multi-leader replication resolve write conflicts?

Answer:
Common strategies include last-write-wins by timestamp, CRDTs, or
application-level merge logic.

## How the Interviewer Thinks

The interviewer wants the candidate to name the specific failure mode of
each replication style, not just its name.

## Production Engineering Perspective

Teams monitor replication lag as a first-class metric and rehearse
failover to catch unreplicated-write data loss before production does.

## Common Misconceptions

"Replication guarantees no data loss" is false, it depends entirely on
sync versus async configuration and failover handling.

## Revision Notes

- Leader-follower, multi-leader, leaderless are the three core models.
- Sync trades latency for safety; async trades safety for latency.
- Replication lag is the operational metric that matters most.

## Summary

Replication trades some combination of latency, complexity, or conflict
resolution for durability and read scale, the right model depends on
whether the workload prioritizes write safety or write availability.

# Consistent Hashing

## What is it?

Consistent hashing maps data keys and nodes onto the same conceptual
ring, so adding or removing a node only remaps a small fraction of keys.

## Why do we need it?

Plain modulo hashing (`hash(key) mod N`) remaps almost every key when N
changes, causing a rebalance storm on any scaling event.

## What problem does it solve?

Minimizes data movement when nodes join or leave a hash-based system.

## History (only if useful)

Introduced by Karger et al. (1997) for web caches; later core to Amazon
Dynamo (2007) and Cassandra's partitioning.

## How does it work?

Nodes and keys are hashed onto the same ring. A key belongs to the first
node found walking clockwise from its hash position.

## Internal Working

Virtual nodes (each physical node hashed to many ring positions) smooth
out the uneven distribution a few real nodes would otherwise produce.

## Step-by-Step Example

1. Nodes A, B, C are hashed onto ring positions.
2. Key `k` hashes between A and B, so it's owned by B.
3. Node D is added between A and B.
4. Only keys between A and D move; B and C's keys are untouched.

## Visualization

```text
        A
     /     \
   D         (new node, takes slice of A-B keys)
   |         |
   C         B
     \     /
     ring, clockwise ownership
```

## Real-World Usage

Amazon Dynamo and Cassandra for partitioning, CDN routing, memcached
client libraries for shard selection.

## Advantages

Only ~`K/N` keys move on a node change instead of nearly all keys;
predictable, incremental scaling.

## Disadvantages

Without virtual nodes, load can be uneven; still requires data migration
for the affected range.

## Trade-offs

More virtual nodes smooth load distribution but increase ring metadata
and lookup bookkeeping.

## Alternatives

Rendezvous hashing achieves similar remapping minimality differently;
static sharding is simpler but not adaptive.

## Performance Analysis

Ring lookup is O(log n); rebalancing moves only the affected key range.

## Complexity Analysis (if applicable)

O(log n) lookup with n ring positions; O(K/N) keys move per node
join/leave.

## Implementation

```java
TreeMap<Long, Node> ring = new TreeMap<>();
Node owner(String key) {
    long h = hash(key);
    Map.Entry<Long, Node> e = ring.ceilingEntry(h);
    return e != null ? e.getValue() : ring.firstEntry().getValue();
}
```

## Common Mistakes

Too few virtual nodes, causing load skew; forgetting to migrate data
during a rebalance.

## Common Interview Questions

Q: Why not just use `hash(key) mod N`?

Answer:
Changing N remaps nearly every key, forcing a full-dataset reshuffle
instead of a small, local one.

Q: What are virtual nodes for?

Answer:
They give each physical node multiple ring positions, smoothing out
uneven distribution.

Q: How many keys move when a node is added?

Answer:
Roughly `K/N` keys on average, the ones between the new node and its
ring predecessor.

## Follow-up Interview Questions

Q: How does consistent hashing interact with replication?

Answer:
A key replicates to the next few distinct nodes clockwise, so one node
loss still leaves replicas available.

Q: What's the trade-off of adding more virtual nodes?

Answer:
Better load balance at the cost of more ring metadata per lookup.

## How the Interviewer Thinks

The interviewer checks whether the candidate understands why modulo
hashing fails at scale and can explain virtual nodes as the fix.

## Production Engineering Perspective

Teams monitor per-node load distribution and tune virtual-node count
based on observed imbalance.

## Common Misconceptions

"Consistent hashing guarantees perfectly even load" is false without
enough virtual nodes.

## Revision Notes

- Solves the mass-remapping problem of modulo hashing.
- Virtual nodes fix uneven load from too few ring positions.
- Only ~K/N keys move per node change.

## Summary

Consistent hashing turns scaling a hash-partitioned system from a
full-dataset reshuffle into small, local data movement, with virtual
nodes fixing uneven load.

# Leader Election

## What is it?

Leader election is the process by which distributed nodes agree on a
single node to coordinate a task, avoiding duplicated or conflicting
work.

## Why do we need it?

Some responsibilities need exactly one active coordinator to avoid
conflicting decisions.

## What problem does it solve?

Prevents split-brain coordination, where two nodes both believe they're
in charge.

## History (only if useful)

The Bully algorithm (Garcia-Molina, 1982) was an early approach; modern
systems derive leadership from consensus (Raft, ZAB) or a coordination
service.

## How does it work?

Nodes exchange messages to agree on a leader, often one that wins a
lease/lock in a coordination service.

## Internal Working

A lease-based approach: a node acquires a time-bound lock; it must renew
before expiry or another node takes over, avoiding permanent split-brain.

## Step-by-Step Example

1. Nodes A, B, C each try to acquire the leader lock.
2. A wins and becomes leader; B and C watch for it to expire.
3. A renews its lease periodically while healthy.
4. A crashes; its lease expires; B acquires the lock and becomes leader.

## Visualization

```text
A (leader, renewing lease) --expires--> [lock free]
B, C (watching) -----------------------> B acquires -> B is leader
```

## Real-World Usage

Kafka controller election, Kubernetes controller-manager leader election
via Lease objects, database failover coordination.

## Advantages

Guarantees a single active coordinator and automates failover.

## Disadvantages

Brief leaderless gaps during failover; naive designs can produce
split-brain if lease expiry isn't handled carefully.

## Trade-offs

Shorter lease timeouts detect failure faster but risk unnecessary
failover during brief network jitter.

## Alternatives

Static/manual leader assignment (simple, no automatic failover); full
consensus-based leadership (Raft), which also replicates the decision
log.

## Performance Analysis

Election typically completes within one to a few lease-timeout periods;
frequent churn signals misconfigured timeouts.

## Complexity Analysis (if applicable)

Bully algorithm costs O(n²) messages worst case; lease-based election is
O(1) per node.

## Implementation

```java
boolean tryBecomeLeader() {
    return coordinationService.acquireLease("leader", nodeId, leaseTtl);
}
```

## Common Mistakes

Setting lease timeouts without accounting for GC pauses; assuming the
old leader knows it lost leadership immediately.

## Common Interview Questions

Q: What is split-brain and how does leader election prevent it?

Answer:
Two nodes both believing they're leader; time-bound leases with one
source of truth prevent two valid leases existing at once.

Q: Why use lease-based election instead of the Bully algorithm today?

Answer:
It delegates correctness to a proven coordination service and needs only
O(1) messages per node.

Q: What happens if a leader is merely slow, not dead?

Answer:
Its lease can still expire and a new leader elected, so the old leader
must stop acting once its lease is gone, a fencing problem.

## Follow-up Interview Questions

Q: What is fencing and why does leader election need it?

Answer:
Fencing blocks a former leader from acting after losing leadership,
typically via an increasing token, preventing state corruption.

Q: How does Raft's leader election differ from a standalone election service?

Answer:
Raft's election is intrinsic to reaching consensus on a replicated log,
so leadership and log agreement share the same mechanism.

## How the Interviewer Thinks

The interviewer checks whether the candidate knows fencing is required
for real safety, not just that "an election happens."

## Production Engineering Perspective

Teams tune lease timeouts against observed GC pause and jitter, and
monitor leader-change frequency as a health proxy.

## Common Misconceptions

"Once elected, a node is safely leader until it crashes" is false —
partitions can make it lose its lease while still running.

## Revision Notes

- Leases with expiry are the common modern mechanism.
- Fencing tokens prevent a stale leader from acting after losing
  leadership.
- Timeout tuning trades detection speed against false-positive churn.

## Summary

Leader election guarantees a single active coordinator through
time-bound leases, and real safety depends on fencing off the previous
leader, not just picking a new one.

# Consensus

## What is it?

Consensus is the general problem of getting distributed nodes to agree
on a single value or operation sequence, even if some nodes fail or
messages are delayed.

## Why do we need it?

Replicated systems need every node to apply the same operations in the
same order, which requires agreement despite failures.

## What problem does it solve?

Provides a foundation for replicated logs, leader election, and
distributed locking.

## History (only if useful)

Formalized via the FLP impossibility result (Fischer, Lynch, Paterson,
1985), showing pure asynchronous consensus can't guarantee termination;
Paxos (1989) and Raft (2014) work around this with timeouts.

## How does it work?

Nodes propose values; a protocol ensures only one value is chosen and
every node eventually learns it, typically via majority voting.

## Internal Working

Safety (never agreeing on two different values) always holds; liveness
(eventually agreeing) depends on timing assumptions, exactly what FLP
proves cannot be guaranteed in a fully asynchronous model.

## Step-by-Step Example

1. Node A proposes value `X`.
2. A majority of nodes acknowledge `X`.
3. `X` becomes the chosen value; no other value can be chosen afterward.
4. Nodes that missed the round learn `X` on the next message received.

## Visualization

```text
Propose(X) -> [N1, N2, N3] majority ack -> X chosen (durable, final)
```

## Real-World Usage

Distributed databases replicating a log (Spanner, CockroachDB), cluster
coordination (etcd, ZooKeeper), and leader election.

## Advantages

Provides provable safety even under node failure and message loss, the
bedrock other distributed guarantees build on.

## Disadvantages

Requires a majority of nodes reachable, costs multiple round trips per
decision, and correct implementations are hard to get right.

## Trade-offs

More participating nodes increase fault tolerance but also increase the
message and latency cost of every decision.

## Alternatives

Quorum-based eventual consistency avoids consensus's cost by accepting
reconciled conflicts instead of guaranteed agreement.

## Performance Analysis

Each decision costs at least one round trip to a majority; latency grows
with the slowest required acknowledgment.

## Complexity Analysis (if applicable)

O(n) messages per decision round in an n-node cluster; fault tolerance
is `floor((n-1)/2)` node failures.

## Implementation

```java
boolean propose(Value v) {
    int acks = nodes.broadcastAndCollectAcks(v, majorityTimeout);
    return acks > nodes.size() / 2;
}
```

## Common Mistakes

Assuming consensus guarantees fast termination; underestimating the
message cost of frequent small decisions.

## Common Interview Questions

Q: What does the FLP result actually prove?

Answer:
No fully asynchronous consensus protocol can guarantee both safety and
termination if even one node can fail, hence practical protocols add
timeouts.

Q: Why does consensus require a majority?

Answer:
A majority overlap between any two decision rounds guarantees one node
carries forward the previously chosen value, preventing conflicts.

Q: What's the difference between safety and liveness in consensus?

Answer:
Safety means nothing bad ever happens; liveness means something good
eventually happens, a value is chosen at all.

## Follow-up Interview Questions

Q: Why do practical consensus protocols use randomized timeouts?

Answer:
To avoid multiple nodes simultaneously trying to lead and repeatedly
splitting the vote, which would stall progress indefinitely.

Q: How does consensus relate to the CAP theorem?

Answer:
A consensus-based system is inherently CP: it sacrifices availability
during a partition since a minority side cannot safely progress.

## How the Interviewer Thinks

The interviewer wants to confirm the candidate understands consensus as
a formally hard problem with known impossibility results.

## Production Engineering Perspective

Teams size clusters with fault tolerance in mind (an odd number, typically
3 or 5), and monitor round latency to majority as an early signal of a
struggling node.

## Common Misconceptions

"Consensus means all nodes must respond" is false, only a majority is
required.

## Revision Notes

- FLP: no guaranteed termination in a fully asynchronous model with
  failures.
- Safety always holds; liveness depends on timing assumptions.
- Majority overlap prevents conflicting decisions.

## Summary

Consensus is the formally hard problem of agreeing on one value despite
failures, solved in practice by protocols like Paxos and Raft that trade
strict asynchronous guarantees for majority quorums and bounded
timeouts.

# Raft

## What is it?

Raft is a consensus algorithm (Ongaro & Ousterhout, 2014) designed to be
easier to understand than Paxos while providing the same safety
guarantees for replicating a log.

## Why do we need it?

Paxos is notoriously hard to implement correctly; Raft decomposes
consensus into separable sub-problems: leader election, log
replication, and safety.

## What problem does it solve?

Keeps a replicated log consistent across nodes despite crashes, so every
node applies the same commands in the same order.

## History (only if useful)

Published in 2014 as "In Search of an Understandable Consensus
Algorithm"; now powers etcd, Consul, CockroachDB, and Kafka's KRaft.

## How does it work?

One node is elected leader via randomized election timeouts. The leader
appends client commands to its log and replicates to followers; an entry
commits once a majority have it.

## Internal Working

Each entry carries a term number; a follower rejects `AppendEntries`
from a leader with a stale term, preventing an old leader from
overwriting a newer, committed log.

## Step-by-Step Example

1. Client sends a command to the leader.
2. Leader appends it locally and sends `AppendEntries` to followers.
3. A majority acknowledge; the leader commits and applies the entry.
4. The leader notifies followers of the new commit index on the next
   heartbeat.

## Visualization

```text
Leader: [1][2][3] --AppendEntries--> Follower A: [1][2][3]
                  --AppendEntries--> Follower B: [1][2]
Majority (Leader+A) has entry 3 -> committed
```

## Real-World Usage

etcd, Consul, CockroachDB, TiKV, and Kafka's KRaft controller quorum all
use Raft for their replicated logs.

## Advantages

Understandable, well-specified leader election and log replication with
strong safety proofs; widely implemented and battle-tested.

## Disadvantages

Write throughput bounded by the single leader; failover briefly pauses
writes until a new leader is elected.

## Trade-offs

Larger clusters tolerate more failures but slow down every commit, since
more acknowledgments are needed for a majority.

## Alternatives

Paxos (older, mathematically equivalent, harder to implement); Multi-
Paxos variants adding Raft-like leader stability.

## Performance Analysis

Commit latency is one round trip to a majority of followers; a 5-node
cluster tolerates 2 failures at the cost of 3 acknowledgments per write.

## Complexity Analysis (if applicable)

O(n) messages per log entry across an n-node cluster; tolerates
`floor((n-1)/2)` failures.

## Implementation

```java
boolean appendEntries(LogEntry entry) {
    log.append(entry);
    int acks = followers.replicate(entry, majorityTimeout);
    if (acks >= quorum) { commit(entry); applyToStateMachine(entry); }
    return acks >= quorum;
}
```

## Common Mistakes

Assuming any follower with the most recent term wins an election, it
must also have an up-to-date log; ignoring that writes pause during
failover.

## Common Interview Questions

Q: How does Raft differ from Paxos in approach?

Answer:
Raft splits consensus into understandable sub-problems, election,
replication, safety, while Paxos handles them more implicitly.

Q: What is a term in Raft?

Answer:
A monotonically increasing election period identifier; followers reject
messages from a leader with a stale term.

Q: When does a Raft log entry become committed?

Answer:
Once a majority of nodes have appended it to their logs, at which point
it's safe to apply and cannot be lost.

## Follow-up Interview Questions

Q: What happens to uncommitted entries after a leader crashes?

Answer:
A new leader is elected only from a node with an up-to-date log, and any
entries the old leader hadn't committed to a majority may be
overwritten.

Q: Why does Raft use randomized election timeouts?

Answer:
Randomization reduces the chance multiple followers time out
simultaneously and repeatedly split the vote.

## How the Interviewer Thinks

The interviewer checks whether the candidate can explain why Raft is
easier to reason about than Paxos, not just that it "is."

## Production Engineering Perspective

Operators size clusters (3 or 5 nodes) based on desired fault tolerance
and watch leader-election frequency as a health signal.

## Common Misconceptions

"Raft is a fundamentally different algorithm from Paxos" is an
oversimplification, it's mathematically similar but decomposed for
understandability.

## Revision Notes

- Decomposes consensus into election, replication, and safety.
- Terms prevent stale leaders from corrupting the log.
- Commit requires majority acknowledgment.

## Summary

Raft achieves the same safety guarantees as Paxos through a
deliberately more understandable decomposition, which is why it became
the default consensus choice for modern infrastructure.

# Paxos

## What is it?

Paxos is a family of consensus protocols (Lamport, 1989/1998) that lets
nodes agree on a single value despite failures, forming the theoretical
basis for most modern consensus systems.

## Why do we need it?

Before Paxos, ad hoc agreement protocols routinely had subtle
correctness bugs under failure; Paxos provides a proven-correct
foundation.

## What problem does it solve?

Guarantees a single agreed value is chosen even if proposers, acceptors,
or the network fail partway through.

## History (only if useful)

Lamport described it in 1989 (published 1998) as "The Part-Time
Parliament," whose obscure allegory delayed adoption until a simplified
2001 paper.

## How does it work?

Two phases: **Prepare** (a proposer asks acceptors to promise not to
accept older proposals) and **Accept** (asks acceptors to accept a
value); a value is chosen once a majority accepts it.

## Internal Working

Proposal numbers are unique and increasing; an acceptor promises to
reject any future proposal numbered lower than one it already promised,
preventing two values from both being chosen.

## Step-by-Step Example

1. Proposer sends `Prepare(n=5)` to all acceptors.
2. A majority promise not to accept proposals below 5.
3. Proposer sends `Accept(n=5, value=X)`.
4. A majority accept it; `X` is now the chosen value.

## Visualization

```text
Prepare(5) -> majority promise -> Accept(5, X) -> majority accept -> X chosen
```

## Real-World Usage

Google Chubby and Spanner (Paxos-based), and as the theoretical basis
referenced by nearly every modern consensus system's design.

## Advantages

Mathematically proven correct under asynchronous, failure-prone
conditions, the foundation nearly every practical consensus system
compares itself to.

## Disadvantages

The basic single-value protocol is notoriously difficult to implement
correctly and inefficient for a continuous stream of decisions without
Multi-Paxos.

## Trade-offs

Multi-Paxos adds a stable leader to avoid repeating Prepare for every
value, gaining efficiency at the cost of leader-failover complexity.

## Alternatives

Raft (same guarantees, more understandable decomposition); Zab
(ZooKeeper's purpose-built protocol).

## Performance Analysis

Basic Paxos costs two round trips per decision; Multi-Paxos amortizes
Prepare across many decisions once a stable leader exists, one round
trip per value.

## Complexity Analysis (if applicable)

O(n) messages per phase across an n-node cluster; tolerates
`floor((n-1)/2)` failures, identical to Raft.

## Implementation

```java
boolean propose(int n, Value v) {
    int promises = acceptors.prepare(n);
    if (promises < quorum) return false;
    int accepts = acceptors.accept(n, v);
    return accepts >= quorum;
}
```

## Common Mistakes

Implementing single-decree Paxos for a continuous log without the
Multi-Paxos optimization, causing needless round trips.

## Common Interview Questions

Q: What are the two phases of Paxos?

Answer:
Prepare, securing a promise from a majority not to accept older
proposals, and Accept, getting a majority to accept the value.

Q: Why do proposal numbers need to be unique and increasing?

Answer:
They let acceptors reject stale proposals, preventing two different
values from ever being chosen for the same decision.

Q: What is Multi-Paxos?

Answer:
An optimization electing a stable leader so Prepare runs once, not per
decision, making a continuous stream of decisions efficient.

## Follow-up Interview Questions

Q: Why is basic Paxos hard to use directly for log replication?

Answer:
Running two full phases per log entry is expensive, so real systems use
Multi-Paxos or a related leader-based variant.

Q: How does Paxos guarantee safety with only a majority participating?

Answer:
Any two majorities overlap by at least one node, and that node's prior
promise prevents the second majority from choosing a conflicting value.

## How the Interviewer Thinks

The interviewer wants to see the candidate understands why Paxos is
hard in practice, and that Multi-Paxos, not basic Paxos, runs in
production.

## Production Engineering Perspective

Teams rarely implement Paxos from scratch today; they adopt Raft for new
systems or trust a battle-tested Paxos-derived implementation.

## Common Misconceptions

"Basic single-decree Paxos is what you'd run in production" is false,
real systems run Multi-Paxos or a leader-based variant for efficiency.

## Revision Notes

- Two phases: Prepare (promise) and Accept (commit).
- Majority overlap guarantees safety.
- Multi-Paxos adds a stable leader for efficient repeated decisions.

## Summary

Paxos proved that consensus is achievable under asynchronous,
failure-prone conditions, and remains the theoretical ancestor of nearly
every practical consensus protocol in use today.

# Lamport Clock

## What is it?

A Lamport clock is a logical counter (Leslie Lamport, 1978) that gives
events in a distributed system a "happened-before" ordering without
synchronized physical clocks.

## Why do we need it?

Physical clocks across machines drift and can't be perfectly
synchronized, so ordering events by wall-clock timestamp alone is
unreliable.

## What problem does it solve?

Provides a consistent way to order causally related events across nodes
without shared clock hardware.

## History (only if useful)

Introduced in Lamport's 1978 paper "Time, Clocks, and the Ordering of
Events in a Distributed System," among the most cited in distributed
computing.

## How does it work?

Each node keeps a counter. On an internal event, it increments the
counter. On sending, it attaches the counter. On receiving, it sets its
counter to `max(local, received) + 1`.

## Internal Working

If event A happened-before event B, A's timestamp is guaranteed smaller
than B's — the converse isn't true: a smaller timestamp doesn't prove
happened-before for concurrent events.

## Step-by-Step Example

1. Node A: counter=1 (local event).
2. Node A sends a message with timestamp 1 to Node B.
3. Node B's counter was 0; on receipt it becomes `max(0,1)+1 = 2`.
4. Node B's next local event gets timestamp 3.

## Visualization

```text
A: 1 ---msg(1)---> B: max(0,1)+1=2 -> 3
```

## Real-World Usage

Version tracking in early distributed databases, event-ordering in
distributed tracing, a conceptual building block toward vector clocks.

## Advantages

Simple, small (one integer), sufficient whenever you only need to detect
that A definitely happened before B.

## Disadvantages

Cannot determine whether two events are truly concurrent or one caused
the other, since unrelated events can get any relative order.

## Trade-offs

Simplicity and low overhead versus limited expressiveness, the moment
you need true concurrency detection, you need a vector clock.

## Alternatives

Vector clocks (detect concurrency precisely, at higher cost); hybrid
logical clocks combining physical and logical time.

## Performance Analysis

O(1) update per event; a single integer per message, negligible network
overhead.

## Complexity Analysis (if applicable)

O(1) space per node and per message; comparing two timestamps is O(1).

## Implementation

```java
int clock = 0;
int tick() { return ++clock; }
int onReceive(int received) { clock = Math.max(clock, received) + 1; return clock; }
```

## Common Mistakes

Assuming a smaller Lamport timestamp always means "happened earlier"
causally, it only guarantees the reverse.

## Common Interview Questions

Q: What guarantee does a Lamport clock provide?

Answer:
If event A happened-before event B, A's timestamp is smaller than B's;
it doesn't guarantee the converse for concurrent events.

Q: How is the clock updated on message receipt?

Answer:
The receiver sets its counter to the max of its own and the received
timestamp, then increments by one.

Q: Why can't Lamport clocks detect concurrency?

Answer:
Concurrent, causally unrelated events still receive some definite
relative order, with no way to distinguish that from true causality.

## Follow-up Interview Questions

Q: How would you break ties when two events get the same timestamp?

Answer:
Combine the timestamp with a fixed node identifier as a tiebreaker,
giving a total order even when raw counters coincide.

Q: What's the practical difference between a Lamport clock and a vector clock?

Answer:
A Lamport clock costs one integer but only orders causally related
events; a vector clock costs one integer per node but can detect
concurrent, conflicting updates.

## How the Interviewer Thinks

The interviewer wants the candidate to distinguish "gives an ordering"
from "detects true causality," a common point of confusion.

## Production Engineering Perspective

Systems needing only ordering use Lamport or hybrid logical clocks for
low overhead; systems needing conflict detection move to vector clocks.

## Common Misconceptions

"A Lamport clock proves causality between any two events" is false, it
only proves it in one direction.

## Revision Notes

- Happened-before implies smaller timestamp, not the converse.
- Update rule: max(local, received) + 1.
- Insufficient for true concurrency detection; vector clocks fix this.

## Summary

Lamport clocks give distributed events a cheap, consistent partial
ordering using a single counter, but stop short of detecting real
concurrency, the gap that motivates vector clocks.

# Vector Clock

## What is it?

A vector clock is one counter per node that lets a distributed system
determine whether two events are causally related or truly concurrent,
unlike a single Lamport counter.

## Why do we need it?

Detecting genuine conflicts, two updates neither of which caused the
other, requires more information than a single logical timestamp
provides.

## What problem does it solve?

Lets systems distinguish "this update definitely came after that one"
from "these two updates conflict and need reconciling."

## History (only if useful)

Developed independently by Colin Fidge and Friedemann Mattern (1988),
building on Lamport's 1978 logical clock.

## How does it work?

Each node keeps a vector of counters, one per node. A node increments its
own entry on each event; on receipt, it takes the element-wise maximum
with the sender's vector, then increments its own entry.

## Internal Working

Vector `V1` happened-before `V2` if every entry of `V1` is `<=` the
corresponding entry of `V2` and at least one is strictly less; if
neither dominates, the events are concurrent.

## Step-by-Step Example

1. Node A: `[1,0,0]` after a local event.
2. Node A sends to Node B; B updates to `[1,1,0]`.
3. Meanwhile Node C independently reaches `[0,0,1]`.
4. B's `[1,1,0]` and C's `[0,0,1]` are concurrent, a real conflict.

## Visualization

```text
A: [1,0,0] --msg--> B: [1,1,0]
C: [0,0,1]  (concurrent with B: neither vector dominates)
```

## Real-World Usage

Amazon Dynamo's original conflict detection, Riak's sibling resolution,
distributed version-control-style merge detection.

## Advantages

Precisely detects true concurrency, telling an application exactly when
it must reconcile conflicting writes.

## Disadvantages

Vector size grows with node count, becoming costly in large or dynamic
clusters; conflict resolution is left to the application.

## Trade-offs

Precise conflict detection costs O(n) space per version versus a
Lamport clock's O(1).

## Alternatives

Dotted version vectors (reducing metadata bloat); CRDTs, which sidestep
manual conflict resolution with mathematically well-defined merges.

## Performance Analysis

Comparison is O(n) for an n-node vector; both cost grow with cluster
size, a real concern for churny, elastic clusters.

## Complexity Analysis (if applicable)

O(n) space per version and O(n) time per comparison.

## Implementation

```java
int[] clock = new int[n];
void localEvent() { clock[myId]++; }
void onReceive(int[] received) {
    for (int i = 0; i < n; i++) clock[i] = Math.max(clock[i], received[i]);
    clock[myId]++;
}
```

## Common Mistakes

Letting the vector grow unbounded as nodes churn; assuming the system
resolves conflicts automatically, vector clocks only detect them.

## Common Interview Questions

Q: How does a vector clock detect concurrency that a Lamport clock can't?

Answer:
By comparing entry-by-entry: if neither vector's entries all dominate
the other's, the events are provably concurrent.

Q: What does it mean if neither vector dominates the other?

Answer:
The events are concurrent, neither caused the other, and the
application must reconcile them.

Q: Why doesn't a vector clock scale well to very large clusters?

Answer:
Each vector needs one entry per node, so storage and comparison cost
grow linearly with cluster size.

## Follow-up Interview Questions

Q: How did Amazon Dynamo use vector clocks in practice?

Answer:
It attached a vector clock to each object version and returned all
concurrent versions to the client on a read, letting the application
reconcile them.

Q: What's a lighter-weight alternative when full vector clocks are too expensive?

Answer:
Dotted version vectors trim redundant entries while preserving the same
concurrency-detection guarantee.

## How the Interviewer Thinks

The interviewer wants the candidate to explain the dominance comparison
precisely and know vector clocks detect, but don't resolve, conflicts.

## Production Engineering Perspective

Systems using vector clocks pair them with an explicit conflict-
resolution policy, since detection alone doesn't produce a final value.

## Common Misconceptions

"Vector clocks automatically resolve conflicts" is false, they only
detect that a conflict exists.

## Revision Notes

- Compare vectors entry-by-entry to detect concurrency.
- Cost is O(n) per version, unlike a Lamport clock's O(1).
- Detection is not resolution; that's a separate application step.

## Summary

Vector clocks give distributed systems the ability to prove two events
are truly concurrent rather than causally ordered, at the cost of
per-node metadata, closing the gap Lamport clocks leave open.

# Kafka

## What is it?

Kafka is a distributed, partitioned, replicated commit-log platform
(LinkedIn, 2011) for high-throughput event streaming between services.

## Why do we need it?

Point-to-point messaging doesn't scale to many producers/consumers or
retain history; Kafka provides an ordered, replayable, scalable log.

## What problem does it solve?

Decouples producers from consumers, absorbs bursty load, and lets
multiple independent consumers replay the same event stream.

## History (only if useful)

Built at LinkedIn in 2011; later dropped its ZooKeeper dependency for
built-in Raft-based KRaft consensus (2022+).

## How does it work?

Topics split into partitions, each an ordered log replicated across
brokers. Producers write by key or round robin; consumers track their
own offset and can replay from any point.

## Internal Working

Each partition has one leader broker and follower replicas; a message
is "committed" once replicated to all in-sync replicas, controlled via
`acks`.

## Step-by-Step Example

1. Producer sends a message keyed by `user_id` to a topic.
2. Kafka routes it to the partition determined by the key's hash.
3. The partition leader appends it and replicates to followers.
4. Consumers in a group each read sequentially from their own offset.

## Visualization

```text
Producer -> Partition 0: [m1][m2][m3] -> Consumer A (offset 2)
         -> Partition 1: [m4][m5]     -> Consumer B (offset 1)
```

## Real-World Usage

Event sourcing, log aggregation, real-time analytics, backbone connecting
microservices asynchronously.

## Advantages

Extremely high throughput, ordered log per partition, replay
capability, decoupled multi-consumer fan-out.

## Disadvantages

Only ordered within a partition, not across the topic; operating a
cluster requires real expertise around partitioning and retention.

## Trade-offs

More partitions increase parallelism but weaken ordering guarantees;
`acks=all` maximizes durability at the cost of write latency.

## Alternatives

RabbitMQ (traditional queue semantics); Pulsar (similar log model);
cloud-managed equivalents like Kinesis.

## Performance Analysis

Brokers commonly sustain hundreds of thousands of messages per second
per partition; end-to-end latency is single-digit milliseconds under
`acks=1`.

## Complexity Analysis (if applicable)

O(1) append and sequential read per partition; rebalancing cost is
O(consumers × partitions).

## Implementation

```java
producer.send(new ProducerRecord<>("orders", userId, orderJson));
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
```

## Common Mistakes

Assuming global ordering across a whole topic; too few partitions,
capping consumer parallelism.

## Common Interview Questions

Q: Does Kafka guarantee message ordering?

Answer:
Only within a single partition, messages across different partitions
have no guaranteed relative order.

Q: What does `acks=all` mean for a producer?

Answer:
The producer waits until all in-sync replicas have the message,
maximizing durability at the cost of latency.

Q: How does Kafka enable multiple independent consumers of the same stream?

Answer:
Each consumer group tracks its own offset independently, so multiple
groups can replay the same log at their own pace.

## Follow-up Interview Questions

Q: How would you guarantee ordering for all messages about one user?

Answer:
Key messages by user ID so partitioning always routes that user's
messages to the same partition.

Q: Why did Kafka move away from depending on ZooKeeper?

Answer:
To remove an external coordination dependency by embedding an
equivalent Raft-based consensus (KRaft) directly into the brokers.

## How the Interviewer Thinks

The interviewer checks whether the candidate knows ordering is
per-partition, not global, a common point of confusion.

## Production Engineering Perspective

Teams monitor consumer lag and under-replicated partitions, and size
partition counts based on target parallelism up front.

## Common Misconceptions

"Kafka guarantees exactly-once delivery by default" is false without
explicitly enabling idempotent producers and transactions.

## Revision Notes

- Ordering is per-partition only.
- `acks` setting controls the durability/latency trade-off.
- Consumer groups enable independent, replayable consumption.

## Summary

Kafka turns a stream of events into a durable, replayable, partitioned
log, decoupling producers from consumers at high throughput, at the cost
of ordering being scoped to a partition.

# Redis

## What is it?

Redis is an in-memory data structure store (Salvatore Sanfilippo, 2009)
used as a cache, database, or message broker, supporting strings, hashes,
lists, sets, sorted sets, and streams.

## Why do we need it?

Applications need sub-millisecond access to hot data and richer atomic
operations than plain get/set, without hitting the primary database
every time.

## What problem does it solve?

Reduces primary-database load and latency, and provides fast, shared,
atomic primitives across application instances.

## History (only if useful)

First released in 2009; Redis Cluster (2015) added horizontal sharding.
A 2024 licensing change prompted the Linux Foundation-backed Valkey fork.

## How does it work?

A single-threaded event loop per shard executes commands atomically.
Persistence is optional via RDB snapshots and/or an append-only file.
Redis Cluster shards keys across 16384 hash slots.

## Internal Working

Replication is asynchronous by default, so a failover can lose the most
recent unreplicated writes. Eviction under `maxmemory` uses approximate
LRU or LFU.

## Step-by-Step Example

1. Request checks Redis first (cache-aside).
2. Miss: application reads from the database.
3. Application writes the result into Redis with a TTL.
4. Later requests are served from Redis until expiry or invalidation.

## Visualization

```text
Client -> GET key -> [Redis] --miss--> [DB] -> SET key(TTL) -> [Redis]
Client -> GET key -> [Redis] --hit --> return cached value
```

## Real-World Usage

Session stores, leaderboards (sorted sets), rate limiting
(`INCR`+`EXPIRE`), pub/sub notifications, full-page or query-result
caching.

## Advantages

Extremely low latency, rich atomic data structures with scripting, a
simple operational model at moderate scale.

## Disadvantages

Memory-bound and costly at large scale; asynchronous replication can lose
very recent writes on failover.

## Trade-offs

Enabling AOF durability improves crash safety but costs write throughput
compared to pure in-memory operation.

## Alternatives

Memcached (simpler, pure cache); Hazelcast/Apache Ignite (in-memory grids
with compute); DynamoDB Accelerator (DAX).

## Performance Analysis

Sub-millisecond operations with per-node throughput in the hundreds of
thousands of ops/sec; the network hop often dominates observed latency.

## Complexity Analysis (if applicable)

O(1) for GET/SET, O(log n) for sorted-set operations, O(n) for some
list/set operations.

## Implementation

```java
String cached = redis.get(key);
if (cached == null) {
    cached = db.load(key);
    redis.setex(key, 300, cached);
}
```

## Common Mistakes

Treating Redis as a system of record without tuning durability; storing
oversized values or hot keys on one shard; skipping TTLs.

## Common Interview Questions

Q: Why is Redis so fast?

Answer:
It keeps data in memory and processes commands on a single-threaded event
loop, avoiding locking overhead and disk latency for the common case.

Q: Is Redis durable by default?

Answer:
Not strongly — durability requires explicitly tuning AOF fsync policy or
accepting the data-loss window of async replication.

Q: How does Redis Cluster distribute data?

Answer:
Keys hash into one of 16384 fixed hash slots, each assigned to a
specific node.

## Follow-up Interview Questions

Q: What is a cache stampede and how does Redis usage relate to it?

Answer:
It's when many concurrent misses for the same hot key all hit the
database simultaneously; request coalescing or a short mutex during
recomputation mitigates it.

Q: Why is Redlock controversial?

Answer:
Critics argue it can behave unsafely under clock skew or process pauses
like long GC, so it shouldn't be relied on as a strict fencing mechanism.

## How the Interviewer Thinks

The interviewer wants to know whether the candidate understands Redis's
speed comes from specific design choices with real trade-offs, not that
it's "just always fast and safe."

## Production Engineering Perspective

Teams monitor keyspace hit ratio, eviction rate, and replication lag; hot
keys are mitigated via client-side caching or key splitting.

## Common Misconceptions

"Redis persistence makes it as durable as a database by default" is
false without explicit tuning.

## Revision Notes

- Single-threaded event loop; async replication by default.
- Cluster mode shards via 16384 hash slots.
- Durability requires explicit AOF/fsync tuning.

## Summary

Redis delivers speed and rich data structures by keeping data in memory
and processing commands atomically on a single thread, fast, but
requiring deliberate configuration to be durable at scale.

# ZooKeeper

## What is it?

ZooKeeper is a centralized coordination service (Yahoo, 2007) offering a
hierarchical namespace of nodes ("znodes") with strong consistency for
configuration, naming, locks, and leader election, built on ZAB.

## Why do we need it?

Distributed applications kept reimplementing fragile coordination
primitives from scratch; ZooKeeper centralizes correct implementations
behind a simple filesystem-like API.

## What problem does it solve?

Provides a reliable, strongly consistent metadata layer so applications
don't need to build their own consensus for locks or election.

## History (only if useful)

Built at Yahoo around 2007 to coordinate Hadoop-ecosystem projects;
became an Apache top-level project in 2011.

## How does it work?

An odd-sized ensemble elects one leader; ZAB provides atomic broadcast —
a write commits once a majority acknowledges. Reads are served by any
node and may be slightly stale unless a `sync()` is requested first.

## Internal Working

Znodes can be persistent, ephemeral (deleted when the session ends), or
sequential (suffixed with an increasing number). Clients set one-time
watches that must be re-registered to keep observing.

## Step-by-Step Example

1. Clients wanting a lock create an ephemeral sequential znode under
   `/lock`.
2. The client with the lowest sequence number holds the lock.
3. Every other client watches only its immediate predecessor.
4. When the lock holder's session ends, the next-lowest client wakes up.

## Visualization

```text
/lock/n_000001  <- holds the lock
/lock/n_000002  <- watches n_000001 only
/lock/n_000003  <- watches n_000002 only
```

## Real-World Usage

Historically Kafka's broker metadata, HBase master election, and Apache
Curator's recipes for locks, barriers, and election.

## Advantages

Strong consistency (CP), well-tested coordination primitives, reactive
notifications via watches.

## Disadvantages

Not for bulk data (znodes capped around 1MB); write throughput limited by
the single leader plus majority ack; watches are one-shot.

## Trade-offs

Strong consistency and mature primitives cost an extra operational
service and a write-throughput ceiling.

## Alternatives

etcd (similar niche, Raft-based, Kubernetes' default); Consul (adds
service discovery); Google Chubby (the Paxos-based precursor).

## Performance Analysis

Read-heavy workloads scale well since any follower can serve reads;
writes are bounded by leader-plus-majority round trips.

## Complexity Analysis (if applicable)

Writes cost O(n) broadcast messages across an n-node ensemble; each
active watch is O(1) to register.

## Implementation

```java
CuratorFramework client = CuratorFrameworkFactory.newClient(zkHosts, retry);
LeaderLatch latch = new LeaderLatch(client, "/election", nodeId);
```

## Common Mistakes

Storing large payloads in znodes; forgetting to re-register one-shot
watches; sizing ensembles with an even number of nodes.

## Common Interview Questions

Q: What consistency model does ZooKeeper provide?

Answer:
Strong consistency for writes via ZAB majority commit, with reads served
locally, possibly stale unless the client requests `sync()` first.

Q: How does the ephemeral-sequential lock recipe avoid a thundering herd?

Answer:
Each waiting client watches only its immediate predecessor's znode, so a
release wakes exactly one client instead of every waiter.

Q: Why must ensembles be sized as odd numbers?

Answer:
Fault tolerance depends on majority quorums, and an even-sized ensemble
adds a node without increasing tolerated failures.

## Follow-up Interview Questions

Q: Why did some systems move away from ZooKeeper?

Answer:
To remove an external coordination dependency once they could embed an
equivalent consensus protocol directly, as Kafka did with KRaft.

Q: What causes a false session expiry in ZooKeeper?

Answer:
A JVM garbage-collection pause or disk stall delaying heartbeats past the
session timeout, dropping the session and its ephemeral znodes.

## How the Interviewer Thinks

The interviewer checks whether the candidate knows ZooKeeper's guarantees
and practical operational risks, not just that "it does locks."

## Production Engineering Perspective

Operators give ZooKeeper dedicated fast disks for its transaction log and
tune JVM GC carefully to avoid spurious failovers.

## Common Misconceptions

"ZooKeeper is a general-purpose database" is false — it's a coordination
service, not for bulk data.

## Revision Notes

- Ephemeral + sequential znodes are the building block for locks and
  election.
- ZAB requires majority commit; reads can be stale without `sync()`.
- Not for bulk data; watches are one-shot.

## Summary

ZooKeeper packaged the hard, easy-to-get-wrong parts of distributed
coordination behind a simple API backed by a proven consensus protocol,
at the cost of an extra operational dependency.

# etcd

## What is it?

etcd is a distributed, strongly consistent key-value store (CoreOS, 2013)
built on Raft, exposing a gRPC/HTTP API with watch support, serving as
Kubernetes' primary metadata store.

## Why do we need it?

Kubernetes needs a reliable, strongly consistent, watchable store for
cluster state with a simpler API than older alternatives.

## What problem does it solve?

Provides one source of truth for cluster configuration, with watch-based
change notification and leader-based coordination.

## History (only if useful)

Created at CoreOS in 2013; adopted by Kubernetes soon after and later
graduated as a CNCF project.

## How does it work?

Every write is Raft-replicated to a majority before commit. etcd uses
MVCC, keeping historical revisions so watches can resume from any past
revision. Leases provide TTL-based key expiry.

## Internal Working

Reads default to linearizable (routed through the leader) or opt into
faster, possibly stale serializable reads. Compaction is required
periodically to bound MVCC history growth.

## Step-by-Step Example

1. The Kubernetes API server writes a new Pod spec to etcd.
2. etcd assigns it a new revision after Raft-committing the write.
3. The scheduler's watch stream receives the change event.
4. The scheduler acts and writes its decision back, generating a new
   revision.

## Visualization

```text
[etcd Raft cluster] N1(leader) -> N2, N3 (majority replication)
        gRPC watch stream -> Scheduler, Kubelet, Controller-manager
```

## Real-World Usage

The sole backing store for the Kubernetes control plane, and the Lease
API underlying Kubernetes' own leader-election mechanism.

## Advantages

Strong consistency, a simple modern API, efficient watch-based change
propagation, tight Kubernetes integration.

## Disadvantages

Write throughput capped by a single Raft leader and disk-fsync latency;
recommended storage size is modest.

## Trade-offs

Strong consistency and API simplicity cost write scalability — etcd is
typically scaled vertically, not sharded.

## Alternatives

ZooKeeper (older, heavier API); Consul (adds service mesh); TiKV/
CockroachDB (horizontally sharded alternatives).

## Performance Analysis

Single-digit-millisecond write latency on good SSDs; performance degrades
sharply on slow disks since Raft commits require majority fsync.

## Complexity Analysis (if applicable)

Writes cost O(n) Raft messages across the cluster; storage grows with
revision history until compacted.

## Implementation

```java
client.getKVClient().put(bytesOf("/pods/x"), bytesOf(podSpecJson)).get();
client.getWatchClient().watch(bytesOf("/pods/"), resp -> handle(resp));
```

## Common Mistakes

Storing large blobs or high-churn data; neglecting compaction and
defragmentation; running etcd on slow or networked disks.

## Common Interview Questions

Q: Why does Kubernetes use etcd?

Answer:
Because it needs a strongly consistent, watchable store for cluster
state, and etcd's Raft-backed consistency plus watch API fit that need
directly.

Q: What is MVCC used for in etcd?

Answer:
It keeps historical revisions of each key so watches can resume from any
past revision and transactions can perform safe compare-and-swap.

Q: Can etcd scale horizontally like a sharded database?

Answer:
Not natively — a single etcd cluster is one Raft group covering the whole
keyspace, so scaling beyond its ceiling requires separate clusters.

## Follow-up Interview Questions

Q: Why is etcd so sensitive to disk latency?

Answer:
Every Raft commit requires a majority of nodes to fsync the write before
acknowledging, so slow disks directly inflate write latency.

Q: What operational practice prevents etcd storage bloat?

Answer:
Scheduling regular compaction of old MVCC revisions and periodic
defragmentation to reclaim freed disk space.

## How the Interviewer Thinks

The interviewer wants confirmation the candidate understands etcd's role
as a consistency-optimized store, not a general database.

## Production Engineering Perspective

Teams track etcd disk-latency SLOs, schedule compaction, and monitor
leader changes as an early-warning signal for control-plane health.

## Common Misconceptions

"etcd is just like Redis but consistent" is false — it optimizes for
consistency and correct watches, not raw cache throughput.

## Revision Notes

- Raft-backed, MVCC key-value store with gRPC watch API.
- Linearizable reads need a leader check; serializable reads are faster
  but can be stale.
- Disk latency directly bounds write throughput.

## Summary

etcd gives Kubernetes a strongly consistent, watchable source of truth
built on Raft, trading horizontal write scalability for consistency, a
deliberate choice for cluster metadata over bulk data.

# Idempotency

## What is it?

Idempotency is the property that performing an operation multiple times
has the same effect as performing it once, so a client can safely retry.

## Why do we need it?

Networks are unreliable: a client may not know whether a timed-out
request succeeded, and retrying is only safe if repeating it cannot
double its effect.

## What problem does it solve?

Prevents duplicate side effects, double charges, duplicate orders,
caused by retries.

## History (only if useful)

HTTP defines GET/PUT/DELETE as idempotent by contract (RFC 7231);
Stripe's "Idempotency-Key" header popularized the client-generated-key
pattern.

## How does it work?

Some operations are naturally idempotent (`SET x=5`). Others use a
client-generated idempotency key: the server stores key → result on
first execution, and a duplicate returns the stored result.

## Internal Working

The server needs an atomic check-and-store on the key so two concurrent
duplicates can't both "miss" and both execute.

## Step-by-Step Example

1. Client generates a UUID key and calls `POST /charge` with it.
2. The network times out before the response arrives.
3. Client retries with the same key.
4. Server recognizes the key already has a result and returns it without
   charging twice.

## Visualization

```text
Client --POST(key=K)--> [Server] charges once, stores K->result
Client --POST(key=K) retry--> [Server] looks up K, returns cached result
```

## Real-World Usage

Payment gateways, order/inventory APIs, message-consumer deduplication
by message ID.

## Advantages

Enables safe retries, simplifies client error handling.

## Disadvantages

Adds storage and lookup overhead; requires careful handling of
concurrent duplicate requests.

## Trade-offs

A longer key-retention window is safer against slow retries but costs
more storage.

## Alternatives

Exactly-once processing frameworks (Kafka transactions) solve a heavier
version of the same problem; naturally idempotent API design avoids
needing keys.

## Performance Analysis

Adds one cache/DB lookup per request, negligible next to the retry cost
it prevents.

## Complexity Analysis (if applicable)

O(1) key lookup; storage cost is O(active keys × retention window).

## Implementation

```java
Optional<Response> existing = store.find(idempotencyKey);
if (existing.isPresent()) return existing.get();
Response result = processCharge(request);
store.saveIfAbsent(idempotencyKey, result);
```

## Common Mistakes

Using a hash of the request body instead of a unique client key; never
expiring records.

## Common Interview Questions

Q: Is a GET request always idempotent in practice?

Answer:
It's idempotent by contract, but a poorly implemented endpoint can still
violate that contract.

Q: How do you make a non-idempotent operation idempotent?

Answer:
Require a unique client-generated key and store the result keyed by it,
returning the cached result on any duplicate.

Q: What race condition must idempotency-key storage guard against?

Answer:
Two near-simultaneous duplicates both missing the cache and both
executing; an atomic insert prevents this.

## Follow-up Interview Questions

Q: Does idempotency solve message-ordering problems?

Answer:
No, it only prevents duplicate execution; ordering needs a separate
mechanism like sequence numbers.

Q: How long should idempotency keys typically be retained?

Answer:
Long enough to cover realistic retry windows; Stripe's API uses roughly
24 hours.

## How the Interviewer Thinks

The interviewer wants to see whether the candidate distinguishes
naturally idempotent operations from ones requiring an explicit key.

## Production Engineering Perspective

Teams pair idempotency keys with at-least-once consumers via a dedup
table keyed by message ID.

## Common Misconceptions

"Idempotent means safe to call" in a security sense is false, it's
purely about side-effect equivalence, not authorization.

## Revision Notes

- Idempotency keys make retries safe; body-hash alone is not enough.
- Guard against the concurrent-duplicate race with atomic inserts.
- Idempotency prevents duplication, not reordering.

## Summary

Idempotency is what makes retries safe in the first place, without it,
resending a request risks doing real damage twice.

# Retry Mechanisms

## What is it?

A retry mechanism re-attempts a failed operation automatically, on the
assumption that many distributed failures are temporary.

## Why do we need it?

Distributed calls fail transiently far more often than local calls;
surfacing every blip as a user-facing error is worse than absorbing it
with bounded automatic retries.

## What problem does it solve?

Converts transient failures into slightly higher latency instead of a
hard error.

## History (only if useful)

Retry logic is as old as networking; modern client libraries
standardized configurable retry policies with backoff and jitter by the
mid-2010s.

## How does it work?

The client catches a retryable error, waits (usually with backoff), and
re-issues the request up to a max attempt count, only for errors
classified as safely retryable and idempotent-safe.

## Internal Working

Retry policies combine a max-attempts cap, a backoff strategy, jitter,
and a classifier deciding which errors are retryable, retrying a 400
Bad Request only wastes time.

## Step-by-Step Example

1. Client calls a payment service; the call times out.
2. Client checks: is this idempotent and is a timeout retryable? Yes.
3. Client waits per backoff policy and retries.
4. Second attempt succeeds; the response is returned.

## Visualization

```text
Attempt 1 --timeout--> wait 100ms
Attempt 2 --timeout--> wait 200ms
Attempt 3 --success--> return result
```

## Real-World Usage

HTTP clients retrying 5xx/connection errors, message consumers
redelivering unacknowledged messages, database drivers retrying
deadlock errors.

## Advantages

Absorbs transient failures automatically, improves perceived reliability
without server-side changes.

## Disadvantages

Naive retries can amplify load during real outages, and retrying
non-idempotent operations risks duplicate side effects.

## Trade-offs

More attempts improve success odds but increase tail latency and
downstream load.

## Alternatives

Failing fast and surfacing the error immediately; circuit breakers,
which stop retrying once a dependency is clearly unhealthy.

## Performance Analysis

A well-tuned policy adds negligible average latency but meaningfully
raises tail latency for the retried minority.

## Complexity Analysis (if applicable)

O(k) additional calls for k retries in the worst case.

## Implementation

```java
for (int attempt = 1; attempt <= maxAttempts; attempt++) {
    try { return call(); }
    catch (RetryableException e) {
        if (attempt == maxAttempts) throw e;
        sleep(backoff(attempt));
    }
}
```

## Common Mistakes

Retrying non-idempotent calls with no dedup key; omitting backoff/
jitter, causing synchronized retry storms.

## Common Interview Questions

Q: What errors should you retry?

Answer:
Only errors classified as transient, like timeouts and 503s, never
validation errors.

Q: What is a retry storm?

Answer:
When many clients retry a failing dependency simultaneously, the added
load deepens the outage.

Q: How do retries interact with idempotency?

Answer:
Retries are only safe when the operation is idempotent or protected by a
deduplication key.

## Follow-up Interview Questions

Q: How should retries be capped in a call chain with multiple hops?

Answer:
A shared retry budget or "retry once, then propagate the failure" policy
prevents multiplicative retry explosion.

Q: Why is jitter important in a retry policy?

Answer:
Without it, clients that failed at the same moment retry at the same
moment again, creating synchronized load spikes.

## How the Interviewer Thinks

The interviewer checks whether the candidate treats retries as a policy
with real risks rather than a reflexive "just try again" fix.

## Production Engineering Perspective

Teams enforce a shared retry budget per request chain and pair retries
with circuit breakers.

## Common Misconceptions

"More retries are always safer" is false, unbounded retries are a common
self-inflicted cause of cascading outages.

## Revision Notes

- Only retry transient, idempotent-safe errors.
- Always pair retries with backoff and jitter.
- Retry storms can turn partial outages into total ones.

## Summary

Retries convert many transient failures into slightly higher latency,
but only when scoped to safe errors and paired with backoff.

# Exponential Backoff

## What is it?

Exponential backoff is a retry-delay strategy where each successive
retry waits exponentially longer than the last (e.g., 100ms, 200ms,
400ms), typically combined with random jitter.

## Why do we need it?

Fixed-delay retries from many clients synchronize into repeating load
spikes against a struggling dependency; a growing, randomized delay
spreads retries out.

## What problem does it solve?

Reduces the odds that many retrying clients overwhelm a recovering
service at the same instant, while still recovering quickly from brief
failures.

## History (only if useful)

Rooted in Ethernet's binary exponential backoff (1970s); AWS's
"Exponential Backoff and Jitter" post (2015) popularized jitter as a
near-mandatory addition.

## How does it work?

Delay for attempt *n* is typically `min(cap, base * 2^n)`, then jitter is
applied — "full jitter" picks a uniformly random delay between 0 and
that capped value.

## Internal Working

A capped ceiling prevents unbounded delay growth. Jitter type matters:
no jitter causes synchronized retry waves, while full jitter spreads
them into a smooth distribution.

## Step-by-Step Example

1. Attempt 1 fails; wait ~100ms (with jitter).
2. Attempt 2 fails; wait ~200ms (with jitter).
3. Attempt 3 fails; wait ~400ms (with jitter).
4. Attempt 4 succeeds.

## Visualization

```text
Attempt: 1    2    3    4
Delay:  100  200  400  800  (ms, before jitter and cap)
```

## Real-World Usage

Cloud SDKs, gRPC's built-in retry policy, mobile app network clients
recovering from spotty connectivity.

## Advantages

Reduces synchronized load spikes, adapts naturally to short and longer
outages without per-failure-type configuration.

## Disadvantages

Adds latency proportional to attempt number; a high cap can make an
already-slow failure feel very slow before it finally gives up.

## Trade-offs

A higher backoff cap tolerates longer outages gracefully but delays final
failure reporting.

## Alternatives

Fixed-delay retry (simpler, prone to synchronized storms); no retry plus
immediate circuit breaking for tight latency budgets.

## Performance Analysis

Worst-case wait across k attempts is bounded by `k * cap`; full jitter
typically yields faster aggregate recovery than fixed-delay retries under
load.

## Complexity Analysis (if applicable)

O(1) to compute each delay; no additional overhead beyond a counter and
a random draw.

## Implementation

```java
long delay = Math.min(cap, base * (1L << attempt));
long jittered = ThreadLocalRandom.current().nextLong(delay + 1);
sleep(jittered);
```

## Common Mistakes

Omitting jitter entirely; forgetting a cap; applying backoff without
also capping total attempts.

## Common Interview Questions

Q: Why exponential rather than linear backoff?

Answer:
Exponential growth backs off quickly enough to relieve real overload
within a few attempts, while linear growth stays aggressive for too many
retries.

Q: What is "full jitter" and why does it matter?

Answer:
It picks a uniformly random delay between zero and the capped exponential
value, and AWS found it reduces total completion time under contention
better than no-jitter variants.

Q: What happens without a cap on the backoff delay?

Answer:
Delays keep doubling indefinitely, so a client can end up waiting an
absurdly long time before its next attempt.

## Follow-up Interview Questions

Q: How does exponential backoff interact with a retry budget?

Answer:
Backoff controls spacing between attempts; a separate retry budget or
max-attempts cap controls how many attempts happen at all.

Q: Would you use exponential backoff for a real-time, latency-sensitive call?

Answer:
Rarely as designed; for strict latency budgets, a smaller cap, a single
retry, or immediate fallback is usually preferable.

## How the Interviewer Thinks

The interviewer wants to see that jitter is not optional polish, it
materially changes recovery behavior under real contention.

## Production Engineering Perspective

Teams tune base delay, cap, and jitter type per dependency, and monitor
retry-delay distributions to catch synchronized spikes early.

## Common Misconceptions

"Exponential backoff alone prevents retry storms" is false without
jitter, delays can still synchronize across clients that failed
together.

## Revision Notes

- Delay grows as base × 2^attempt, capped, then jittered.
- Full jitter outperforms no-jitter under real contention.
- Always pair with a max-attempts cap.

## Summary

Exponential backoff spaces out retries so a recovering dependency gets
breathing room, but only reliably prevents synchronized overload when
combined with jitter and a sane delay cap.

# Circuit Breakers

## What is it?

A circuit breaker is a stateful guard around a remote call that stops
attempting calls to a failing dependency once failures cross a
threshold, failing fast until it shows signs of recovery.

## Why do we need it?

Retrying a truly broken dependency wastes resources and can keep it
overloaded, delaying recovery.

## What problem does it solve?

Prevents cascading failure, where slow or failing calls exhaust threads
or connections in the caller.

## History (only if useful)

Popularized by Michael Nygard's *Release It!* (2007) and implemented
widely via Netflix Hystrix (2012) and resilience4j.

## How does it work?

In **closed** state, calls pass through normally. Once failures cross a
threshold, it trips to **open**, failing fast. After a timeout, it moves
to **half-open**, allowing trial calls.

## Internal Working

Failure-rate tracking typically uses a sliding window rather than a
consecutive-failure counter, avoiding trips on a brief blip.

## Step-by-Step Example

1. Closed: calls flow normally; failure rate is low.
2. Failure rate crosses 50% over the last 20 calls; breaker opens.
3. For 30 seconds, calls fail immediately without hitting the dependency.
4. Breaker moves to half-open, allows one trial call; it succeeds;
   breaker closes.

## Visualization

```text
CLOSED --failures>threshold--> OPEN --timeout--> HALF-OPEN
   ^                                                 |
   |----------------success----------------------|   |
                                              failure |
   OPEN <----------------------------------------------
```

## Real-World Usage

Netflix Hystrix/resilience4j wrapping microservice calls, database pools
tripping on repeated timeouts, API gateways protecting upstream services.

## Advantages

Fails fast instead of piling up slow calls, gives a failing dependency
room to recover.

## Disadvantages

Adds configuration complexity; a misconfigured breaker can trip on
normal blips or fail to trip during a real outage.

## Trade-offs

Tripping quickly protects the caller but may reject calls that would
have succeeded.

## Alternatives

Bulkheads (isolate resource pools per dependency); load shedding
server-side; simple timeouts without stateful tripping.

## Performance Analysis

Adds negligible overhead per call; the benefit is avoiding the cost of
repeatedly calling a broken dependency.

## Complexity Analysis (if applicable)

O(1) per call for state check; sliding-window tracking costs O(window
size) memory.

## Implementation

```java
CircuitBreaker cb = CircuitBreaker.ofDefaults("paymentService");
Supplier<Response> decorated = CircuitBreaker
    .decorateSupplier(cb, () -> paymentClient.charge(request));
```

## Common Mistakes

Setting thresholds without real traffic data; omitting a fallback; not
monitoring breaker state.

## Common Interview Questions

Q: What are the three states of a circuit breaker?

Answer:
Closed (calls flow normally), open (calls fail fast), and half-open (a
limited number of trial calls test recovery).

Q: How is a circuit breaker different from a retry policy?

Answer:
Retries assume the next attempt might succeed; a circuit breaker assumes
the dependency is broken and stops trying until recovery is evident.

Q: What triggers the transition from open to half-open?

Answer:
A configured timeout elapses, after which the breaker allows a small
number of trial calls.

## Follow-up Interview Questions

Q: Should circuit breakers and retries be used together?

Answer:
Yes, retries handle brief errors while closed; once open, retries stop
since failing fast is more useful than retrying a doomed call.

Q: Why use a sliding window instead of consecutive-failure counting?

Answer:
It tolerates occasional isolated failures without tripping, while still
reacting quickly to a sustained pattern.

## How the Interviewer Thinks

The interviewer wants to see the candidate treats circuit breakers as a
complement to retries, not a replacement.

## Production Engineering Perspective

Teams dashboard breaker state transitions and pick meaningful fallbacks
over generic errors.

## Common Misconceptions

"An open circuit breaker means the service is fully down" is false, it
means calls fail fast by policy.

## Revision Notes

- States: closed, open, half-open.
- Trips on sliding-window failure rate, not one failure.
- Pair with retries (closed state) and a real fallback (open state).

## Summary

Circuit breakers stop a failing dependency from dragging its callers
down with it, complementing retries and backoff.

# Distributed Caching

## What is it?

Distributed caching stores frequently accessed data across multiple
networked nodes rather than in one process's memory, so many application
instances share a fast data layer.

## Why do we need it?

A single-node in-process cache doesn't help other instances behind a
load balancer, and re-fetching from the database on every instance
defeats caching at scale.

## What problem does it solve?

Reduces database load and latency for hot data across an entire fleet.

## History (only if useful)

Memcached (2003) pioneered simple distributed object caching; Redis
(2009) added richer data structures and persistence.

## How does it work?

Cache clients hash keys to determine which node owns them (often via
consistent hashing). Common patterns: cache-aside, write-through, and
write-behind (async flush to the store).

## Internal Working

Consistency between cache and source of truth is the central challenge:
a write that updates the database but fails to invalidate the cache
leaves stale data visible until TTL expiry.

## Step-by-Step Example

1. Request needs `user:42`; cache-aside checks the cache — miss.
2. Application reads from the database and populates the cache with a
   TTL.
3. Later requests hit the cache directly.
4. On update, the application invalidates the cache entry.

## Visualization

```text
App --GET--> [Cache Node A] --miss--> [DB] --populate--> [Cache Node A]
App --GET--> [Cache Node A] --hit --> return value
```

## Real-World Usage

Session storage across a fleet, product-catalog caching, database
query-result caching.

## Advantages

Cuts database load and latency dramatically for read-heavy workloads,
scales horizontally.

## Disadvantages

Introduces a consistency lag between cache and source of truth; can
become a hot-key bottleneck.

## Trade-offs

Cache-aside is simple but risks momentary staleness; write-through keeps
cache fresher but adds write latency; write-behind is fastest but risks
data loss before flushing.

## Alternatives

CDN edge caching for static content; database-native query caches;
client-side caching, which avoids the network hop but doesn't share
state across users.

## Performance Analysis

A high cache-hit ratio turns most reads into sub-millisecond operations,
often reducing database load by an order of magnitude.

## Complexity Analysis (if applicable)

O(1) average lookup per key; consistent hashing keeps rebalancing cost at
roughly O(keys / nodes).

## Implementation

```java
String value = cache.get(key);
if (value == null) {
    value = db.query(key);
    cache.set(key, value, ttlSeconds);
}
```

## Common Mistakes

No TTL, so stale entries live forever; not invalidating cache on write; a
single hot key overwhelming one node.

## Common Interview Questions

Q: What is cache-aside and why is it common?

Answer:
The application checks the cache first, falls back to the database on a
miss, and populates the cache afterward.

Q: How do you keep a distributed cache from serving stale data indefinitely?

Answer:
Set a TTL as a safety net and explicitly invalidate the cache entry on
writes.

Q: What is a hot key and why is it a problem?

Answer:
A single key receiving disproportionate traffic can overwhelm the one
node that owns it even though the rest of the cluster is healthy.

## Follow-up Interview Questions

Q: How does write-behind caching risk data loss?

Answer:
Writes are acknowledged before being flushed to the durable store, so a
cache node failure between acknowledgment and flush can lose that write.

Q: How would you design cache invalidation across multiple services reading the same data?

Answer:
Publish an invalidation event via a message bus when the source of truth
changes, so every service's cache evicts the key independently.

## How the Interviewer Thinks

The interviewer is testing whether the candidate understands
cache-consistency trade-offs rather than treating caching as a black box.

## Production Engineering Perspective

Teams monitor hit ratio and per-key traffic skew, and plan for
cache-cluster failure with graceful degradation to the database.

## Common Misconceptions

"A cache can't be a bottleneck" is false, a poorly distributed hot key
can bottleneck the system it was meant to protect.

## Revision Notes

- Cache-aside, write-through, write-behind trade latency versus
  freshness versus write-loss risk.
- Consistent hashing distributes keys and limits rebalancing cost.
- TTL is a safety net, not a substitute for explicit invalidation.

## Summary

Distributed caching shares a fast data layer across an entire fleet, but
its real challenge is consistency between cache and source of truth.

# Service Discovery

## What is it?

Service discovery is the mechanism by which services find the network
location of other services at runtime, instead of relying on hardcoded
hostnames or IPs.

## Why do we need it?

In dynamic environments, service instances constantly start, stop, and
move, so static address configuration becomes wrong almost immediately.

## What problem does it solve?

Lets clients and load balancers always find currently healthy instances
of a service without manual configuration updates on every deployment.

## History (only if useful)

DNS-based discovery predates microservices; Netflix Eureka (2012),
Consul (2014), and Kubernetes' built-in Service objects (2015) made
dynamic discovery a standard microservices building block.

## How does it work?

Service instances register themselves with a **registry**, including
health status. Clients query the registry to find healthy instances —
either **client-side discovery** (client load-balances itself) or
**server-side discovery** (a proxy does it transparently).

## Internal Working

Health checking keeps the registry honest: instances that stop
heartbeating are removed, but there's inherent lag, so clients still need
timeouts and retries.

## Step-by-Step Example

1. A new instance registers itself with the registry, including its
   health-check endpoint.
2. The registry begins polling that endpoint.
3. A client asks the registry for healthy instances.
4. The client (or its sidecar) picks one and calls it directly.

## Visualization

```text
[Instance A] --register--> [Registry] <--query-- [Client]
[Instance B] --register-->     |
                          health checks
```

## Real-World Usage

Kubernetes Services and DNS resolving to healthy pod IPs, Consul/Eureka
in microservice fleets, service meshes (Istio, Linkerd) using sidecar
proxies.

## Advantages

Removes hardcoded addresses, adapts automatically to scaling and
failures, enables health-aware load balancing.

## Disadvantages

The registry itself is a critical dependency requiring its own high
availability; health-check lag means it can briefly point at a dead
instance.

## Trade-offs

Client-side discovery avoids an extra hop but pushes balancing logic into
every client; server-side discovery centralizes that logic but adds a
hop.

## Alternatives

Static configuration for small, slow-changing fleets; DNS round robin
alone, simpler but with weak health awareness.

## Performance Analysis

Registry lookups are typically cached client-side for seconds, so
per-call overhead is negligible; the main cost is health-polling traffic.

## Complexity Analysis (if applicable)

O(1) average lookup per service name; registry storage and health-check
traffic scale O(instances).

## Implementation

```java
List<ServiceInstance> instances = registry.getInstances("payment-service");
ServiceInstance chosen = loadBalancer.choose(instances);
httpClient.call(chosen.getUri(), request);
```

## Common Mistakes

Caching discovery results too long, calling dead instances; too short,
overloading the registry; skipping client-side retries.

## Common Interview Questions

Q: What's the difference between client-side and server-side service discovery?

Answer:
In client-side discovery, the caller queries the registry and
load-balances itself; in server-side discovery, a proxy does both steps
transparently.

Q: How does Kubernetes implement service discovery?

Answer:
Through built-in DNS and Service objects, a stable name resolves to the
set of currently healthy pod IPs, updated automatically as pods change.

Q: Why is health checking essential to service discovery?

Answer:
Without it, the registry would keep advertising crashed instances,
sending traffic into a black hole.

## Follow-up Interview Questions

Q: What happens if the service registry itself becomes unavailable?

Answer:
Clients typically fall back to their last cached healthy instances for a
bounded time, which is why registries are deployed as a highly available
cluster.

Q: How do service meshes change the service-discovery picture?

Answer:
They move discovery and load balancing into a per-instance sidecar
proxy, so application code no longer needs a discovery client library.

## How the Interviewer Thinks

The interviewer wants to see the candidate understands discovery as a
health-aware, continuously updated system, not a static address book.

## Production Engineering Perspective

Teams tune health-check intervals and cache TTLs to balance registry
load against staleness, and treat the registry cluster as a tier-0
dependency.

## Common Misconceptions

"Service discovery is just DNS" is false for dynamic environments, plain
DNS lacks fast health-aware updates and fine-grained load-balancing
signals.

## Revision Notes

- Client-side vs. server-side discovery is a recurring architectural
  choice.
- Health checking keeps a registry trustworthy, with inherent lag.
- The registry cluster itself needs high availability.

## Summary

Service discovery keeps a fleet's map of who is healthy and where up to
date automatically, replacing brittle static configuration with a
health-aware registry both clients and proxies rely on.
