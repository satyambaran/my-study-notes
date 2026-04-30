# Back-of-the-Envelope Estimation

> **TL;DR** — Quick math to sanity-check a design: QPS, storage, bandwidth, memory. Get the size right; interviewers aren't grading your arithmetic, they want to see you know that a 1 TB cache for a 100-user app is silly.

## Key Takeaways

- **Round hard.** `1 day ≈ 100,000 seconds` is close enough (the real number is 86,400).
- **Think in ratios.** Reads vs writes, active users vs total, cache hit rate.
- **Go one pass at a time:** traffic → storage → bandwidth → memory → servers.
- **Memorize the latency numbers** — they decide which options are OK.
- **Always say your assumptions out loud.** The interviewer cares far more about your thinking than the final number.

## Unit Cheat Sheet

| Category     | Units                                  |
|--------------|----------------------------------------|
| Count        | 1K, 1M, 1B, 1T, 1Q (quadrillion)       |
| Storage      | 1 B, 1 KB, 1 MB, 1 GB, 1 TB, 1 PB      |
| Time         | 1s = 10⁶ µs = 10⁹ ns                   |
| Day          | 86,400s ≈ **10⁵** (100K) — use this    |

> Round `24 × 3600` to `10⁵` and move on.

## Data Size Reference

| Item                 | Size           |
|----------------------|----------------|
| Bit                  | 1 bit          |
| Byte                 | 8 bits         |
| `char` (Java)        | 2 B (UTF-16)   |
| ASCII char           | 1 B            |
| `int`, `float`       | 4 B            |
| `long`, `double`     | 8 B            |
| UUID                 | 16 B           |
| Typical URL          | ~100 B         |
| Tweet (280 chars)    | ~300 B         |
| IP address           | 4 B (v4) / 16 B (v6) |
| Compressed image     | 100 KB – 1 MB  |
| 4K photo             | ~10 MB         |
| 1 min HD video       | ~100 MB        |
| 1 min 4K video       | ~300 MB – 1 GB |

## Latency Numbers Every Programmer Should Know

*(Peter Norvig / Jeff Dean — 2020 ballpark)*

| Operation                            | Latency          |
|--------------------------------------|------------------|
| L1 cache reference                   | 0.5 ns           |
| Branch mispredict                    | 5 ns             |
| L2 cache reference                   | 7 ns             |
| Mutex lock/unlock                    | 25 ns            |
| Main memory reference                | 100 ns           |
| Compress 1 KB with Snappy            | 2,000 ns (2 µs)  |
| Send 2 KB over 1 Gbps                | 20 µs            |
| SSD random read                      | 150 µs           |
| **Read 1 MB from memory in order**   | **250 µs**       |
| Round trip inside one datacenter     | 500 µs           |
| **Read 1 MB from SSD in order**      | **1 ms**         |
| Disk seek (HDD)                      | 10 ms            |
| Read 1 MB from HDD                   | 20 ms            |
| Packet CA → Netherlands → CA         | 150 ms           |

**Rules of thumb:**
- Memory is ~100× faster than SSD.
- SSD is ~10–100× faster than HDD.
- Between datacenters = 10–100ms.
- Inside one datacenter (RPC) = 1–10ms (network + app time).

## Typical System Numbers

| Resource                | Per-server capacity           |
|-------------------------|-------------------------------|
| API server QPS          | 1K–10K (CPU-bound)            |
| MySQL QPS               | 10K reads / 1K writes         |
| Redis QPS               | 100K+                         |
| Kafka throughput        | 1M+ msgs/s per broker         |
| Nginx reverse proxy     | 50K+ req/s                    |
| Disk (NVMe SSD)         | ~500K IOPS, 3–7 GB/s          |
| Network (typical VM NIC)| 10–25 Gbps                    |

## Estimation Steps — 5 of them

1. **Traffic** — DAU, QPS, peak vs average.
2. **Storage** — size per item × items per day × how long you keep them.
3. **Bandwidth** — data in + data out per second.
4. **Memory** — cache size, memory per connection.
5. **Servers** — total need divided by per-server capacity.

## Worked Example — Social Feed (FB / Twitter-like)

