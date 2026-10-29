# Core CS Interview Answers

## Object-Oriented Programming (OOP)

### Core Concepts

#### Encapsulation
Encapsulation bundles data and methods inside a class and restricts direct access to internal state. It protects invariants and exposes controlled operations through a public interface.

#### Inheritance (types and use cases)
Inheritance lets a child class reuse or specialize a parent class; common types are single, multilevel, hierarchical, multiple, and hybrid. Use it for a genuine **is-a** relationship, not merely to reuse code.

#### Polymorphism (compile-time vs runtime)
Polymorphism lets one interface represent different implementations. Compile-time polymorphism uses overloading/operator overloading, while runtime polymorphism uses overriding through dynamic dispatch.

#### Abstraction
Abstraction exposes essential behavior while hiding implementation details. It reduces complexity by letting callers depend on what an object does rather than how it does it.

#### Abstract Class vs Interface
An abstract class can hold state, constructors, and implemented methods, whereas an interface mainly defines a contract that multiple unrelated classes can implement. Use an abstract class for shared base behavior and an interface for capabilities.

#### Method Overloading vs Overriding
Overloading defines methods with the same name but different parameter lists and is resolved at compile time. Overriding replaces an inherited method with a subclass implementation and is resolved at runtime.

#### Access Modifiers
Access modifiers control visibility: `public` is broadly accessible, `private` only within the class, `protected` within subclasses/package depending on language, and default/package-private within the package.

#### Static vs Dynamic Binding
Static binding resolves a call at compile time, commonly for static, private, final, or overloaded methods. Dynamic binding resolves overridden instance methods at runtime using the object's actual type.

#### Deep Copy vs Shallow Copy
A shallow copy creates a new outer object but shares referenced nested objects. A deep copy recursively duplicates nested mutable objects so changes do not affect the original.

### Advanced Topics

#### SOLID Principles
SOLID means Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion. Together they encourage maintainable, extensible, loosely coupled object-oriented designs.

#### Diamond Problem (Multiple Inheritance)
The diamond problem occurs when a class inherits the same member through two parent paths, creating ambiguity. Languages solve it using virtual inheritance, explicit resolution, interfaces, or by disallowing multiple class inheritance.

#### Association vs Aggregation vs Composition
Association is a general relationship; aggregation is a weak **has-a** relationship where parts can exist independently. Composition is strong ownership where a part's lifecycle normally depends on the whole.

#### Virtual Functions and Vtable
A virtual function supports runtime overriding; many languages implement it using a per-class virtual table of function pointers. Each object carries or implies a vtable reference used to dispatch the correct method.

#### Design Patterns
Design patterns are reusable templates for recurring software-design problems rather than ready-made code. Creational, structural, and behavioral patterns improve communication and design consistency when applied appropriately.

#### Singleton Pattern
Singleton ensures a class has one globally accessible instance, often through a private constructor and static accessor. Use carefully because global state complicates testing, concurrency, and dependency management.

#### Factory Pattern
Factory centralizes object creation and returns an implementation through a common abstraction. It hides construction details and lets callers remain independent of concrete classes.

#### Observer Pattern
Observer creates a one-to-many dependency where subscribers are notified when a subject changes. It is common in event systems, GUIs, and message-driven applications.

#### Strategy Pattern
Strategy defines interchangeable algorithms behind a common interface and lets behavior be selected at runtime. It replaces large conditional blocks and favors composition over inheritance.

#### Decorator Pattern
Decorator wraps an object to add behavior without changing its class or existing callers. Multiple decorators can be composed dynamically, such as buffering and compression around a stream.

#### Adapter Pattern
Adapter converts one interface into another expected by the client. It enables incompatible or legacy components to work together without modifying their source code.

## Operating Systems (OS)

### Process Management

#### Process vs Thread
A process has its own address space and resources, while threads within a process share memory and resources. Threads are lighter to create and switch but require synchronization around shared data.

#### Process States and PCB
Typical process states are new, ready, running, waiting/blocked, and terminated. The Process Control Block stores its PID, state, registers, scheduling data, memory information, and open resources.

#### Context Switching
Context switching saves the state of one process or thread and restores another's state. It enables multitasking but adds overhead because no application work occurs during the switch.

#### CPU Scheduling Algorithms
FCFS runs in arrival order, SJF chooses the shortest job, Round Robin gives each process a time quantum, and Priority chooses the highest priority. Their trade-offs involve fairness, response time, throughput, starvation, and overhead.

