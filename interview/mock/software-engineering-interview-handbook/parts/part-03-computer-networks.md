# Part III: Computer Networks

## Introduction

Every distributed system, no matter how elegant its architecture, ultimately
depends on bytes moving correctly between machines that do not share memory,
a clock, or a trust boundary. This part builds the networking foundation an
interviewer expects from anyone claiming backend, infrastructure, or platform
experience: how names become addresses, how addresses become reliable byte
streams, how those streams become secure, and how modern systems shape that
traffic at scale with proxies, load balancers, CDNs, and RPC frameworks.

The chapters are sequenced deliberately. We start with the two mental maps
everyone uses to reason about networking (OSI and TCP/IP), then descend into
the transport layer that makes unreliable wires feel reliable (TCP, UDP, the
handshake and teardown that bookend every connection, and the flow and
congestion control that keep it stable). From there we move to the protocols
that resolve identity on a network (DNS, ARP, NAT), then to the application
and security layer that powers the web (HTTP, HTTPS, TLS), and finally to the
infrastructure that sits in front of real production systems (reverse
proxies, load balancers, CDNs) and the modern communication patterns that
replace plain request/response (WebSockets, gRPC).

Every chapter follows the same discipline: define the concept precisely,
explain the failure mode it exists to prevent, show its internal mechanics
with an ASCII diagram, and connect it to how the concept actually breaks in
production. The goal is not memorization. It is the ability to trace a single
request from a browser's address bar to a backend service and back, and to
explain, at any point along that path, what could go wrong and why.

## Packet-Flow Roadmap

The diagram below is the map for this part. It follows one HTTPS request from
a cold browser to a backend service and back, annotated with the chapter that
explains each hop. Later chapters assume you can place any networking term
somewhere on this line.

```text
 USER TYPES https://app.example.com/orders IN A BROWSER
      |
      | 1. Name resolution                         -> [DNS]
      v
 +---------------------+        recursive resolver walks
 |  DNS Resolver        | <---- root -> .com TLD -> authoritative NS
 +---------------------+
      |
      | 2. Local-segment address resolution        -> [ARP]
      v
 client resolves the MAC address of its default gateway
      |
      | 3. Private -> public address translation    -> [NAT]
      v
 home/VPC router rewrites (src IP, src port)
      |
      | 4. Connection establishment                 -> [TCP], [Three-Way Handshake]
      v
 SYN ------------------------------->
      <----------------------------- SYN-ACK
 ACK  ------------------------------->
      |
      | 5. Secure the channel                       -> [TLS], [HTTPS]
      v
 ClientHello --------------------->
      <--------------------------- ServerHello, certificate, Finished
 Finished ------------------------->
      |
      | 6. Throughput is paced the whole time        -> [Flow Control], [Congestion Control]
      v
 +-----------------+     +----------------+     +----------------+
 |  Load Balancer   | --> | Reverse Proxy  | --> | Backend Service|
 |  (L4/L7)         |     | (TLS term,     |     | (may call peers|
 |  [Load Balancer] |     |  routing)      |     |  over [gRPC])  |
 +-----------------+     | [Reverse Proxy]|     +----------------+
      ^                  +----------------+              |
      |                                                  |
      | 7. Edge cache may answer before origin is hit    |
      +---------------------- [CDN] -------------------- +
      |
      | 8. Application semantics                     -> [HTTP]
      v
 GET /orders HTTP/1.1  ------------->
      <------------------------------ 200 OK + JSON body
      |
      | 9. Live updates without re-polling            -> [WebSockets]
      v
 Upgrade: websocket  --------------->
      <------------------------------ 101 Switching Protocols
      |
      | 10. Connection ends cleanly                   -> [Connection Termination]
      v
 FIN -> ACK -> FIN -> ACK  (TIME_WAIT, then CLOSED)
```

