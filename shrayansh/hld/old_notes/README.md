# High-Level Design Notes

Optimized notes for HLD / system design interviews, reorganized from [shrayansh/hld/](../shrayansh/hld/).

## Fundamentals
1. [Network Protocols](.01-network-protocols.md) — OSI model, TCP vs UDP, WebSockets, WebRTC.
2. [CAP Theorem & Consistency](.02-cap-theorem-and-consistency.md) — CAP trade-offs and consistency models.
3. [Microservices & Patterns](.03-microservices.md) — decomposition, SAGA, CQRS, Strangler.
4. [Scaling 0 → 1 Million](.04-scaling-0-to-million.md) — progressive architecture evolution.
5. [Consistent Hashing](.05-consistent-hashing.md) — hash rings, virtual nodes.

## Building Blocks
6. [URL Shortener](.06-url-shortener.md) — Base62, Snowflake, ZooKeeper ranges.
7. [Back-of-Envelope Estimation](.07-back-of-envelope-estimation.md) — sizing QPS, storage, RAM.
8. [Key-Value Database](.08-key-value-db.md) — DynamoDB-style: quorum, vector clocks, Merkle trees.
9. [SQL vs NoSQL](.09-sql-vs-nosql.md) — ACID vs BASE, keys, indexes, concurrency.

## System Designs
10. [Chat Application](.10-chat-application.md) — WebSockets, user-server mapping, Cassandra.
11. [Rate Limiter](.11-rate-limiter.md) — Token/Leaking/Sliding algorithms, Redis + Lua.
12. [Zoom / Video Conferencing](.12-zoom.md) — WebRTC, SFU vs MCU.