#### Multithreading vs Multiprocessing
Multithreading uses shared memory within one process and is lightweight but vulnerable to shared-state bugs. Multiprocessing provides isolation and true parallelism with higher communication and creation costs.

#### User Mode vs Kernel Mode
User mode restricts privileged operations to protect the system, while kernel mode can directly access hardware and all memory. Applications use system calls to request kernel services.

### Synchronization

#### Critical Section Problem
A critical section accesses shared data and must not execute concurrently when doing so would be unsafe. A correct solution provides mutual exclusion, progress, and bounded waiting.

#### Race Condition
A race condition occurs when a result depends on the unpredictable timing of concurrent operations. Synchronization or elimination of shared mutable state is required to make the result deterministic.

#### Mutex vs Semaphore
A mutex provides ownership-based mutual exclusion and is unlocked by its owner; a binary semaphore is a 0/1 signal without ownership. A counting semaphore tracks multiple available units of a resource.

#### Monitors and Locks
A lock explicitly controls entry to a critical section. A monitor is a higher-level construct that combines protected data, mutually exclusive methods, and condition variables for waiting and notification.

#### Producer-Consumer Problem
Producers add items to a bounded buffer while consumers remove them. Mutual exclusion protects the buffer, while empty/full semaphores or condition variables coordinate availability and capacity.

#### Readers-Writers Problem
Multiple readers may access shared data together, but writers require exclusive access. Solutions differ in whether they prioritize readers, writers, or fairness to prevent starvation.

#### Dining Philosophers Problem
Philosophers compete for pairs of shared forks, illustrating deadlock and starvation. Solutions include resource ordering, limiting contenders, using a waiter, or acquiring both resources atomically.

### Deadlocks

#### Deadlock
A deadlock is a state where processes wait indefinitely for resources held by one another. None can proceed unless the cycle is prevented, broken, or a process is terminated.

#### Deadlock Conditions
Deadlock requires four simultaneous conditions: mutual exclusion, hold and wait, no preemption, and circular wait. Breaking any one condition prevents deadlock.

#### Deadlock Prevention vs Avoidance vs Detection
Prevention structurally breaks a deadlock condition, avoidance grants resources only when the state remains safe, and detection allows deadlocks then finds and recovers from them.

#### Banker's Algorithm
Banker's Algorithm simulates each resource request and grants it only if all processes can still finish in some safe sequence. It needs advance knowledge of each process's maximum demand.

### Memory Management

#### Paging vs Segmentation
Paging divides memory into fixed-size pages/frames and avoids external fragmentation. Segmentation uses variable-size logical units such as code and stack, matching program structure but causing external fragmentation.

#### Page Replacement Algorithms
FIFO removes the oldest page, LRU removes the least recently used page, and Optimal removes the page needed farthest in the future. Optimal is a theoretical benchmark because future accesses are unknown.

#### Thrashing
Thrashing occurs when insufficient frames cause excessive page faults, so the system spends more time swapping than executing. Reducing multiprogramming or allocating more frames can restore performance.

#### Virtual Memory
Virtual memory gives each process a large logical address space and loads only needed pages into RAM. It improves isolation and capacity but page faults are much slower than memory access.

#### TLB (Translation Lookaside Buffer)
A TLB is a small, fast cache of recent virtual-to-physical address translations. A TLB hit avoids an additional page-table lookup, greatly reducing address-translation time.

#### Internal vs External Fragmentation
Internal fragmentation is wasted space inside an allocated fixed-size block. External fragmentation is free memory split into small noncontiguous holes that cannot satisfy a large contiguous request.

### File Systems and Disk

#### File Allocation Methods
Contiguous allocation is fast but hard to grow, linked allocation grows easily but has slow random access, and indexed allocation stores block pointers in an index for direct access at extra metadata cost.

#### Disk Scheduling
FCFS serves requests in order, SSTF chooses the nearest request, SCAN sweeps in both directions, and C-SCAN serves in one direction before jumping back. Better seek time may trade off against fairness.

## Database Management Systems (DBMS) and SQL

### Database Fundamentals

#### ACID Properties
Atomicity is all-or-nothing, Consistency preserves rules, Isolation hides concurrent intermediate states, and Durability preserves committed data. A bank transfer must debit and credit together and survive a crash after commit.

#### CAP Theorem
During a network partition, a distributed system can guarantee either Consistency or Availability, but not both simultaneously. Partition tolerance is generally unavoidable, so systems choose their partition-time behavior.