### Traffic
- Total users: **1B**
- DAU: 25% = **250M**
- Per user: 5 reads + 2 writes per day = 8 actions
- **QPS = 250M × 8 / 10⁵ = 20K avg QPS**
- **Peak QPS = 2× avg = ~40K**

### Storage
- Posts per user per day: 2
- Text per post: 250 chars = ~500 B
- Images per post: 1 × ~300 KB

| Item          | Math                            | Per day | 5 yr |
|---------------|---------------------------------|---------|------|
| Text storage  | 250M × 2 × 500 B                | 250 GB  | ~450 TB |
| Blob storage  | 250M × 1 × 300 KB               | 75 TB   | ~140 PB |

**Takeaway:** images eat almost all the storage — put them in object storage (S3 / GCS), not your main DB.

### Bandwidth
- Outbound: users read 5 posts each → `250M × 5 × 300 KB / 10⁵` ≈ **4 GB/s**.
- CDN takes the bulk; your origin sees a fraction.

### Memory (cache)
- Cache the 5 latest posts per user → `250M × 5 × 500 B` = ~600 GB for text.
- Per machine: 64 GB usable → ~10 cache nodes.

### Servers
- Per-server: 1K QPS (app) → `40K / 1K` = **40 app servers** (round up with headroom → 60).

### CAP side
- Facebook / Twitter feed → **AP**. A slightly old like count is fine.

## Worked Example — Chat App (WhatsApp-like)

### Traffic
- Total users: 2B, DAU 50M.
- 10 msgs × 4 recipients = 40 msgs per user per day.
- Total msgs per day = **2B**.
- QPS = `2B / 10⁵` = **20K avg msgs/s**.

### Storage
- Per msg ≈ 100 B → `2B × 100 B = 200 GB / day`.
- 10-year retention → **730 TB**.
- Media (photos/videos) dominate — see FB math above.

### Connections
- 50M DAU, but not all online at once. Peak ~10M concurrent WebSocket connections.
- Per connection: ~10 KB of memory → **100 GB memory for connections**.
- Per server: ~100K live WS connections → ~100 chat servers.

## Worked Example — Video Platform (YouTube-like)

### Traffic
- 2B users, 500M DAU.
- Each DAU watches 30 min of video on average.
- **Video hours watched per day = 250M hours**.

### Storage (uploads)
- 500 hours uploaded per minute.
- Per minute of 1080p = ~100 MB, times a few encodings (1080p/720p/480p/240p) = ~500 MB.
- `500 × 500 MB × 60 × 24 = 360 TB / day` of new encoded content.

### Bandwidth (playback)
- 5 Mbps on average.
- 100M concurrent streams → **500 Tbps globally** — CDN is a must.

## Worked Example — Rate Limiter (Sanity Check)

- 100M API users.
- 10 req/s per user at peak → 1B QPS → nuts without sharding.
- More realistic: 10% concurrent, 5 req/s avg → 50M QPS.
- Redis does ~100K ops/s per node → **500 Redis nodes** at least.

## Common Interview Assumptions

| Metric                      | Reasonable guess                    |
|-----------------------------|-----------------------------------------|
| Reads vs writes             | 100:1 for content, 10:1 for social, 1:1 for chat |
| DAU / Total users           | 10–30%                                 |
| Peak vs average QPS         | 2–3×                                   |
| Cache hit rate              | 80–95% (say it out loud)               |
| Avg text post               | 200–500 B                              |
| Avg image                   | 200 KB–1 MB                            |
| Avg video (1 min 1080p)     | 50–100 MB                              |

## Formulas Worth Remembering

- `QPS = DAU × actions_per_user / seconds_per_day`
- `Peak QPS = QPS × 2` (start here; justify bigger multipliers)
- `Storage/day = ingest_rate × size_per_item × seconds_per_day`
- `Servers = peak_QPS / per_server_QPS`
- `Memory = working_set × avg_size`
- `Bandwidth = users × avg_data_per_user / time`

## Interview-Ready Tips

1. **Write down your assumptions first** before doing any math.
2. **Keep units on every step** — don't drop them.
3. **Round generously** — the interviewer cares about your model, not arithmetic.
4. **Spot the dominant cost** — usually blob storage or bandwidth.
5. **Use the number to pick a design** ("Because blobs are 75 TB/day, we'll use S3 and serve via CDN — not our main DB").