Two chapters underpin every hop above rather than occupying one box: the
[OSI Model](#osi-model) and [TCP/IP](#tcpip) chapters give you the layered
vocabulary ("that is a Layer 4 concern", "that header belongs to Layer 3")
that lets you describe any hop precisely in an interview.

---

# OSI Model

## What is it?
A seven-layer conceptual reference model (Physical, Data Link, Network, Transport, Session, Presentation, Application) describing how network communication is organized into independent, stacked responsibilities.

## Why do we need it?
Building and reasoning about networking is easier when responsibilities are separated into layers.

## What problem does it solve?
It solves the complexity of designing interoperable networked systems by giving every protocol and piece of hardware a well-defined place and responsibility.

## History (only if useful)
Developed by the ISO in 1984 as a vendor-neutral standard.

## How does it work?
Each layer provides services to the layer above it, consumes services from the layer below it, and communicates with its peer layer on the other host, while staying ignorant of layers more than one level away.

## Internal Working
Data passed down the stack is wrapped with each layer's own header (encapsulation): Transport adds a TCP/UDP header, Network adds an IP header, and Data Link adds a frame header/trailer, reversing (decapsulation) on the receiving host.

## Step-by-Step Example
1. An application generates data (e.g., an HTTP request).
2. Transport layer wraps it with a TCP header (ports, sequence numbers).

## Visualization
```text
 Layer 7 Application   |  HTTP data
 Layer 6 Presentation  |  (encoding, encryption)
 Layer 5 Session       |  (session mgmt)
 Layer 4 Transport     |  [ TCP/UDP header | data ]
 Layer 3 Network       |  [ IP header | TCP segment ]
 Layer 2 Data Link     |  [ Frame header | IP packet | trailer ]
 Layer 1 Physical      |  raw bits on the wire
```

## Real-World Usage
Used constantly as shared troubleshooting vocabulary ("is this a Layer 2 or Layer 3 issue?") and as a teaching framework.

## Advantages
- Gives engineers a common vocabulary for discussing and isolating network problems.
- Encourages modular, interoperable protocol and hardware design.

## Disadvantages
- No real-world stack implements all seven layers exactly as described.
- Session and Presentation layers are rarely distinct in practice (folded into applications or TLS).

## Trade-offs
The OSI model trades strict practical implementability for conceptual clarity and a shared vocabulary, while TCP/IP trades that theoretical completeness for a simpler model that matches what was actually built and deployed.

## Alternatives
- TCP/IP four-layer model: simpler, matches what the internet actually implements.
- Vendor- or protocol-specific stacks: some systems describe their own layering that doesn't map cleanly to either model.

## Performance Analysis
The model itself has no performance characteristics.

## Complexity Analysis (if applicable)
Not applicable in an algorithmic sense.

## Implementation
```text
Layer 7 Application:   HTTP, DNS, gRPC
Layer 4 Transport:      TCP, UDP
Layer 3 Network:        IP, ICMP
Layer 2 Data Link:      Ethernet, ARP
Layer 1 Physical:       cabling, radio, voltage signaling
```

## Common Mistakes
- Trying to force every real protocol into exactly one OSI layer when some (like TLS) span or blur layer boundaries.
- Confusing the OSI model with the TCP/IP model, which has only four layers.

## Common Interview Questions

Q: Why does the OSI model have seven layers if the internet's real stack only has four?

Answer:
OSI was designed as a vendor-neutral, maximally general reference model before the internet's own TCP/IP stack became dominant in practice.

## Follow-up Interview Questions

Q: Where does TLS fit in the OSI model, and why does that expose the model's limits?

Answer:
TLS sits loosely between the Transport and Application layers but maps to no single OSI layer, showing the model is a teaching abstraction, not a strict blueprint.

## How the Interviewer Thinks
Interviewers use OSI questions to check whether a candidate can map real, everyday protocols (HTTP, TCP, IP, Ethernet) onto layers correctly.

## Production Engineering Perspective
Production engineers use OSI layer vocabulary daily to scope incidents quickly, distinguishing "this is a Layer 3/4 connectivity problem" (routing, firewall, TCP resets) from "this is a Layer 7 problem" (application logic, malformed responses).

## Common Misconceptions
- "The OSI model is what the internet actually runs on." The internet runs on the simpler TCP/IP model.

## Revision Notes
- 7 layers: Physical, Data Link, Network, Transport, Session, Presentation, Application.
- Encapsulation adds a header per layer going down.

## Summary
The OSI model organizes networking into seven layered responsibilities, giving engineers a shared vocabulary for building and troubleshooting interoperable systems, even though no real stack implements it literally.

---

# TCP/IP

## What is it?
A practical four-layer networking model (Link, Internet, Transport, Application) and the actual suite of protocols, most centrally IP and TCP, that the modern internet is built on.

## Why do we need it?
The internet needed a real, implementable, interoperable protocol suite that different networks and vendors could all adopt to exchange data reliably across many interconnected, heterogeneous networks.

## What problem does it solve?
It solves internetworking: connecting many independently operated, physically different networks into a single logical network where any two hosts can communicate, regardless of the underlying hardware or local network technology.

## History (only if useful)
Developed by Vint Cerf and Bob Kahn in the 1970s for ARPANET and adopted as its standard on January 1, 1983 (the internet's informal "birthday"), it has remained the dominant protocol suite ever since.

## How does it work?
The Application layer produces data, the Transport layer (TCP or UDP) adds delivery semantics and port numbers, the Internet layer (IP) adds logical addressing and routing, and the Link layer delivers across the local physical network.

## Internal Working
IP performs best-effort, connectionless packet delivery, using routing tables and protocols (like BGP, OSPF) to move packets across networks hop by hop.

## Step-by-Step Example
1. An application calls a socket API to send data to a destination IP and
   port.
2. TCP segments the data, adding sequence numbers and port information.

## Visualization
```text
 Application  |  HTTP, DNS, gRPC
 Transport    |  TCP (reliable) or UDP (fast, unordered)
 Internet     |  IP (addressing, routing)
 Link         |  Ethernet, Wi-Fi (local delivery)
```

## Real-World Usage
Every internet connection, from browsing the web to cloud service-to- service calls, runs on this four-layer stack.

## Advantages
- Simple, practical model that matches what is actually implemented and deployed.
- Modular: TCP and UDP can be swapped depending on application needs, independent of IP.

## Disadvantages
- IPv4's address space exhaustion required NAT as a workaround, breaking true end-to-end addressing.
- Four broad layers offer less granularity than OSI's seven for precise discussion.

## Trade-offs
TCP/IP trades OSI's theoretical granularity for practical simplicity, and IP's own best-effort delivery trades guaranteed reliability for simplicity, pushing reliability up into TCP only where needed.

## Alternatives
- OSI seven-layer model: more granular conceptual framework, not itself implemented.
- IPX/SPX, AppleTalk: legacy alternative protocol suites, both effectively obsolete today.

## Performance Analysis
IP routing lookups are typically longest-prefix-match operations optimized in hardware (using tries or CAMs) on routers.

## Complexity Analysis (if applicable)
IP routing table lookups are typically O(log n) via longest-prefix-match trie structures relative to the number of routing table entries, independent of the total number of hosts on the internet.

## Implementation
```text
$ traceroute example.com
$ ip route show
```

## Common Mistakes
- Confusing the OSI model's seven layers with TCP/IP's four when describing where a protocol sits.
- Assuming IP guarantees delivery.

## Common Interview Questions

Q: What are the four layers of the TCP/IP model?

Answer:
Link (local network delivery), Internet (IP addressing and routing), Transport (TCP or UDP), and Application (HTTP, DNS, and similar protocols using that transport).

## Follow-up Interview Questions

Q: Why did IPv4 address exhaustion happen, and what are the two main responses to it?

Answer:
IPv4's 32-bit address space provides roughly 4.3 billion addresses, far fewer than today's connected devices; NAT (sharing one public address across many devices) and IPv6 (a vastly larger address space) are the two main responses.

## How the Interviewer Thinks
Interviewers use TCP/IP questions to confirm a candidate understands the practical, implemented stack (versus the theoretical OSI model) and can explain why responsibilities like reliability were deliberately placed in TCP rather than IP.

## Production Engineering Perspective
Engineers use `traceroute` and `ip route` to diagnose connectivity issues hop by hop, and IPv4 exhaustion concerns directly drive infrastructure decisions like NAT gateway capacity planning and IPv6 dual-stack rollout.

## Common Misconceptions
- "IP guarantees packet delivery." It is explicitly best-effort.

## Revision Notes
- Four layers: Link, Internet (IP), Transport (TCP/UDP), Application.
- IP = best-effort, connectionless addressing/routing.

## Summary
TCP/IP is the practical four-layer protocol suite the real internet runs on, deliberately keeping IP simple and best-effort while pushing reliability into TCP only where needed.

---

# TCP

## What is it?
A connection-oriented, reliable, ordered, byte-stream transport-layer protocol that guarantees delivered data arrives complete, in order, and without duplication.

## Why do we need it?
Applications like file transfer, web requests, and databases need data to arrive correctly and in order without reimplementing retransmission, ordering, and flow control themselves atop IP's best-effort delivery.

## What problem does it solve?
It solves unreliable, unordered, best-effort IP delivery by adding acknowledgments, retransmission, sequencing, flow control, and congestion control.

## History (only if useful)
Specified by Vint Cerf and Bob Kahn alongside IP in the 1970s and formalized in RFC 793 (1981), TCP has been continuously refined since, including congestion control added in the late 1980s.

## How does it work?
Two hosts establish a connection via a three-way handshake, exchange data as sequenced, acknowledged segments, retransmit any segment not acknowledged within a timeout, and close the connection via a four-way termination.

## Internal Working
Every byte sent has an implicit sequence number.

## Step-by-Step Example
1. Client and server complete a three-way handshake (SYN, SYN-ACK, ACK).
2. Client sends a segment with sequence number 1000 for 500 bytes of
   data.

## Visualization
```text
 Sender                                   Receiver
   |------ seq=1000, len=500 ------------->|
   |<----------------- ACK=1500 -----------|
   |------ seq=1500, len=500 ------------->|
   |<----------------- ACK=2000 -----------|
   (sliding window allows several unacknowledged segments in flight)
```

## Real-World Usage
Used for HTTP/HTTPS, database connections, SSH, email, and virtually any protocol where correctness and completeness of the data matter more than absolute minimum latency.

## Advantages
- Guarantees reliable, ordered, duplicate-free delivery to the application.
- Automatically adapts to network conditions via flow and congestion control.

## Disadvantages
- Handshake and reliability guarantees add latency compared to a connectionless protocol.
- Head-of-line blocking: one lost segment stalls delivery of everything behind it.

## Trade-offs
TCP trades raw speed and simplicity for strong reliability and ordering guarantees.

## Alternatives
- UDP: connectionless, no reliability guarantees, lower latency and overhead.
- QUIC: reliable and ordered like TCP, but built on UDP with independent streams to avoid head-of-line blocking.

## Performance Analysis
Throughput is bounded by the smaller of the congestion window and receiver's advertised window divided by round-trip time.

## Complexity Analysis (if applicable)
Per-segment processing (sequence tracking, acknowledgment matching) is O(1) with appropriate data structures.

## Implementation
```java
Socket socket = new Socket("example.com", 80);
OutputStream out = socket.getOutputStream();
out.write("GET / HTTP/1.1\r\nHost: example.com\r\nConnection: close\r\n\r\n".getBytes());
```

## Common Mistakes
- Assuming TCP guarantees low latency.
- Not handling partial reads/writes correctly, since TCP is a byte stream, not a message-based protocol.

## Common Interview Questions

Q: What guarantees does TCP provide that IP alone does not?

Answer:
Reliable delivery (retransmitting lost segments), ordered delivery (reassembling out-of-order segments), and duplicate suppression, none of which the underlying best-effort IP layer provides on its own.

## Follow-up Interview Questions

Q: Why can a single lost TCP segment stall the delivery of data that arrived after it, even though that later data was received correctly?

Answer:
Because TCP must deliver bytes to the application in strict sequence order.

## How the Interviewer Thinks
Interviewers use TCP questions to see whether a candidate understands it as guarantees layered on top of unreliable IP delivery, not just "the reliable protocol," and can explain the real cost (latency, head-of-line blocking).

## Production Engineering Perspective
Engineers monitor retransmission rates and connection counts as key TCP health signals, tune socket buffer sizes and keep-alive settings for high-throughput services, and use `netstat`/`ss` and packet captures to diagnose issues.

## Common Misconceptions
- "TCP guarantees fast delivery." It only guarantees reliable, ordered delivery.

## Revision Notes
- Connection-oriented, reliable, ordered byte stream on top of unreliable IP.
- Sequence numbers + acknowledgments detect loss.

## Summary
TCP builds reliable, ordered, byte-stream delivery on top of IP's best-effort packets, using sequence numbers, acknowledgments, and sliding windows to detect and recover from loss automatically.

---

# UDP

## What is it?
A connectionless, unreliable, unordered transport-layer protocol that sends independent datagrams with minimal overhead and no built-in delivery guarantees.

## Why do we need it?
Some applications (live video, gaming, DNS lookups) need low latency and minimal overhead far more than guaranteed delivery or ordering, and TCP's reliability machinery would only add unwanted delay.

## What problem does it solve?
It solves the need for a lightweight, low-latency transport option by skipping connection setup, acknowledgments, retransmission, and ordering, leaving the application free to implement only the guarantees it actually needs.

## History (only if useful)
Defined by David P. Reed in RFC 768 (1980) as a minimal, nearly direct exposure of IP's own best-effort delivery model to applications.

## How does it work?
A sender simply addresses a datagram to a destination IP and port and transmits it.

## Internal Working
Each UDP datagram carries only a minimal 8-byte header (source port, destination port, length, checksum), far smaller than TCP's 20+ byte header, and the OS delivers each datagram independently with no session state.

## Step-by-Step Example
1. A DNS client sends a single UDP datagram containing a query to a DNS
   resolver's port 53.
2. The resolver processes the query and sends a single UDP datagram back
   with the answer.

## Visualization
```text
 Client                                  Server
   |------ datagram (query) ------------->|
   |<----- datagram (response) -----------|
   (no handshake, no acknowledgment, no guaranteed delivery)
```

## Real-World Usage
DNS queries, video/voice calls (RTP), online gaming, DHCP, and any protocol layered on QUIC (which itself runs over UDP) all rely on UDP's low overhead and lack of ordering guarantees.

## Advantages
- Minimal header overhead and no handshake, minimizing latency.
- No head-of-line blocking.

## Disadvantages
- No guarantee of delivery, ordering, or duplicate suppression.
- Applications needing reliability must implement it themselves.

## Trade-offs
UDP trades away all delivery guarantees for minimal latency and overhead.

## Alternatives
- TCP: full reliability and ordering, at higher latency and overhead.
- QUIC: UDP-based but adds reliability, ordering per stream, and encryption without TCP's head-of-line blocking.

## Performance Analysis
Because there is no handshake or acknowledgment wait, UDP has the lowest possible transport-layer latency for a single datagram.

## Complexity Analysis (if applicable)
Sending or receiving a datagram is O(1) with no per-connection state to maintain, in contrast to TCP's O(1) but stateful per-connection tracking.

## Implementation
```java
DatagramSocket socket = new DatagramSocket();
byte[] data = "ping".getBytes();
DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("example.com"), 9999);
socket.send(packet);
```

## Common Mistakes
- Assuming UDP is inherently insecure or unreliable in every scenario, without considering that many real protocols add reliability atop it (QUIC, custom retry logic).
- Not implementing any timeout/retry logic in applications where a lost datagram matters.

## Common Interview Questions

Q: What are the main differences between UDP and TCP?

Answer:
UDP is connectionless, with a minimal header, no handshake, and no delivery, ordering, or duplicate guarantees, while TCP is connection-oriented and reliable at the cost of handshake overhead.

## Follow-up Interview Questions

Q: Why is UDP a common vector for amplification-based denial-of-service attacks?

Answer:
Because UDP has no handshake to verify sender identity, an attacker can spoof a victim's IP in a small request to a service replying with much more data (e.g., DNS, NTP), flooding the victim.

## How the Interviewer Thinks
Interviewers use UDP questions to check whether a candidate can identify concrete scenarios (DNS, video, gaming) where giving up reliability is the right engineering trade-off, not just recite "UDP is unreliable."

## Production Engineering Perspective
Engineers building on UDP typically add their own minimal reliability (sequence numbers, acknowledgments, retries) only where needed, and must defend exposed UDP services against spoofed-source amplification abuse.

## Common Misconceptions
- "UDP is only useful for unimportant data." It underlies DNS and QUIC, both critical to modern internet performance and reliability.

## Revision Notes
- Connectionless, no delivery/ordering guarantees, minimal 8-byte header.
- No handshake, no acknowledgment, no retransmission built in.

## Summary
UDP strips away every guarantee TCP provides in exchange for minimal latency and overhead.

---

# Three-Way Handshake

## What is it?
The three-step SYN, SYN-ACK, ACK exchange TCP uses to establish a connection between a client and server before any application data is sent.

## Why do we need it?
Both sides need to confirm the other is reachable and willing to communicate, and both need to agree on starting sequence numbers before either can reliably detect lost or duplicate data.

## What problem does it solve?
It solves mutual connection establishment over an unreliable network: without it, a sender couldn't know whether a server is listening, and neither side would agree on a starting point for sequence numbering.

## History (only if useful)
Defined alongside TCP in RFC 793 (1981), with security-motivated refinements (randomized initial sequence numbers, SYN cookies) added later after real attacks like SYN flooding were discovered in the 1990s.

## How does it work?
The client sends a SYN segment with a random initial sequence number.

## Internal Working
The server tracks each received SYN as a "half-open" connection in its SYN backlog queue until the final ACK arrives.

## Step-by-Step Example
1. Client sends `SYN, seq=X` to the server.
2. Server replies `SYN-ACK, seq=Y, ack=X+1`, entering a half-open state
   in its backlog.

## Visualization
```text
 Client                                   Server
   |-------------- SYN, seq=X ------------>|
   |<-------- SYN-ACK, seq=Y, ack=X+1 ------|
   |-------------- ACK, ack=Y+1 ----------->|
   |========= connection established ======|
```

## Real-World Usage
Every TCP connection, from a browser loading a webpage to a database client connecting to its server, begins with this exact three-step exchange before any data flows.

## Advantages
- Confirms mutual reachability and willingness to communicate before committing resources to data transfer.
- Randomized sequence numbers make blind connection hijacking and spoofing much harder.

## Disadvantages
- Adds a full round trip of latency before any application data can be sent.
- The SYN backlog is a finite resource that can be exhausted by a flood of SYNs.

## Trade-offs
The extra round trip and backlog resource cost are traded for the ability to reliably detect connection establishment and defend against naive sequence-number guessing attacks.

## Alternatives
- TCP Fast Open (TFO): allows data in the initial SYN for previously-seen clients, saving a round trip.
- QUIC's 0-RTT/1-RTT handshake: combines connection setup and cryptographic handshake into fewer round trips than TCP+TLS separately.

## Performance Analysis
The handshake adds exactly one round-trip time of latency before any data can be exchanged.

## Complexity Analysis (if applicable)
Each half-open connection is tracked as an O(1) backlog entry.

## Implementation
```text
$ sysctl net.ipv4.tcp_max_syn_backlog
$ sysctl net.ipv4.tcp_syncookies
```

## Common Mistakes
- Assuming the handshake adds negligible latency regardless of network distance.
- Not tuning SYN backlog size or enabling SYN cookies on internet-facing servers vulnerable to SYN flood attacks.

## Common Interview Questions

Q: Walk through the three steps of the TCP three-way handshake.

Answer:
The client sends a SYN with an initial sequence number; the server replies with a SYN-ACK acknowledging it and offering its own sequence number; the client then sends an ACK, completing the handshake.

## Follow-up Interview Questions

Q: Why are TCP initial sequence numbers randomized instead of starting from a fixed value?

Answer:
If sequence numbers were predictable, an attacker could spoof packets appearing to come from a trusted host, hijacking or injecting data without ever seeing the real traffic.

## How the Interviewer Thinks
Interviewers use handshake questions to verify a candidate can name each step and its sequence-number semantics, and probe whether they understand real attacks (SYN flooding) and defenses (SYN cookies).

## Production Engineering Perspective
Production engineers tune SYN backlog size and enable SYN cookies on internet-facing services to withstand SYN flood attacks, and monitor handshake completion rates as an early signal of network problems or attacks.

## Common Misconceptions
- "The handshake is free or instantaneous." It always costs at least one full round trip before data can flow.

## Revision Notes
- SYN -> SYN-ACK -> ACK, exactly three segments, one round trip before data flows.
- Initial sequence numbers are randomized for security, not sequential from zero.

## Summary
The three-way handshake is the minimum exchange needed for two hosts to confirm mutual reachability and agree on starting sequence numbers before trusting each other with data.

---

# Connection Termination

## What is it?
The process by which a TCP connection is gracefully closed, typically through a four-step FIN/ACK exchange that lets both sides finish sending data before the connection fully closes.

## Why do we need it?
Both sides need a reliable way to signal "I have no more data to send" independently.

## What problem does it solve?
It solves graceful, orderly connection shutdown: without a defined termination sequence, one side could stop sending while unaware the other side still had unacknowledged data in flight, risking silent data loss.

## History (only if useful)
Specified alongside the rest of TCP in RFC 793 (1981).

## How does it work?
Either side can initiate closure by sending a FIN.

## Internal Working
Because TCP connections are full-duplex, closing is directional: a FIN only closes the sender's outbound direction, and the connection isn't fully closed until both sides have sent and acknowledged a FIN.

## Step-by-Step Example
1. Client sends `FIN` after finishing its data.
2. Server acknowledges with `ACK`, and may continue sending its own
   remaining data.

## Visualization
```text
 Client                                   Server
   |----------------- FIN ---------------->|
   |<---------------- ACK ------------------|
   |<----------------- FIN -----------------|
   |----------------- ACK ------------------>|
   (client enters TIME_WAIT before fully closing)
```

## Real-World Usage
Every graceful TCP connection close (finishing an HTTP keep-alive connection, a database session ending cleanly) uses this four-step sequence rather than an abrupt reset.

## Advantages
- Ensures both directions of data are fully delivered before the connection closes.
- TIME_WAIT absorbs delayed/duplicate packets, preventing them from corrupting a future connection reusing the same port pair.

## Disadvantages
- TIME_WAIT sockets consume kernel resources until they expire.
- Servers handling very high connection churn can exhaust available ephemeral ports due to accumulated TIME_WAIT sockets.

## Trade-offs
The graceful four-way close trades a bit of extra time and held resources (TIME_WAIT) for the guarantee that no in-flight or duplicate data corrupts a future connection.

## Alternatives
- Abrupt RST (reset): immediately terminates a connection without a graceful handshake, used for errors or forceful termination rather than clean shutdown.
- Connection pooling/keep-alive: avoids frequent termination entirely by reusing established connections.

## Performance Analysis
Graceful termination itself adds two round trips (FIN/ACK in each direction) beyond the last data exchange.

## Complexity Analysis (if applicable)
Each connection's termination state machine transition is O(1).

## Implementation
```text
$ ss -tan state time-wait | wc -l
$ sysctl net.ipv4.tcp_fin_timeout
```

## Common Mistakes
- Not reusing connections (via keep-alive/pooling), causing excessive TIME_WAIT accumulation under high request rates.
- Confusing an abrupt RST with a graceful FIN-based close when debugging connection issues.

## Common Interview Questions

Q: Walk through the steps of a graceful TCP connection termination.

Answer:
One side sends a FIN and stops sending; the other acknowledges it, finishes any remaining data, then sends its own FIN, which the first side acknowledges to fully close the connection.

## Follow-up Interview Questions

Q: Why can a high-throughput server run into problems from too many connections in TIME_WAIT?

Answer:
Each TIME_WAIT socket holds onto its (local IP, local port, remote IP, remote port) tuple until it expires.

## How the Interviewer Thinks
Interviewers use termination questions to check whether a candidate understands closure as a directional, four-step process (not a single instant event) and can explain why TIME_WAIT exists rather than just naming it.

## Production Engineering Perspective
Engineers monitor TIME_WAIT socket counts on high-throughput servers, tune kernel parameters like `tcp_fin_timeout` cautiously, and prefer connection reuse (keep-alive, pooling) over many short-lived connections to avoid port exhaustion.

## Common Misconceptions
- "Closing a connection releases its resources instantly." The TIME_WAIT state deliberately holds resources for a bounded period afterward.

## Revision Notes
- Four-way close: FIN -> ACK -> FIN -> ACK, since each direction closes independently.
- TIME_WAIT absorbs delayed/duplicate packets before releasing the connection.

## Summary
Connection termination gracefully closes each direction of a full-duplex TCP connection independently via FIN/ACK exchanges, ensuring no in-flight data is lost.

---

# Flow Control

## What is it?
A TCP mechanism that prevents a fast sender from overwhelming a slower receiver by letting the receiver advertise how much buffer space it currently has available.

## Why do we need it?
A fast sender and a receiver with a slow application or small buffer could otherwise let the sender flood the receiver faster than it can process data.

## What problem does it solve?
It solves receiver overload: without it, a sender has no way to know how much data the receiver can accept, and would send either too conservatively (wasting throughput) or too aggressively (overwhelming the buffer).

## History (only if useful)
Part of TCP's original design in RFC 793 (1981), flow control's sliding window mechanism was later extended with window scaling (RFC 1323, 1992) to support larger windows on high-bandwidth, high-latency networks.

## How does it work?
The receiver continuously advertises a "receive window" value in every acknowledgment, indicating how many additional bytes it can currently buffer.

## Internal Working
The receive window shrinks as data arrives faster than the application reads it, and grows again as the application consumes buffered data, giving the sender real-time backpressure signaling.

## Step-by-Step Example
1. Receiver advertises a window of 4096 bytes in its initial ACK.
2. Sender sends up to 4096 bytes of unacknowledged data, then pauses.

## Visualization
```text
 Sender                                   Receiver (buffer: 4096 bytes free)
   |------ up to 4096 bytes unacked ------>|
   |<---------- ACK, window=0 -------------|   (receiver buffer now full)
   |------------- window probe ----------->|
   |<---------- ACK, window=2048 ----------|   (app read some data)
```

## Real-World Usage
Operates transparently in every TCP connection.

## Advantages
- Automatically prevents receiver buffer overflow without application involvement.
- Adapts dynamically as the receiving application's consumption speed changes.

## Disadvantages
- A consistently small advertised window can bottleneck throughput even when the network itself has capacity.
- Without window scaling, the classic 16-bit window field caps throughput on high-bandwidth-delay-product links.

## Trade-offs
Flow control trades some throughput (by capping in-flight data to the receiver's current capacity) for the guarantee that the receiver is never overwhelmed.

## Alternatives
- Application-level backpressure (like reactive streams): explicit signaling above TCP, used when finer control than TCP's window is needed.
- No flow control (as in raw UDP): leaves the receiver's protection entirely up to the application.

## Performance Analysis
On high-bandwidth, high-latency links, an insufficiently large advertised window (without window scaling) can cap throughput far below the link's actual capacity.

## Complexity Analysis (if applicable)
Updating and checking the advertised window is an O(1) operation per acknowledgment.

## Implementation
```text
$ sysctl net.ipv4.tcp_window_scaling
$ ss -i   # shows negotiated window scale factor per connection
```

## Common Mistakes
- Assuming flow control and congestion control are the same mechanism.
- Not enabling or accounting for window scaling on high-bandwidth-delay- product links, capping achievable throughput.

## Common Interview Questions

Q: What problem does TCP flow control solve, and how does it solve it?

Answer:
It prevents a fast sender from overwhelming a slower receiver: the receiver continuously advertises its available buffer space, and the sender keeps unacknowledged data within that advertised window.

## Follow-up Interview Questions

Q: How is flow control different from congestion control, even though both limit data in flight?

Answer:
Flow control is receiver-driven, protecting the receiver's buffer via its advertised window, while congestion control is sender-driven, protecting the network path via the sender's own capacity estimate.

## How the Interviewer Thinks
Interviewers use flow control questions specifically to test whether a candidate can clearly distinguish it from congestion control.

## Production Engineering Perspective
Engineers tune socket buffer sizes and ensure window scaling is enabled for high-throughput services on high-bandwidth-delay-product paths (like cross-region replication).

## Common Misconceptions
- "Flow control and congestion control are the same thing." Flow control protects the receiver.

## Revision Notes
- Receiver advertises a window.
- Zero window pauses the sender.

## Summary
Flow control lets a TCP receiver continuously tell the sender exactly how much data it can currently accept, preventing buffer overflow without any application-level intervention.

---

# Congestion Control

## What is it?
A set of TCP algorithms that let a sender estimate how much data the network path itself can currently handle, and throttle transmission to avoid overwhelming routers and links along the way.

## Why do we need it?
Unlike flow control, which protects a single receiver's buffer, nothing else in TCP inherently protects shared network infrastructure from being overwhelmed if every sender transmitted as fast as its receiver's window allowed.

## What problem does it solve?
It solves shared network resource contention: without it, multiple senders competing for the same limited bandwidth would overload routers, causing cascading packet loss and retransmissions that make congestion worse.

## History (only if useful)
Van Jacobson introduced the original TCP congestion control algorithms in 1988 after that 1986 collapse.

## How does it work?
The sender maintains a congestion window: it starts small and grows as acknowledgments confirm delivery (slow start), switches to slower linear growth past a threshold (congestion avoidance), and shrinks sharply on loss.

## Internal Working
Slow start doubles the congestion window each round trip until reaching a threshold or detecting loss.

## Step-by-Step Example
1. Connection starts with a small congestion window (e.g., 10 segments)
   in slow start.
2. Window doubles each round trip as acknowledgments arrive successfully.

## Visualization
```text
 cwnd
   |                     /\    (loss detected, window cut)
   |                 /\ /  \
   |             /\ /  \    \___
   |         /\ /  \
   |     /\ /  (congestion avoidance: linear growth)
   |    /  (slow start: exponential growth)
   +--------------------------------------------------> time
```

## Real-World Usage
Every TCP connection on the internet runs some congestion control algorithm.

## Advantages
- Prevents individual connections from overwhelming shared network infrastructure.
- Automatically adapts to changing network conditions without manual tuning.

## Disadvantages
- Loss-based algorithms (Reno, CUBIC) can misinterpret non-congestion loss (like Wi-Fi interference) as congestion, unnecessarily throttling throughput.
- Slow start's exponential growth can still momentarily overshoot available capacity before backing off.

## Trade-offs
Loss-based algorithms are simple and widely deployed but react only after congestion has already caused a loss.

## Alternatives
- Reno/CUBIC (loss-based): simpler, widely deployed, react to observed packet loss.
- BBR (model-based): estimates actual available bandwidth and round-trip time to avoid relying on loss as a signal.

## Performance Analysis
Congestion control directly determines achievable throughput on lossy or long-distance links: a purely loss-based algorithm on a link with non-congestion packet loss (satellite, Wi-Fi) can throttle throughput far below what BBR-style bandwidth estimation allows.

## Complexity Analysis (if applicable)
Congestion window adjustments are O(1) computations per acknowledgment or loss event.

## Implementation
```text
$ sysctl net.ipv4.tcp_congestion_control
$ sysctl -w net.ipv4.tcp_congestion_control=bbr
```

## Common Mistakes
- Confusing congestion control (protects the network) with flow control (protects the receiver).
- Assuming all packet loss indicates congestion, when it can also be caused by unrelated transmission errors.

## Common Interview Questions

Q: What is the difference between slow start and congestion avoidance?

Answer:
Slow start grows the window exponentially each round trip until a threshold or loss, while congestion avoidance then grows it more conservatively (roughly linearly) to probe for capacity safely.

## Follow-up Interview Questions

Q: Why can loss-based congestion control algorithms like CUBIC perform poorly on networks like Wi-Fi or satellite links?

Answer:
These algorithms treat any packet loss as congestion, but Wi-Fi and satellite links often lose packets from interference, needlessly throttling throughput on a path that still had spare capacity.

## How the Interviewer Thinks
Interviewers use congestion control questions to see whether a candidate can separate it from flow control and explain the real motivation (protecting shared network capacity) rather than just naming "slow start."

## Production Engineering Perspective
Engineers select and tune congestion control algorithms (like switching to BBR) for services sensitive to throughput on lossy or long-distance links, monitoring retransmission rates and throughput to detect a poor algorithm fit.

## Common Misconceptions
- "Congestion control and flow control are the same mechanism." Congestion control protects the shared network.

## Revision Notes
- Protects the shared network, not the receiver (that's flow control's job).
- Slow start = exponential growth.

## Summary
Congestion control lets each TCP sender estimate and respect the shared network's actual capacity, preventing the kind of cascading collapse the internet experienced in 1986.

---

# DNS

## What is it?
The Domain Name System is a globally distributed, hierarchical naming service that translates human-readable domain names into IP addresses (and other records).

## Why do we need it?
Humans work far better with memorable names than raw numeric IP addresses, and those underlying addresses can change over time.

## What problem does it solve?
It solves human-usable naming and location independence: without it, every service would need a memorized, fixed IP address, and changing a server's IP would break every client that had it hardcoded.

## History (only if useful)
Introduced in RFC 882/883 (1983) to replace a single centrally-maintained hosts file that could no longer scale with the growing ARPANET, DNS's hierarchical, delegated design let it scale to today's internet.

## How does it work?
A resolver walks the domain hierarchy right to left: root servers (which know the TLD servers), then TLD servers (which know the domain's authoritative servers), then the authoritative server itself.

## Internal Working
Most lookups are accelerated by caching at multiple levels (OS, local recursive resolver, ISP), governed by a Time-To-Live value on each record that bounds how long it may be cached.

## Step-by-Step Example
1. A resolver asks a root server who is authoritative for `.com`.
2. The root server refers it to the `.com` TLD servers.

## Visualization
```text
 Client -> Recursive Resolver -> Root Server ("go ask .com TLD")
                              -> TLD Server ("go ask example.com's NS")
                              -> Authoritative NS ("A record = 93.184.216.34")
```

## Real-World Usage
Every web request, email delivery, and API call begins with a DNS lookup.

## Advantages
- Decouples human-usable names from changeable underlying IP addresses.
- Hierarchical, delegated design scales globally without central coordination.

## Disadvantages
- Cache staleness means DNS changes can take time (up to a record's TTL) to fully propagate.
- Traditional DNS queries are unencrypted, exposing which domains a user is looking up.

## Trade-offs
Longer TTLs reduce lookup load and latency but slow down how quickly changes (like failover to a new IP) propagate.

## Alternatives
- Hardcoded IP addresses: avoids lookup latency entirely but breaks immediately if the address changes.
- Service discovery systems (like Consul, etcd-backed discovery): used within private networks for more dynamic, fine-grained service location.

## Performance Analysis
A cold, uncached lookup can take tens to hundreds of milliseconds across multiple round trips through the resolution hierarchy.

## Complexity Analysis (if applicable)
Resolution is effectively O(depth of the domain hierarchy) round trips in the uncached worst case, typically a small constant (root, TLD, authoritative), and O(1) for any cached result.

## Implementation
```java
InetAddress address = InetAddress.getByName("example.com");
System.out.println(address.getHostAddress());
```
```text
$ dig example.com A
```

## Common Mistakes
- Setting excessively long TTLs on records that may need to change quickly (like during a failover).
- Not accounting for DNS propagation delay when planning a cutover or migration.

## Common Interview Questions

Q: Walk through what happens when you type a domain name and DNS resolution begins.

Answer:
A recursive resolver queries a root server for the TLD servers, then those for the domain's authoritative nameservers, then those for the record itself, caching it per its TTL.

## Follow-up Interview Questions

Q: Why can a DNS change still cause errors for some users well after it is made?

Answer:
Because DNS records are cached at multiple layers, including the operating system, local resolvers, and ISP resolvers, based on the record's TTL.

## How the Interviewer Thinks
Interviewers use DNS questions to check whether a candidate understands it as a caching, hierarchical, delegated lookup system with real propagation delay implications, not just "the thing that turns names into IPs."

## Production Engineering Perspective
Engineers lower TTLs proactively before a planned migration to reduce propagation delay, monitor DNS query latency and error rates as an early signal of resolver issues, and increasingly adopt DNS-over-HTTPS/TLS for privacy.

## Common Misconceptions
- "DNS changes take effect instantly everywhere." Cached records at various layers persist until their TTL expires.

## Revision Notes
- Hierarchical, delegated resolution: root -> TLD -> authoritative nameserver.
- Common record types: A, AAAA, CNAME, MX, TXT, NS.

## Summary
DNS provides the internet's naming layer through a hierarchical, cached, delegated lookup system that lets human-readable names map to changeable IP addresses at global scale.

---

# ARP

## What is it?
The Address Resolution Protocol maps a known IP address to the corresponding MAC (hardware) address of a device on the same local network segment.

## Why do we need it?
IP addresses are used for logical, routable addressing across networks, but delivering a frame on a local physical network (like Ethernet) requires the destination's actual hardware address.

## What problem does it solve?
It solves local-network address translation: without it, a host knowing only another device's IP address would have no way to construct a properly addressed Ethernet frame to deliver data on the local network.

## History (only if useful)
Defined in RFC 826 (1982), ARP was designed for early Ethernet-based local networks and remains fundamentally unchanged today, though IPv6 replaces it with the more secure Neighbor Discovery Protocol.

## How does it work?
A host broadcasts an ARP request ("who has this IP address?") to every device on the local network segment.

## Internal Working
Resolved mappings are stored in a local ARP cache with a limited lifetime, avoiding a fresh broadcast for every packet.

## Step-by-Step Example
1. Host A wants to send a packet to `192.168.1.10` but doesn't know its
   MAC address.
2. Host A broadcasts an ARP request to the entire local network.

## Visualization
```text
 Host A                    (broadcast)                 All hosts on LAN
   |--- "Who has 192.168.1.10? Tell A" ------------------->|
   |<---------- "192.168.1.10 is at MAC xx:xx" -------------|  (only .10 replies)
```

## Real-World Usage
Runs constantly, transparently, underneath every IP communication on Ethernet and Wi-Fi local networks, resolving next-hop addresses for both end hosts and routers.

## Advantages
- Simple, lightweight mechanism requiring no configuration or central coordination.
- Caching makes repeated communication with the same local host fast after the first lookup.

## Disadvantages
- No built-in authentication, making it vulnerable to spoofing.
- Broadcast-based requests add minor overhead on the local network segment.

## Trade-offs
ARP's simplicity and lack of authentication were reasonable for small, trusted 1980s local networks, but the same simplicity is now a security liability requiring mitigations like Dynamic ARP Inspection.

## Alternatives
- Neighbor Discovery Protocol (NDP): IPv6's ARP replacement, adds a measure of security and uses multicast instead of broadcast.
- Static ARP entries: manually configured mappings, avoiding dynamic resolution at the cost of maintenance overhead, used in highly security-sensitive setups.

## Performance Analysis
ARP resolution itself is fast and cached, adding negligible overhead after the first lookup for a given host.

## Complexity Analysis (if applicable)
Lookup in the local ARP cache is O(1).

## Implementation
```text
$ arp -a
$ ip neigh show
```

## Common Mistakes
- Assuming ARP has any authentication, when in fact it will accept unsolicited replies from anyone on the local segment.
- Not enabling switch-level protections (Dynamic ARP Inspection) on networks where ARP spoofing is a realistic threat.

## Common Interview Questions

Q: What problem does ARP solve, and at what layer does it operate?

Answer:
It resolves a known IP address to the corresponding MAC address needed to deliver a frame on the local network, operating essentially between the Network and Data Link layers.

## Follow-up Interview Questions

Q: How can a network defend against ARP spoofing given that the protocol itself has no authentication?

Answer:
Defenses are typically implemented at the switch level rather than within ARP itself, such as Dynamic ARP Inspection.

## How the Interviewer Thinks
Interviewers use ARP questions to check whether a candidate understands it as a local-network resolution mechanism distinct from DNS, and can name a concrete security weakness (spoofing) and mitigation.

## Production Engineering Perspective
Network engineers enable switch-level protections like Dynamic ARP Inspection on security-sensitive networks, and diagnose local connectivity issues (duplicate IPs, MAC flapping) using tools like `arp -a` or `ip neigh`.

## Common Misconceptions
- "ARP and DNS solve the same problem." ARP resolves IP-to-MAC on a local network.

## Revision Notes
- Resolves IP address to MAC address on a local network segment.
- Broadcast request, unicast reply, cached locally with a limited lifetime.

## Summary
ARP bridges IP's logical addressing to the physical MAC addressing needed for local network delivery, using a simple broadcast-request, unicast- reply exchange.

---

# NAT

## What is it?
A technique that rewrites the source (and sometimes destination) IP address and port of packets at a network boundary, most commonly translating many private addresses to one shared public address.

## Why do we need it?
IPv4 provides roughly 4.3 billion addresses, far fewer than the number of connected devices.

## What problem does it solve?
IPv4 address exhaustion, pragmatically, without requiring every device to have a globally unique public address, and as a side effect provides an implicit boundary against unsolicited inbound connections.

## History (only if useful)
Formalized in RFC 1631 (1994) as a short-term measure to extend IPv4's lifetime while IPv6 was developed.

## How does it work?
When an internal device sends a packet outward, the NAT device rewrites its private source IP/port to its own public IP and a chosen public port, recording the mapping in a translation table.

## Internal Working
Port Address Translation (PAT/NAT overload) lets many internal (IP, port) pairs share one public IP by also distinguishing them by public port.

## Step-by-Step Example
1. Internal host `10.0.0.5:51000` sends a packet to `93.184.216.34:443`.
2. The gateway rewrites the source to `203.0.113.9:40001` and records the
   mapping.

## Visualization
```text
 Internal host        NAT Gateway (translation table)              Internet
 10.0.0.5:51000 ---> [10.0.0.5:51000 <-> 203.0.113.9:40001] ---> 93.184.216.34:443
 10.0.0.5:51000 <---  [rewrite dst back to 10.0.0.5:51000]  <--- 93.184.216.34:443
```

## Real-World Usage
Runs on nearly every home router and corporate network, and as cloud NAT gateways letting private VPC subnets reach the internet outbound without exposing instances directly.

## Advantages
- Conserves scarce public IPv4 addresses across many devices.
- Provides an incidental security boundary against unsolicited inbound connections.

## Disadvantages
- Breaks true end-to-end connectivity, complicating protocols needing unsolicited inbound connections.
- Adds a stateful table that must scale with connection count.

## Trade-offs
Trades transparent, direct end-to-end addressing for address conservation and an incidental security boundary.

## Alternatives
- IPv6: enough address space that every device can have a public address.
- NAT64/DNS64: transitional techniques for IPv6-only networks reaching IPv4-only destinations.

## Performance Analysis
Lookups are fast, but the translation table is finite: a busy NAT gateway can exhaust available (public IP, port) combinations or connection-tracking capacity, a common scaling limit under high outbound churn.

## Complexity Analysis (if applicable)
Each packet's translation is an O(1) hash table lookup.

## Implementation
```text
$ iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
```

## Common Mistakes
- Treating NAT as a security feature by design rather than an incidental side effect.
- Not accounting for port/table exhaustion under many short-lived outbound connections.

## Common Interview Questions

Q: Why does NAT exist?

Answer:
Primarily to let many devices with private IPs share a small number of public IPv4 addresses, while incidentally also blocking unsolicited inbound connections to internal devices.

## Follow-up Interview Questions

Q: Why can a cloud NAT gateway run out of capacity even though the organization has plenty of application-level resources?

Answer:
NAT capacity is bounded by available (public IP, port) combinations and connection-tracking table size, not CPU or memory.

## How the Interviewer Thinks
NAT questions test whether a candidate treats it as an address-sharing mechanism with real operational limits (table capacity, port exhaustion) rather than a vague "security thing."

## Production Engineering Perspective
Cloud engineers monitor NAT gateway connection counts and port utilization, alerting well before exhaustion, and provision additional NAT gateways or public IPs proactively for high-connection-churn workloads.

## Common Misconceptions
- "NAT is a firewall." It incidentally blocks unsolicited inbound traffic but does not inspect payloads or enforce policy.

## Revision Notes
- Rewrites (IP, port) at a network boundary.
- Motivated by IPv4 exhaustion (RFC 1631, 1994).

## Summary
NAT stretches a scarce pool of public IPv4 addresses across many private devices by rewriting IP/port headers at a boundary, incidentally gaining a security-like property.

---

# HTTP

## What is it?
An application-layer, stateless request/response protocol used to exchange structured messages, most commonly between a browser or client and a web server.

## Why do we need it?
Applications need a standardized, interoperable way to request and exchange resources over a network.

## What problem does it solve?
It solves interoperability for retrieving and submitting structured data (documents, JSON, forms) over an unreliable network, using a simple, text-based (in HTTP/1.1) request/response model that any client or server can implement independently.

## History (only if useful)
HTTP/0.9 (1991) was a one-line protocol.

## How does it work?
A client sends a request line (method, path, version), headers, and an optional body.

## Internal Working
HTTP/1.1 reuses one TCP connection for multiple sequential requests (keep-alive) but processes them one at a time per connection unless pipelined.

## Step-by-Step Example
1. Client sends `GET /orders HTTP/1.1` with a `Host` header over an
   established TCP connection.
2. Server parses the request, looks up the resource, and prepares a
   response.

## Visualization
```text
 Request                         Response
 GET /orders HTTP/1.1             HTTP/1.1 200 OK
 Host: api.example.com            Content-Type: application/json
 Accept: application/json         Content-Length: 27
 (blank line)                     (blank line)
                                  {"id": 1, "status": "ok"}
```

## Real-World Usage
Powers virtually all web traffic and REST APIs.

## Advantages
- Simple, human-readable in its 1.1 text form, universally supported.
- Stateless design scales easily across many servers.

## Disadvantages
- Statelessness requires extra mechanisms (cookies, tokens) for session state.
- HTTP/1.1 suffers head-of-line blocking without pipelining in practice.

## Trade-offs
Statelessness trades built-in session continuity for horizontal scalability.

## Alternatives
- gRPC: binary, contract-first, better suited to internal service-to-service calls.
- WebSockets: persistent, full-duplex for real-time use cases.

## Performance Analysis
Latency is dominated by round trips: connection setup, TLS handshake, and the request/response itself.

## Complexity Analysis (if applicable)
Parsing overhead is proportional to header size.

## Implementation
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.example.com/orders")).GET().build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.statusCode());
```

## Common Mistakes
- Not reusing connections, paying handshake cost per request.
- Ignoring caching headers (`Cache-Control`, `ETag`), causing unnecessary refetching.

## Common Interview Questions

Q: Why is HTTP described as a stateless protocol?

Answer:
Each request is processed independently with no memory of prior requests by the protocol itself.

## Follow-up Interview Questions

Q: Why can HTTP/2 still suffer head-of-line blocking despite multiplexing streams?

Answer:
Because all streams share one underlying TCP connection, so a single lost TCP segment blocks delivery of every stream's data until it is retransmitted, even though HTTP/2 already solved head-of-line blocking at the application layer.

## How the Interviewer Thinks
Interviewers check whether a candidate understands HTTP as a protocol with real version-specific trade-offs, not just "GET and POST," and can explain why binary framing and multiplexing were introduced rather than just naming them.

## Production Engineering Perspective
Engineers debug HTTP issues with `curl -v` to see raw requests/headers/status codes, monitor status code distributions (4xx vs 5xx) as health signals, and tune keep-alive/HTTP/2 settings for high-traffic services.

## Common Misconceptions
- "HTTP being stateless means it cannot support login sessions." It can, via cookies or tokens layered above the protocol.

## Revision Notes
- Stateless request/response.
- HTTP/1.1 = text, keep-alive.

## Summary
HTTP is the standardized request/response protocol that powers the web, evolving from a simple text exchange to a multiplexed, binary, increasingly UDP-based protocol while keeping the same request/response semantics.

---

# HTTPS

## What is it?
HTTP layered on top of TLS, providing an encrypted, authenticated channel for the same request/response semantics as plain HTTP.

## Why do we need it?
Plain HTTP sends requests and responses in the clear.

## What problem does it solve?
It solves eavesdropping, tampering, and impersonation: without it, credentials, session tokens, and private data traveling over HTTP are visible to any intermediary, and a man-in-the-middle could silently alter responses.

## History (only if useful)
Netscape introduced SSL in 1994.

## How does it work?
A client opens a TCP connection, performs a TLS handshake to authenticate the server (via its certificate) and derive session keys, then sends the HTTP request and response as encrypted TLS records.

## Internal Working
The detailed cryptographic handshake mechanics belong to the TLS chapter.

## Step-by-Step Example
1. Client connects on port 443 and completes the TCP handshake.
2. Client and server complete a TLS handshake, authenticating the
   server's certificate and deriving session keys.

## Visualization
```text
 +------------------------------+
 |   HTTP (request/response)     |
 +------------------------------+
 |   TLS (encryption, auth)       |
 +------------------------------+
 |   TCP (reliable transport)     |
 +------------------------------+
 |   IP (addressing/routing)      |
 +------------------------------+
```

## Real-World Usage
Virtually all modern web traffic.

## Advantages
- Confidentiality, integrity, and server authentication in one layer.
- Transparent to application logic once established.

## Disadvantages
- Handshake adds latency and CPU cost, though largely mitigated by TLS 1.3 and session resumption.
- Certificate issuance, renewal, and revocation add operational overhead.

## Trade-offs
The security overhead of encryption and certificate management is traded against the near-elimination of plaintext interception risk.

## Alternatives
- Plain HTTP behind a fully trusted private network: rarely recommended given internal threats and compliance requirements.
- mTLS: mutual certificate authentication for service-to-service HTTPS, stronger than server-only authentication.

## Performance Analysis
TLS 1.3 reduces the handshake to one round trip (versus two for TLS 1.2), and session resumption or 0-RTT can eliminate that cost entirely for repeat connections.

## Complexity Analysis (if applicable)
The handshake's asymmetric cryptography is the dominant one-time cost per new connection.

## Implementation
```text
$ openssl s_client -connect example.com:443 -servername example.com
```

## Common Mistakes
- Serving mixed content (HTTP resources on an HTTPS page), triggering browser warnings.
- Letting certificates expire, causing hard failures for all clients.

## Common Interview Questions

Q: What does HTTPS add on top of HTTP?

Answer:
Confidentiality, integrity, and server authentication, by wrapping HTTP requests and responses inside a TLS-encrypted channel established before any HTTP data is sent.

## Follow-up Interview Questions

Q: Does HTTPS fully hide which sites a user is visiting from network observers?

Answer:
Not entirely: the destination IP address, and (without Encrypted Client Hello) the SNI hostname sent in plaintext during the TLS handshake, still reveal which site is being visited even though the request and response content stays encrypted.

## How the Interviewer Thinks
Interviewers check whether a candidate understands HTTPS as HTTP plus TLS, rather than a distinct protocol, and can name real operational concerns like certificate rotation and mixed content beyond just "it's encrypted."

## Production Engineering Perspective
Teams automate certificate issuance and renewal (e.g., ACME/Let's Encrypt), monitor certificate expiry proactively, and decide between terminating TLS at a load balancer versus re-encrypting to backends (or mTLS) per their trust boundary.

## Common Misconceptions
- "HTTPS makes traffic completely anonymous." Metadata like destination IP and SNI hostname can still be visible.

## Revision Notes
- HTTP + TLS.
- Port 443.

## Summary
HTTPS is HTTP carried inside a TLS-encrypted, authenticated channel, turning a plaintext protocol into one safe for credentials and private data.

---

# TLS

## What is it?
Transport Layer Security is a cryptographic protocol that provides confidentiality, integrity, and authentication for data sent over a network, most commonly on top of TCP.

## Why do we need it?
Any protocol built directly on TCP sends data in the clear by default.

## What problem does it solve?
It solves secure channel establishment between parties who have never communicated before, over a network where any intermediary could otherwise read or alter traffic, combining asymmetric cryptography (authenticate, agree a secret) with symmetric cryptography (encrypt efficiently).

## History (only if useful)
Netscape's SSL 1.0-3.0 (1994-1996) evolved into TLS 1.0 (1999), 1.1, and 1.2 (2008, widely used for a decade), culminating in TLS 1.3 (2018).

## How does it work?
The client and server negotiate a cipher suite, the server authenticates via a certificate (validated against a trusted CA chain), both derive session keys via an asymmetric exchange (commonly ECDHE), then encrypt data with a fast symmetric cipher (AES-GCM).

## Internal Working
TLS 1.2 required two round trips before application data could flow.

## Step-by-Step Example
1. Client sends `ClientHello`: supported cipher suites and a key share.
2. Server replies with `ServerHello`, its certificate, and its own key
   share.

## Visualization
```text
 Client                                          Server
   |------------- ClientHello ------------------->|
   |<---- ServerHello, Certificate, KeyShare ------|
   |------------- Finished ----------------------->|
   |<------------ Finished ------------------------|
   |========== Encrypted Application Data =========|
   (TLS 1.3: 1 round trip before application data flows)
```

## Real-World Usage
Secures HTTPS, most modern email transport, VPN tunnels, and service-to-service communication via mutual TLS (mTLS) in microservice meshes.

## Advantages
- Confidentiality, integrity, and authentication in one negotiated layer.
- Forward secrecy with modern cipher suites protects past sessions.

## Disadvantages
- Handshake adds CPU and at least one round trip of latency for new connections.
- Certificate issuance, rotation, and revocation add operational overhead.

## Trade-offs
Stronger security (fresh keys, full certificate validation) is traded against handshake speed.

## Alternatives
- IPsec: network-layer encryption, transparent to applications but harder to manage per-application policy.
- SSH tunneling: application-specific secure channel, common for administrative access.

## Performance Analysis
TLS 1.3's one-round-trip handshake, combined with session resumption and hardware AES acceleration, makes the ongoing cost of encryption negligible for most workloads.

## Complexity Analysis (if applicable)
Asymmetric handshake operations (ECDHE key exchange, certificate signature verification) are computationally the most expensive part, performed once per connection (or session resumption).

## Implementation
```text
$ openssl s_client -connect example.com:443 -tls1_3
```

## Common Mistakes
- Disabling certificate validation "temporarily" for testing and leaving it disabled in production.
- Continuing to support deprecated TLS 1.0/1.1 or weak cipher suites.

## Common Interview Questions

Q: What are the main security properties TLS provides?

Answer:
Confidentiality (encryption), integrity (tamper detection), and authentication (verifying the server's, and optionally the client's, identity via certificates).

## Follow-up Interview Questions

Q: What is forward secrecy, and why does it matter?

Answer:
Forward secrecy means session keys are derived from ephemeral, per-connection key pairs rather than the server's long-term private key, so a future key compromise cannot decrypt past recorded traffic.

## How the Interviewer Thinks
Interviewers check whether a candidate understands TLS as a generic security layer with real cryptographic trade-offs (forward secrecy, 0-RTT replay risk), not just "the thing that makes HTTPS secure."

## Production Engineering Perspective
Teams automate certificate rotation, monitor handshake failure rates and protocol version usage to catch downgrades or expiring certificates, and deliberately choose where TLS terminates (edge load balancer vs. each backend via mTLS).

## Common Misconceptions
- "TLS and SSL are interchangeable and equally safe." SSL is deprecated and insecure.

## Revision Notes
- Confidentiality + integrity + authentication, protocol-agnostic.
- Handshake: ClientHello -> ServerHello/certificate -> Finished.

## Summary
TLS provides a generic, protocol-agnostic way to add encryption, integrity, and authentication to any connection, using asymmetric cryptography once at handshake time and fast symmetric cryptography for the actual data.

---

# Reverse Proxy

## What is it?
A server-side intermediary that receives client requests on behalf of one or more backend servers, forwards them appropriately, and returns the response as if it came from the proxy itself.

## Why do we need it?
Backend services should not need to handle every cross-cutting concern (TLS termination, routing, security filtering) themselves, and clients should not need to know the internal topology behind a single entry point.

## What problem does it solve?
It solves direct client-to-backend coupling: without it, clients would need every backend's address, backends would each need their own TLS certificates and hardening, and scaling a backend would require client-visible changes.

## History (only if useful)
Early web accelerators like Squid provided caching reverse-proxy functionality in the 1990s.

## How does it work?
The proxy accepts a client connection, inspects the request (path, headers, host), selects a backend per configured rules, forwards the request (often over a pooled connection), and relays the backend's response to the client.

## Internal Working
Reverse proxies typically rewrite or add headers like `X-Forwarded-For` (original client IP) and `X-Forwarded-Proto` so backends can recover information otherwise lost when the proxy is the visible peer.

## Step-by-Step Example
1. Client requests `https://app.example.com/orders`.
2. The reverse proxy terminates TLS and reads the request path.

## Visualization
```text
 Client --> [ Reverse Proxy ] --> Backend Pool
                 |     \--> orders-service (3 instances)
                 |     \--> users-service (2 instances)
                 (TLS termination, routing, header rewriting)
```

## Real-World Usage
nginx, HAProxy, and Envoy commonly sit in front of application servers.

## Advantages
- Centralizes TLS termination, caching, and load balancing in one place.
- Hides backend topology and reduces backend attack surface.

## Disadvantages
- Becomes a single point of failure unless deployed redundantly.
- Adds one more network hop and a layer of configuration to maintain.

## Trade-offs
Centralizing cross-cutting concerns simplifies backends but adds an infrastructure dependency that itself needs high availability, monitoring, and careful configuration.

## Alternatives
- Forward proxy: sits in front of clients (not servers), for outbound filtering or anonymization -- the conceptual opposite.
- Direct backend exposure: simpler but pushes TLS, routing, and security onto every backend individually.

## Performance Analysis
Connection pooling to upstreams and TLS session reuse significantly reduce backend load and latency.

## Complexity Analysis (if applicable)
Routing decisions are typically O(1) hash lookups for exact host/path matches or O(log n) for prefix-tree-based path matching, negligible compared to network and backend processing time.

## Implementation
```text
location /orders {
    proxy_pass http://orders_service;
    proxy_set_header X-Forwarded-For $remote_addr;
    proxy_set_header Host $host;
}
```

## Common Mistakes
- Forgetting to forward `X-Forwarded-For`/`Host` headers, breaking backend logic that depends on them.
- Not tuning proxy timeouts, causing premature failures under backend latency spikes.

## Common Interview Questions

Q: What is the difference between a forward proxy and a reverse proxy?

Answer:
A forward proxy acts on behalf of clients, sitting between them and the internet, often for filtering or anonymization, while a reverse proxy acts on behalf of servers, handling routing, TLS, and load distribution for backends.

## Follow-up Interview Questions

Q: Why must a reverse proxy itself be deployed with high availability?

Answer:
Because every client request passes through it, so if it becomes unavailable, every backend behind it becomes unreachable regardless of the backends' own health, making it a single point of failure that itself needs redundancy.

## How the Interviewer Thinks
Interviewers check whether a candidate can clearly distinguish reverse proxies from load balancers and forward proxies (a common point of confusion), and can name concrete operational responsibilities a reverse proxy takes on.

## Production Engineering Perspective
Reverse proxies are a natural chokepoint for rate limiting, request logging, canary/blue-green routing by header or percentage, and centralized observability.

## Common Misconceptions
- "Reverse proxy and forward proxy are the same thing." They serve opposite sides of a connection.

## Revision Notes
- Sits in front of backends.
- Adds/rewrites headers (X-Forwarded-For, Host) for backend visibility.

## Summary
A reverse proxy decouples clients from backend topology by centralizing TLS termination, routing, and cross-cutting concerns in front of a pool of servers.

---

# Load Balancer

## What is it?
A component that distributes incoming traffic across multiple backend instances to improve scalability, availability, and resource utilization.

## Why do we need it?
A single server has finite capacity and represents a single point of failure.

## What problem does it solve?
It solves both scalability (one server cannot handle unlimited traffic) and availability (one server going down should not take down the whole service), by spreading requests across a healthy pool of backends.

## History (only if useful)
Early load balancing used dedicated hardware appliances (F5), followed by flexible software load balancers (HAProxy, nginx), and more recently cloud-managed load balancers (ALB/NLB) and service-mesh-based load balancing.

## How does it work?
The load balancer accepts incoming connections and forwards each to a backend chosen by an algorithm (round robin, least connections, weighted, consistent hashing), while continuously checking backend health to avoid routing to failed instances.

## Internal Working
Layer 4 load balancing forwards packets based on IP/port with minimal inspection, offering high throughput and protocol independence.

## Step-by-Step Example
1. Three backend instances are registered; a health check marks instance
   2 as unhealthy.
2. A new request arrives; the load balancer's round-robin algorithm skips
   instance 2 and sends it to instance 3.

## Visualization
```text
                 +--> Backend 1 (healthy)
 Client --> [LB] +--> Backend 2 (unhealthy, skipped)
                 +--> Backend 3 (healthy)
        (health checks continuously probe each backend)
```

## Real-World Usage
Cloud-managed load balancers (ALB/NLB), HAProxy, and Kubernetes Services/Ingress controllers all implement this pattern.

## Advantages
- Enables horizontal scalability and zero-downtime deployments.
- Removes unhealthy instances automatically, improving availability.

## Disadvantages
- Adds a network hop and another piece of critical infrastructure to keep highly available.
- Sticky sessions (routing a client to the same backend) complicate true statelessness.

## Trade-offs
Layer 4 balancing is fast and protocol-agnostic but routing-blind to content.

## Alternatives
- Client-side load balancing: the client itself picks a backend from a known list (common in gRPC and service meshes).
- DNS round robin: coarse-grained, cache-affected load distribution without a dedicated component.

## Performance Analysis
Round robin and least-connections algorithms add negligible per-request overhead.

## Complexity Analysis (if applicable)
Round robin is O(1) per request.

## Implementation
```text
backend orders_pool
    balance leastconn
    server orders1 10.0.0.5:8080 check
    server orders2 10.0.0.6:8080 check
```

## Common Mistakes
- Not implementing meaningful health checks, routing traffic to instances that are up but broken.
- Ignoring connection draining, dropping in-flight requests during deploys.

## Common Interview Questions

Q: What is the difference between Layer 4 and Layer 7 load balancing?

Answer:
Layer 4 balances based on IP and port with minimal inspection, offering speed and protocol independence, while Layer 7 inspects HTTP-level content (path, headers, cookies) for smarter routing at the cost of more overhead.

## Follow-up Interview Questions

Q: Why can overusing sticky sessions undermine the purpose of a load balancer?

Answer:
Sticky sessions pin a client to one backend for the life of a session, which can create uneven load across backends and loses that client's session state if the pinned backend fails.

## How the Interviewer Thinks
Interviewers check whether a candidate can reason about algorithm choice and health check design as deliberate trade-offs, not just name-drop "round robin," and can connect load balancing to real deployment practices like canary releases.

## Production Engineering Perspective
Load balancers are a key metric surface: 5xx rate, latency percentiles, and per-backend request distribution are monitored continuously, and weighted routing is a common mechanism for canary and blue-green deployments.

## Common Misconceptions
- "A load balancer guarantees perfectly even load at all times." Sticky sessions and uneven traffic patterns can still skew distribution.

## Revision Notes
- Distributes traffic for scalability and availability.
- L4 = fast, protocol-agnostic.

## Summary
A load balancer spreads traffic across backend instances so no single server is a bottleneck or single point of failure, choosing between fast, content-blind Layer 4 routing and smarter, costlier Layer 7 routing.

---

# CDN

## What is it?
A Content Delivery Network is a geographically distributed set of edge servers that cache and serve content closer to end users than a single origin server could.

## Why do we need it?
Physical distance between a user and an origin server directly adds latency, and a single origin can become a bottleneck or single point of failure under global traffic.

## What problem does it solve?
It solves distance-induced latency, origin overload, and regional outages by absorbing traffic at edge locations distributed close to users, serving cached content directly and only contacting the origin when necessary.

## History (only if useful)
Akamai pioneered commercial CDN service in 1998 to handle the web's rapid growth.

## How does it work?
DNS or anycast routing directs a user's request to a nearby edge point of presence (PoP).

## Internal Working
Cache behavior is governed by `Cache-Control` and related headers (or CDN-specific rules) that define the cache key and TTL.

## Step-by-Step Example
1. A user in Mumbai requests an image; DNS/anycast routes them to a
   Mumbai edge PoP.
2. The edge has no cached copy (cache miss) and fetches it from the
   origin in the US.

## Visualization
```text
 User (Mumbai) --> Edge PoP (Mumbai) --cache miss--> Origin (US)
                         |
                    cache hit for
                  subsequent requests
                  from nearby users
```

## Real-World Usage
Cloudflare, Akamai, Fastly, and CloudFront serve static assets, accelerate APIs, stream video, and absorb large-scale DDoS traffic at the edge before it ever reaches the origin.

## Advantages
- Lower latency by serving from a location physically closer to the user.
- Reduces origin load and improves availability during traffic spikes.

## Disadvantages
- Cache invalidation across many distributed edges adds real complexity.
- Misconfigured caching can serve stale or, worse, incorrect personalized content to the wrong user.

## Trade-offs
Higher cache hit ratios improve performance and reduce origin cost but risk staleness.

## Alternatives
- No CDN, serving directly from the origin: simpler but slower globally and less resilient to spikes.
- Multi-region origin deployment: reduces distance without a caching layer, at higher infrastructure cost.

## Performance Analysis
Cache hit ratio is the primary performance metric.

## Complexity Analysis (if applicable)
Cache lookups are O(1) hash-based operations at each edge.

## Implementation
```text
Cache-Control: public, max-age=86400, stale-while-revalidate=60
```

## Common Mistakes
- Caching personalized or authenticated content without proper cache-key scoping.
- Missing or incorrect `Cache-Control` headers, causing over- or under-caching.

## Common Interview Questions

Q: What is a cache hit ratio, and why does it matter for a CDN?

Answer:
It is the fraction of requests served directly from edge cache instead of the origin; a higher ratio means less origin load, lower latency, and lower bandwidth cost.

## Follow-up Interview Questions

Q: Why is cache invalidation considered one of the harder problems in using a CDN effectively?

Answer:
Because a change must propagate across many independent, distributed edge locations, and until it does, different users may see different versions of the same resource.

## How the Interviewer Thinks
Interviewers check whether a candidate understands a CDN as a caching and edge-routing system with real invalidation trade-offs, not simply "a service that makes websites faster."

## Production Engineering Perspective
Teams monitor cache hit ratio as a core metric, use origin shields to protect origins during cold-cache events, and adopt versioned asset URLs so new deploys are inherently cache-safe.

## Common Misconceptions
- "A CDN is only for static files." It also accelerates dynamic and API traffic through various edge techniques.

## Revision Notes
- Distributed edge PoPs cache content close to users, reducing latency and origin load.
- Cache-Control headers and CDN rules define cache key and TTL.

## Summary
A CDN pushes content physically closer to users through distributed edge caching, cutting latency and shielding the origin from load and spikes.

---

# WebSockets

## What is it?
A protocol providing a full-duplex, persistent connection over a single TCP connection, established by upgrading from an initial HTTP request.

## Why do we need it?
Some applications need low-latency, bidirectional communication (chat, live dashboards, trading data) where repeatedly opening new HTTP requests to check for updates is wasteful and slow.

## What problem does it solve?
It solves the inefficiency of polling or long-polling for real-time updates by keeping one connection open in both directions.

## History (only if useful)
Standardized in RFC 6455 (2011), WebSockets grew out of the need for real-time web applications that HTTP's request/response model handled only awkwardly through polling workarounds.

## How does it work?
A client sends a normal HTTP request with an `Upgrade: websocket` header.

## Internal Working
After the upgrade, both sides exchange lightweight frames (not full HTTP messages) that can carry text or binary payloads.

## Step-by-Step Example
1. Client sends `GET /chat HTTP/1.1` with `Upgrade: websocket` and
   `Connection: Upgrade` headers.
2. Server replies `101 Switching Protocols`, confirming the upgrade.

## Visualization
```text
 Client                                   Server
   |---- GET /chat, Upgrade: websocket ----->|
   |<---- 101 Switching Protocols ------------|
   |========= full-duplex framed messages ===|
   |---- message ---------------------------->|
   |<--------------------------- message ----|
```

## Real-World Usage
Chat applications, live dashboards, multiplayer games, and trading platforms that need instant, bidirectional updates without polling overhead.

## Advantages
- Low latency, full-duplex communication over one long-lived connection.
- Much less overhead than repeated HTTP polling for frequent updates.

## Disadvantages
- Stateful, long-lived connections complicate horizontal scaling and load balancing.
- Some proxies and firewalls handle long-lived upgraded connections poorly.

## Trade-offs
WebSockets trade the simplicity and cacheability of stateless HTTP requests for lower latency and reduced overhead on frequent bidirectional updates, at the cost of managing long-lived connection state.

## Alternatives
- Server-Sent Events: simpler, one-way (server-to-client) streaming over plain HTTP.
- Long polling: an HTTP-only workaround with higher latency and overhead than WebSockets.

## Performance Analysis
Eliminates the repeated handshake and header overhead of polling.

## Complexity Analysis (if applicable)
Per-message framing overhead is O(1).

## Implementation
```java
@ServerEndpoint("/chat")
public class ChatEndpoint {
    @OnMessage
    public void onMessage(String message, Session session) {
        session.getAsyncRemote().sendText("echo: " + message);
    }
}
```

## Common Mistakes
- Not implementing client-side reconnection with backoff after a dropped connection.
- Ignoring sticky-session requirements when scaling WebSocket servers behind a load balancer.

## Common Interview Questions

Q: How does a WebSocket connection get established?

Answer:
The client sends a normal HTTP request with an `Upgrade: websocket` header; if the server agrees, it replies `101 Switching Protocols`, and the same TCP connection then carries bidirectional WebSocket frames instead of HTTP.

## Follow-up Interview Questions

Q: What makes scaling WebSocket servers harder than scaling stateless HTTP servers?

Answer:
Each connection is long-lived and stateful on a specific server instance, so scaling requires sticky routing back to that instance or a shared pub/sub backplane to fan out messages, unlike stateless HTTP requests any instance can handle independently.

## How the Interviewer Thinks
Interviewers check whether a candidate recognizes WebSockets as complementary to HTTP for a specific latency/bidirectionality need, and understands the real operational cost of holding many stateful connections open at scale.

## Production Engineering Perspective
Teams monitor open connection counts and per-connection memory usage, tune ping/pong heartbeat intervals to detect dead connections promptly, and use a pub/sub backplane to fan out messages across multiple WebSocket server instances.

## Common Misconceptions
- "WebSockets replace HTTP entirely." They are complementary, typically used alongside regular HTTP APIs for the same application.

## Revision Notes
- Upgrades from HTTP via `101 Switching Protocols`.
- Client frames are masked.

## Summary
WebSockets upgrade a single HTTP connection into a persistent, full-duplex channel, eliminating the overhead of polling for real-time bidirectional communication.

---

# gRPC

## What is it?
A high-performance remote procedure call framework that uses HTTP/2 for transport and Protocol Buffers for a compact, strongly-typed, binary message format.

## Why do we need it?
Internal service-to-service communication benefits from efficient serialization, strict typed contracts, and native support for streaming, none of which plain JSON-over-HTTP provides without extra convention and tooling.

## What problem does it solve?
It solves the verbosity and lack of strict contracts common with REST/JSON for internal microservice communication, and natively supports request/response streaming in either or both directions.

## History (only if useful)
Built on Google's internal RPC system, Stubby, gRPC was open-sourced in 2015 and became a Cloud Native Computing Foundation project, now widely used for microservice-to-microservice communication across many languages.

## How does it work?
A `.proto` file defines services and message types using Protocol Buffers' interface definition language.

## Internal Working
gRPC supports four call types: unary (single request/response), server streaming, client streaming, and bidirectional streaming, all multiplexed as independent HTTP/2 streams over one connection.

## Step-by-Step Example
1. Define a service and message in a `.proto` file.
2. Generate client and server stubs from that file using the protobuf
   compiler.

## Visualization
```text
 Client Stub                 HTTP/2 (multiplexed streams)          Server Stub
  call(Request) ---> [ binary protobuf frame, stream 1 ] ---> handle(Request)
  Response      <--- [ binary protobuf frame, stream 1 ] <--- return Response
                       (other RPCs use other stream IDs concurrently)
```

## Real-World Usage
Widely used for internal microservice communication in polyglot environments, and in infrastructure systems needing efficient, strongly-typed, streaming RPC between services.

## Advantages
- Compact binary payloads and multiplexed streams improve performance over text-based REST.
- Strongly-typed contracts (`.proto` files) catch mismatches at compile time, not runtime.

## Disadvantages
- Not natively browser-friendly.
- Binary payloads are harder to inspect and debug than human-readable JSON.

## Trade-offs
gRPC trades human readability and universal tooling (curl, browser dev tools) for performance and strict type safety.

## Alternatives
- REST/JSON: simpler, universally tooled, better suited for public APIs.
- GraphQL: client-driven flexible queries over a single HTTP endpoint.

## Performance Analysis
Binary protobuf payloads are typically smaller than equivalent JSON, and HTTP/2 multiplexing avoids the connection-per-request overhead of naive REST clients.

## Complexity Analysis (if applicable)
Protobuf encoding and decoding are close to O(n) in message size with low constant overhead.

## Implementation
```text
service OrderService {
  rpc GetOrder (OrderRequest) returns (OrderResponse);
}
message OrderRequest { int32 id = 1; }
message OrderResponse { string status = 1; }
```

## Common Mistakes
- Reusing or renumbering existing protobuf field numbers, breaking wire compatibility.
- Ignoring deadline and cancellation propagation across chained service calls.

## Common Interview Questions

Q: Why does gRPC use HTTP/2 instead of HTTP/1.1?

Answer:
HTTP/2's multiplexed streams let many concurrent RPC calls share one connection without head-of-line blocking, and its binary framing pairs naturally with protobuf's encoding, unlike HTTP/1.1.

## Follow-up Interview Questions

Q: Why is gRPC generally a poor fit for public, browser-facing APIs?

Answer:
Browsers cannot natively originate HTTP/2 trailer-based gRPC calls without a translating proxy like grpc-web, and gRPC's binary payloads are harder to inspect than plain JSON.

## How the Interviewer Thinks
Interviewers check whether a candidate can articulate when gRPC's performance and type-safety benefits are worth its tooling and readability costs, rather than treating it as a universal REST replacement.

## Production Engineering Perspective
Teams propagate deadlines across chained gRPC calls to prevent cascading slow requests, use interceptors for tracing and authentication, and rely on client-side or service-mesh load balancing, since persistent HTTP/2 connections behave differently than HTTP/1.1.

## Common Misconceptions
- "gRPC should replace REST everywhere." It is a poor fit for public browser-facing APIs without additional tooling.

## Revision Notes
- HTTP/2 transport + Protocol Buffers binary encoding + code-generated typed stubs.
- Four call types: unary, server streaming, client streaming, bidirectional streaming.

## Summary
gRPC pairs HTTP/2's multiplexed transport with Protocol Buffers' compact binary encoding and generated typed stubs.