#### Normalization
1NF makes values atomic, 2NF removes partial dependency on a composite key, 3NF removes transitive dependency, and BCNF requires every determinant to be a candidate key. It reduces redundancy and update anomalies.

#### Denormalization
Denormalization intentionally duplicates or combines data to reduce joins and improve read performance. It increases storage and makes consistency and updates more complex.

#### Primary Key vs Foreign Key vs Candidate Key
A candidate key can uniquely identify a row; one candidate is selected as the primary key. A foreign key references a key in another table to enforce relationships and referential integrity.

#### ER Diagrams
An Entity-Relationship diagram models entities, attributes, relationships, and cardinalities such as one-to-one or one-to-many. It is used to design a database schema before implementation.

### Database Indexing

#### Indexing
An index is an auxiliary data structure that helps the database locate rows without scanning the entire table. It improves selected reads at the cost of storage and additional write maintenance.

#### Types of Indexes
A primary index is built on the primary ordering key, a clustering index follows the table's physical order, and a secondary index provides another access path without changing row order.

#### B-Tree vs B+ Tree
B-Trees may store records in internal and leaf nodes, while B+ Trees store records only in linked leaves. B+ Trees provide higher fan-out and efficient range scans, so databases commonly prefer them.

#### Hash Index
A hash index maps a key through a hash function to a bucket, giving near-constant-time equality lookups. It is unsuitable for ordered scans, ranges, and prefix queries.

#### Composite Index
A composite index covers multiple columns in a defined order. It usually supports queries matching the leftmost prefix, so column order should reflect common filters, joins, and sorting.

#### Advantages and Disadvantages of Indexing
Indexes speed reads, joins, ordering, and uniqueness checks but consume storage and slow inserts, updates, and deletes. Too many or poorly chosen indexes may waste memory without helping queries.

### Transactions and Concurrency

#### Transaction Lifecycle
A transaction typically moves through active, partially committed, committed, failed, and aborted states. On failure it rolls back; after a successful commit its changes become durable.

#### Isolation Levels
Read Uncommitted allows dirty reads; Read Committed blocks them; Repeatable Read also stabilizes reread rows; Serializable gives the strongest isolation by making concurrent execution appear sequential.

#### Dirty Read, Non-repeatable Read, Phantom Read
A dirty read sees uncommitted data, a non-repeatable read gets different values for the same row, and a phantom read finds new or missing rows when repeating a range query.

#### Lost Update Problem
A lost update occurs when concurrent transactions read the same value and one later overwrites the other's update. Locks, atomic updates, or optimistic version checks prevent it.

#### Two-Phase Locking (2PL)
In 2PL, a growing phase acquires locks and a shrinking phase releases them without acquiring new ones. It ensures conflict serializability; strict 2PL holds write locks until commit to simplify recovery.

#### Optimistic vs Pessimistic Locking
Optimistic locking assumes conflicts are rare and validates a version before commit. Pessimistic locking acquires locks early, reducing conflicts but increasing blocking and deadlock risk.

#### Deadlock in Database
A database deadlock occurs when transactions wait cyclically for each other's locks. The DB detects the cycle and aborts a victim, while consistent lock ordering can reduce occurrences.

### SQL Queries

#### JOINs
`INNER` returns matches, `LEFT/RIGHT` retain unmatched rows from one side, `FULL OUTER` retains both sides, `CROSS` forms all combinations, and `SELF JOIN` joins a table to itself.

#### GROUP BY and HAVING
`GROUP BY` combines rows into groups for aggregation, while `HAVING` filters the resulting groups. `WHERE` filters individual rows before grouping.

#### Aggregate Functions
`COUNT`, `SUM`, `AVG`, `MIN`, and `MAX` summarize a set or group of rows. Most aggregates ignore `NULL`, while `COUNT(*)` counts every row.

#### Correlated vs Non-correlated Subqueries
A non-correlated subquery runs independently and can be evaluated once. A correlated subquery refers to the outer row and is logically reevaluated for each row, though optimizers may transform it.

#### Window Functions
Window functions calculate across related rows without collapsing them: `ROW_NUMBER` is unique, `RANK` leaves gaps after ties, `DENSE_RANK` does not, and `LEAD/LAG` access neighboring rows.

#### Common Table Expressions (CTE)
A CTE uses `WITH` to name a temporary query result for readability and reuse within one statement. Recursive CTEs can traverse trees, graphs, and hierarchical data.

#### UNION vs UNION ALL
`UNION` combines results and removes duplicates, requiring extra work. `UNION ALL` preserves duplicates and is usually faster.

#### Nth Highest Salary Query
Use `DENSE_RANK() OVER (ORDER BY salary DESC)` and select rank `N` to find the Nth distinct highest salary. `DENSE_RANK` correctly handles tied salaries.

```sql
SELECT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employee
) ranked WHERE rnk = N;
```

#### Delete Duplicates Query
Assign `ROW_NUMBER()` within each duplicate group and delete rows whose number is greater than one, retaining a deterministic row such as the smallest ID.

```sql
WITH d AS (
  SELECT id, ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) AS rn FROM users
)
DELETE FROM users WHERE id IN (SELECT id FROM d WHERE rn > 1);
```

### NoSQL

#### NoSQL
NoSQL describes non-relational databases designed for models such as documents, key-value pairs, wide columns, or graphs. They often prioritize flexible schemas and distributed horizontal scaling.

#### SQL vs NoSQL
SQL databases use relational schemas and strong querying/transaction support; NoSQL databases favor flexible models and horizontal scaling. The choice depends on access patterns, consistency needs, and data relationships.

#### Types of NoSQL Databases
Document stores keep JSON-like objects, key-value stores map keys to opaque values, column-family stores organize sparse distributed columns, and graph databases optimize connected nodes and edges.

## Computer Networks (CN)

### Network Models

#### OSI Model
The seven layers are Physical, Data Link, Network, Transport, Session, Presentation, and Application. The model separates networking responsibilities to simplify design, learning, and troubleshooting.

#### TCP/IP Model
The four layers are Link/Network Access, Internet, Transport, and Application. It is the practical model used by the Internet protocol suite.

#### OSI vs TCP/IP
OSI is a seven-layer conceptual reference model, while TCP/IP is a four-layer implementation-oriented protocol suite. TCP/IP combines OSI's session and presentation functions into Application.

### Application Layer

#### HTTP vs HTTPS
HTTP transfers web messages in plaintext, while HTTPS runs HTTP over TLS for encryption, server authentication, and integrity. HTTPS normally uses port 443; HTTP uses port 80.

#### HTTP Methods
`GET` reads, `POST` creates/processes, `PUT` replaces a resource, `PATCH` partially updates it, and `DELETE` removes it. `GET`, `PUT`, and `DELETE` are intended to be idempotent.

#### HTTP Status Codes
`2xx` means success, `3xx` redirection, `4xx` client error, and `5xx` server error. Common examples are 200, 201, 301, 400, 401, 403, 404, and 500.

#### DNS and Its Working
DNS translates domain names into IP addresses. A resolver checks caches, then queries root, TLD, and authoritative name servers before returning and caching the record.

#### FTP, SMTP, POP3, and IMAP
FTP transfers files, SMTP sends email, POP3 usually downloads mail to one client, and IMAP synchronizes server-stored mail across devices. Secure variants use TLS.

#### Cookies vs Sessions
Cookies are small client-side values sent with matching requests; sessions store user state on the server and usually identify it using a cookie. Cookies need secure flags, and server sessions require storage and expiry management.

#### REST API Principles
REST uses resource-oriented URIs, standard HTTP methods, stateless requests, cacheable responses, and a uniform interface. Representations such as JSON transfer resource state between client and server.

### Transport Layer

#### TCP vs UDP
TCP is connection-oriented, reliable, ordered, flow-controlled, and congestion-controlled but has more overhead. UDP is connectionless and message-oriented with low latency but no built-in delivery or ordering guarantee.

#### TCP Three-Way Handshake
The client sends `SYN`, the server replies `SYN-ACK`, and the client sends `ACK`. This synchronizes sequence numbers and establishes a bidirectional connection.

#### TCP Four-Way Termination
One endpoint sends `FIN`, the peer acknowledges it, then sends its own `FIN`, which is acknowledged. Separate shutdowns are needed because TCP directions close independently.

#### Flow Control (Sliding Window)
Flow control prevents a fast sender from overwhelming a slow receiver. TCP's sliding receive window limits unacknowledged data according to currently available receiver buffer space.

#### Congestion Control
Congestion control protects the network by adjusting the sender's congestion window using mechanisms such as slow start, congestion avoidance, fast retransmit, and fast recovery.

#### Port Numbers
Ports identify application endpoints on a host; well-known ports range from 0-1023. Examples include SSH 22, DNS 53, HTTP 80, HTTPS 443, SMTP 25, and MySQL 3306.

#### Socket Programming Basics
A socket is an endpoint identified by protocol, IP address, and port. TCP servers typically create, bind, listen, accept, read/write, and close; clients connect, exchange data, and close.

### Network Layer

#### IPv4 vs IPv6
IPv4 uses 32-bit addresses and often relies on NAT, while IPv6 uses 128-bit addresses with a much larger address space and simplified headers. IPv6 also supports automatic configuration and eliminates broadcast.

#### Public vs Private IP
A public IP is globally routable on the Internet, while a private IP is used within local networks and is not publicly routed. IPv4 private ranges include `10/8`, `172.16/12`, and `192.168/16`.

#### Subnetting and CIDR
Subnetting divides an address block into smaller networks. CIDR notation such as `/24` states how many leading bits form the network prefix, leaving the remaining bits for host addresses.

#### NAT
Network Address Translation rewrites IP addresses, and often ports, between private and public networks. It conserves IPv4 addresses but breaks direct end-to-end addressing.

#### ICMP Protocol
ICMP carries network diagnostics and error messages, such as destination unreachable and time exceeded. `ping` uses echo request/reply, while `traceroute` relies on TTL expiration responses.

#### Routing Algorithms
Distance Vector shares route distances with neighbors and commonly uses Bellman-Ford; Link State floods topology information and runs Dijkstra. Link State converges faster but requires more resources.

#### Routing Protocols
RIP is a simple distance-vector protocol, OSPF is a link-state protocol within an autonomous system, and BGP exchanges policy-based routes between autonomous systems on the Internet.

### Data Link Layer

#### MAC Address
A MAC address identifies a network interface at the local data-link layer, commonly as a 48-bit value. Switches use MAC address tables to forward Ethernet frames within a LAN.

#### ARP
ARP maps an IPv4 address to a MAC address on the local network. A host broadcasts a request, caches the reply, and sends the frame to that MAC address.

#### Switch vs Hub vs Router
A hub repeats traffic to every port, a switch forwards frames using MAC addresses within a LAN, and a router forwards packets between IP networks using routing tables.

#### Ethernet
Ethernet is the dominant wired LAN technology and defines framing, MAC addressing, and physical transmission rules. Modern switched full-duplex Ethernet avoids collisions.

#### Error Detection
Parity adds a simple bit check, checksums sum data units, and CRC performs polynomial division to detect burst errors. CRC generally detects transmission corruption more reliably.

### Physical Layer

#### Transmission Media
Guided media carries signals through cables such as twisted pair, coaxial, and fiber. Unguided media transmits through air using radio, microwave, infrared, or satellite links.

#### Bandwidth vs Throughput
Bandwidth is the theoretical maximum data-carrying capacity of a link, while throughput is the actual delivered rate. Protocol overhead, congestion, loss, and hardware limitations reduce throughput.

#### Network Topologies
Common topologies are bus, star, ring, mesh, tree, and hybrid. They trade off cost, scalability, fault tolerance, cabling, and ease of management.

### Important Concepts

#### Client-Server vs Peer-to-Peer
Client-server centralizes services on dedicated servers, simplifying control but creating central dependencies. Peer-to-peer lets nodes act as both clients and servers, improving distribution but complicating coordination.

#### DHCP
DHCP automatically assigns IP configuration through Discover, Offer, Request, and Acknowledge messages. A lease may include the IP address, subnet mask, gateway, and DNS servers.

#### Firewall
A firewall permits or blocks traffic according to rules based on addresses, ports, protocols, state, or application data. It reduces exposure but does not replace secure application design.

#### VPN
A VPN creates an encrypted tunnel over an untrusted network and can connect a user or site to a private network. It protects traffic in transit but does not make endpoints inherently trustworthy.

#### Load Balancing
Load balancing distributes requests across servers using algorithms such as round robin, least connections, or hashing. It improves scalability and availability and removes unhealthy instances using health checks.

#### CDN
A Content Delivery Network caches content at geographically distributed edge locations. Serving users from a nearby edge reduces latency, origin load, and bandwidth usage.

#### Latency vs Throughput
Latency is the time required for one operation or packet journey, while throughput is work or data completed per unit time. A system can have high throughput yet poor per-request latency.

#### How Does a URL Work?
The browser parses the URL, resolves DNS, establishes TCP and usually TLS, sends an HTTP request, receives a response, and renders resources. Caches, proxies, CDNs, load balancers, and redirects may participate.

#### Basic Network Commands
`ipconfig`/`ifconfig`/`ip addr` shows interfaces, `ping` tests reachability, `traceroute`/`tracert` shows hops, `nslookup`/`dig` queries DNS, and `netstat`/`ss` shows connections and listening ports.
