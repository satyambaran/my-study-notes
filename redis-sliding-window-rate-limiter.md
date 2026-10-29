# Redis Sliding-Window Rate Limiter

## Goal

For every user request:

1. Remove request timestamps that are outside the sliding window.
2. Count requests still inside the window.
3. Allow the request when the count is below the configured limit.
4. Store the accepted request's timestamp.
5. Reject the request when the limit has already been reached.

Example policy:

```text
Allow at most 100 requests per user during any rolling 60-second period.
```

## Redis data structure

Use one Redis **sorted set (`ZSET`) per user and rate-limit scope**.

- **Key:** identifies the user and operation being limited.
- **Score:** request timestamp in Unix milliseconds.
- **Member:** unique request ID.

```text
Key: rate_limit:{user-42}:orders

Member                  Score
request-7d98a           1791903000123
request-2ba71           1791903000892
```

The sorted-set score keeps requests ordered by timestamp and supports efficient
deletion of timestamps outside the current window.

### Suggested key formats

```text
rate_limit:{user-42}:global
rate_limit:{user-42}:orders
rate_limit:{user-42}:login
rate_limit:{tenant-17}:api
```

Use one key per user and scope instead of putting every user in one large sorted
set. This keeps counting and cleanup isolated and avoids creating one global hot
key.

In Redis Cluster, text inside braces is a hash tag. Keys containing the same
hash tag, such as `{user-42}`, are assigned to the same cluster slot.

## Why the member must be unique

Do not use only the timestamp as the sorted-set member:

```redis
ZADD rate_limit:user-42 1791903000123 1791903000123
```

Two requests can arrive during the same millisecond. Because sorted-set members
are unique, the second request would update the first member rather than create
a new entry.

Use a unique value such as:

```text
HTTP request ID
UUID
trace ID
timestamp plus a random suffix
```

The timestamp remains the score:

```redis
ZADD rate_limit:{user-42}:orders 1791903000123 request-7d98a
```

## Atomic Lua script

```lua
-- KEYS contains Redis key names passed to the script.
-- KEYS[1] is the sorted-set key for one user and rate-limit scope.
--
-- ARGV contains ordinary input values passed to the script:
-- ARGV[1] = current Unix timestamp in milliseconds
-- ARGV[2] = sliding-window duration in milliseconds
-- ARGV[3] = maximum accepted requests within the window
-- ARGV[4] = unique request ID
--
-- Returns:
-- {
--   allowed,          -- 1 = allowed, 0 = rejected
--   current_count,    -- accepted requests in the current window
--   remaining,        -- requests still available
--   retry_after_ms    -- wait time when rejected
-- }

-- "local" creates a variable that is visible only inside this script.
local key = KEYS[1]

-- Redis passes ARGV values as strings.
-- tonumber converts the numeric strings into Lua numbers.
local now_ms = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local request_limit = tonumber(ARGV[3])

-- The request ID remains a string because it becomes the sorted-set member.
local request_id = ARGV[4]

-- Validate every input before changing Redis data.
-- "not now_ms" is true when tonumber could not parse the value.
-- redis.error_reply returns a Redis error and return stops the script.
if not now_ms or now_ms <= 0 then
    return redis.error_reply("now_ms must be a positive number")
end

if not window_ms or window_ms <= 0 then
    return redis.error_reply("window_ms must be a positive number")
end

if not request_limit or request_limit <= 0 then
    return redis.error_reply("request_limit must be a positive number")
end

if not request_id or request_id == "" then
    return redis.error_reply("request_id is required")
end

-- Calculate the oldest timestamp that can still belong to the window.
-- The script keeps requests in the interval (window_start, now_ms].
local window_start = now_ms - window_ms

-- redis.call executes a normal Redis command from inside Lua.
--
-- ZREMRANGEBYSCORE key minimum_score maximum_score
-- removes sorted-set members whose scores are inside the given range.
-- "-inf" means negative infinity, so this removes every request timestamp
-- less than or equal to window_start.
redis.call(
    "ZREMRANGEBYSCORE",
    key,
    "-inf",
    window_start
)

-- ZCARD key returns the number of members in a sorted set.
-- Since expired entries were just removed, this is the active request count.
local current_count = redis.call("ZCARD", key)

-- ZSCORE key member returns that member's score.
-- It returns nil when the member does not exist.
-- This checks whether the same request ID was already accepted.
local existing_request = redis.call("ZSCORE", key, request_id)

-- Treat an existing request ID as an idempotent retry.
-- Do not add it or consume another rate-limit slot.
if existing_request then
    return {
        1,
        current_count,

        -- math.max returns the larger value and prevents a negative result.
        math.max(0, request_limit - current_count),
        0
    }
end

-- Reject the request when the active count has reached the configured limit.
if current_count >= request_limit then
    -- ZRANGE key start stop WITHSCORES reads sorted-set members by rank.
    -- Rank 0 is the lowest-scored, therefore oldest, request.
    -- Using start=0 and stop=0 returns only that one request.
    -- WITHSCORES returns both the request ID and its timestamp score.
    --
    -- Example Redis data:
    --   request-A -> score 1000
    --   request-B -> score 5000
    --
    -- "oldest" becomes a Lua table:
    --   oldest[1] = "request-A"  -- member/request ID
    --   oldest[2] = "1000"       -- score/timestamp
    --
    -- If the sorted set is empty:
    --   oldest = {}
    local oldest = redis.call(
        "ZRANGE",
        key,
        0,
        0,
        "WITHSCORES"
    )

    local retry_after_ms = 0

    -- "#" is Lua's table-length operator.
    --
    -- When ZRANGE returns one member with WITHSCORES:
    --   oldest = {"request-A", "1000"}
    --   #oldest = 2
    --
    -- When ZRANGE finds no member:
    --   oldest = {}
    --   #oldest = 0
    --
    -- "#oldest" does not contain data itself. It calculates how many
    -- sequential values are present in the "oldest" Lua table.
    -- The condition ensures both the member and score exist before reading
    -- oldest[2].
    if #oldest >= 2 then
        -- oldest[2] is the timestamp score returned as a string.
        -- tonumber converts it into a Lua number.
        local oldest_timestamp = tonumber(oldest[2])

        -- The oldest request expires at:
        -- oldest_timestamp + window_ms
        --
        -- Subtract now_ms to calculate how long the client must wait.
        -- math.floor removes any fractional part.
        -- math.max prevents a negative retry delay.
        retry_after_ms = math.max(
            0,
            math.floor(oldest_timestamp + window_ms - now_ms)
        )
    end

    -- Return: rejected, current count, zero remaining, retry delay.
    return {
        0,
        current_count,
        0,
        retry_after_ms
    }
end

-- ZADD key score member adds a member to a sorted set.
--
-- Here:
--   key        = this user's rate-limit key
--   score      = the request timestamp
--   member     = the unique request ID
--
-- Sorted-set members are unique. If the same request ID already exists,
-- ZADD updates its score instead of creating a duplicate. The ZSCORE check
-- above prevents an idempotent retry from reaching this command.
redis.call(
    "ZADD",
    key,
    now_ms,
    request_id
)

-- ZCARD was executed before ZADD, so include the newly accepted request.
current_count = current_count + 1

-- PEXPIRE key milliseconds sets the complete key's TTL in milliseconds.
-- If the user sends no more accepted requests, Redis automatically deletes
-- this sorted set after one complete window.
redis.call("PEXPIRE", key, window_ms)

-- Return: allowed, current count, remaining requests, zero retry delay.
return {
    1,
    current_count,
    request_limit - current_count,
    0
}
```


## Worked sliding-window example

Policy:

```text
Maximum requests: 3
Window:           10 seconds
```

### First request at 1 second

```text
Time: 1000 ms
Window: (-9000, 1000]
```

No old entries exist. `ZCARD` returns `0`, so request A is inserted:

```text
request-A -> 1000
```

### Second request at 5 seconds

```text
Time: 5000 ms
Window: (-5000, 5000]
Count before insert: 1
```

Request B is allowed:

```text
request-A -> 1000
request-B -> 5000
```

### Third request at 9 seconds

Request C is allowed:

```text
request-A -> 1000
request-B -> 5000
request-C -> 9000
```

The count is now `3`.

### Fourth request at 9.5 seconds

```text
Time: 9500 ms
Window: (-500, 9500]
Current count: 3
Limit: 3
```

Request D is rejected.

The oldest request expires at:

```text
1000 + 10000 = 11000 ms
```

Retry delay:

```text
11000 - 9500 = 1500 ms
```

### Retry at 11 seconds

```text
Time: 11000 ms
Window: (1000, 11000]
```

`ZREMRANGEBYSCORE` removes request A because its score is equal to the cutoff
`1000`.

The count becomes `2`, so request D can now be inserted:

```text
request-B -> 5000
request-C -> 9000
request-D -> 11000
```

## Script flow

```text
Incoming request
       |
       v
Remove timestamps <= now - window
       |
       v
Count remaining timestamps
       |
       +-------------------------+
       | count >= request limit? |
       +-------------------------+
          | Yes             | No
          v                 v
    Calculate retry      Store request ID
    delay from oldest    with timestamp score
          |                 |
          v                 v
       Reject             Allow
```

The interval used by this implementation is:

```text
(now - window, now]
```

A request exactly one complete window old is removed and no longer counted.

## Executing the script

Assume the script is saved as `sliding-window.lua`.

The following example allows at most 100 requests during a rolling 60-second
period:

```bash
redis-cli --eval sliding-window.lua \
  'rate_limit:{user-42}:orders' , \
  1791903000123 \
  60000 \
  100 \
  'request-7d98a'
```

Example response:

```text
1) (integer) 1    # Allowed
2) (integer) 37   # Accepted requests currently in the window
3) (integer) 63   # Remaining requests
4) (integer) 0    # Retry delay
```

A rejected response might be:

```text
1) (integer) 0       # Rejected
2) (integer) 100     # Current accepted-request count
3) (integer) 0       # No requests remaining
4) (integer) 12450   # Retry after 12,450 milliseconds
```

## `EVAL` versus `EVALSHA`

`EVAL` sends the complete script on every invocation:

```redis
EVAL "<script>" 1 rate_limit:{user-42}:orders 1791903000123 60000 100 request-7d98a
```

For production, load the script once:

```bash
redis-cli SCRIPT LOAD "$(cat sliding-window.lua)"
```

Redis returns the script's SHA:

```text
a4b8...
```

Run the cached script with:

```bash
redis-cli EVALSHA a4b8... 1 \
  'rate_limit:{user-42}:orders' \
  1791903000123 \
  60000 \
  100 \
  'request-7d98a'
```

Applications should handle `NOSCRIPT` by running `SCRIPT LOAD` again. Script
caches can be lost after a restart or failover.

Redis Functions, available in modern Redis versions, are another option when
the logic should be installed and managed on the Redis server.

## Timestamp source

This script accepts `now_ms` from the application for broad Redis-version
compatibility and predictable script execution.

All application instances should have synchronized clocks, normally through
NTP. Unix epoch milliseconds are currently below Lua's exact-integer limit for
double-precision numbers.

Redis also provides:

```redis
TIME
```

It returns:

```text
1) Unix time in seconds
2) Microseconds elapsed during that second
```

If Redis server time is required, the application can call `TIME`, convert the
result to milliseconds, and pass it to the script. This adds an extra
round-trip, so clock-synchronized application timestamps are often preferred.

## Handling expired request timestamps

Redis sorted-set members do not have individual TTLs. Expiration is therefore
handled at two levels.

### 1. Remove old members during every request

```redis
ZREMRANGEBYSCORE rate_limit:{user-42}:orders -inf 1791902940123
```

This removes every timestamp at or before the start of the current window.

### 2. Expire the complete inactive-user key

```redis
PEXPIRE rate_limit:{user-42}:orders 60000
```

Every accepted request refreshes the key TTL to one complete window. After the
user becomes inactive, the whole key disappears automatically.

The script does not refresh the TTL for rejected requests because rejected
requests are not new accepted events that need to be retained.

## Should rejected requests be stored?

Normally, store only accepted requests in the limiter's sorted set.

If rejected requests are inserted into the same set, a client that continuously
retries can continuously fill the window and extend its own blocked period.

If rejected attempts must be measured, publish them to a separate:

- Logging system
- Metrics system
- Redis Stream
- Short-lived counter

Do not mix them with the accepted-request timestamps used to make the rate-limit
decision.

## Why use Lua instead of `MULTI` and `EXEC`?

The operation is conditional:

```text
remove expired -> count -> compare -> add only when allowed
```

A Lua script performs this complete read-decide-write sequence atomically.
Another client cannot modify the sorted set between the count and insertion.

`MULTI` and `EXEC` atomically execute queued commands, but commands cannot
easily be conditionally queued based on results produced inside the transaction.
Doing that from the application requires `WATCH`, optimistic locking, and retry
logic.

Redis Lua scripts are atomic but are not rollback transactions. If a script
writes data and then fails, Redis does not undo the previous writes. This script
therefore validates all arguments before changing any Redis data.

Keep Lua scripts short because Redis blocks other commands while a script is
executing.

## Complexity and memory

The important command complexities are:

```text
ZREMRANGEBYSCORE: O(log N + M)
ZCARD:            O(1)
ZADD:             O(log N)
ZRANGE 0 0:       O(log N)
```

`N` is the number of stored requests and `M` is the number of expired requests
removed.

Because rejected requests are not inserted, the number of live members per key
is approximately bounded by the configured request limit.

An exact sliding log consumes more memory than a fixed-window counter because
it stores one member for every accepted request. At extremely high request
volumes, consider:

- Sliding-window counter with time buckets
- Token bucket
- Leaky bucket
- Redis Cell

These alternatives reduce memory but have different accuracy and burst
semantics.

# Redis Data Structures and Popular Commands

Redis is a data-structure server. A key points to a typed value, and commands
operate on that value according to its type.

Current Redis documentation lists the following data structures:

| Data structure | Shape | Typical uses |
|---|---|---|
| Strings | Byte sequence | Cache values, counters, locks |
| Bitmaps | Bits stored inside a string | Daily activity, boolean flags |
| Bitfields | Packed integers inside a string | Compact counters and attributes |
| Arrays | Sparse index-addressable strings | Indexed sparse data, ring buffers |
| Geospatial indexes | Locations ordered in a sorted set | Nearby-location search |
| Hashes | Field-value record | Objects and profiles |
| JSON | Nested objects and arrays | Structured documents |
| Lists | Insertion-ordered strings | Queues, stacks, recent items |
| Sets | Unordered unique strings | Membership, tags, relationships |
| Sorted sets | Unique strings ordered by score | Ranking, scheduling, rate limits |
| Streams | Append-only event log | Durable events and consumer groups |
| Time series | Timestamp-value samples | Metrics, sensors, monitoring |
| Vector sets | Named high-dimensional vectors | Semantic similarity and recommendations |
| Bloom filters | Approximate membership | Fast duplicate/existence checks |
| Cuckoo filters | Approximate membership with deletion | Membership with removals |
| Count-min sketch | Approximate frequencies | Event-frequency estimation |
| HyperLogLog | Approximate unique count | Unique visitors |
| t-digest | Approximate percentiles | Latency percentiles |
| Top-K | Approximate most-frequent items | Trending products or queries |

Availability depends on the Redis version and distribution. Newer structures
such as arrays and vector sets, and structures historically delivered through
Redis Stack modules, might not exist on an older server.

Check support directly:

```redis
COMMAND INFO ARSET
COMMAND INFO JSON.SET
COMMAND INFO TS.ADD
COMMAND INFO VADD
```

An empty result means that command is not available on that server.

Pub/Sub, transactions, Lua scripting, persistence, and secondary search indexes
are Redis features, but they are not value data structures.

## Strings and counters

Strings store text, serialized values, integers, or binary data.

| Command | Purpose | Example |
|---|---|---|
| `SET` | Store a value | `SET user:42:name "Sam"` |
| `GET` | Read a value | `GET user:42:name` |
| `MSET` | Store multiple values | `MSET a 1 b 2` |
| `MGET` | Read multiple values | `MGET a b` |
| `SET NX` | Store only if absent | `SET lock:order:7 token NX EX 30` |
| `SET XX` | Store only if present | `SET config:version 3 XX` |
| `INCR` | Increment an integer | `INCR page:views` |
| `INCRBY` | Increment by an amount | `INCRBY balance:42 10` |
| `DECR` | Decrement an integer | `DECR inventory:item:9` |
| `APPEND` | Append text | `APPEND log:message " completed"` |
| `STRLEN` | Get byte length | `STRLEN user:42:name` |

Store a value with a 60-second TTL:

```redis
SET verification:abc "123456" EX 60
```

Store only when the key does not exist:

```redis
SET lock:order:7 unique-token NX EX 30
```

For robust distributed locking, use an established implementation that verifies
the lock token before deletion.

## Bitmaps

A bitmap treats the bits inside a Redis string as independently addressable
boolean values.

Example: track whether user IDs were active today.

```redis
SETBIT active:2026-10-13 42 1
SETBIT active:2026-10-13 99 1
```

Check user 42:

```redis
GETBIT active:2026-10-13 42
```

Count active users:

```redis
BITCOUNT active:2026-10-13
```

| Command | Purpose |
|---|---|
| `SETBIT` | Set or clear one bit |
| `GETBIT` | Read one bit |
| `BITCOUNT` | Count bits set to `1` |
| `BITPOS` | Find the first `0` or `1` bit |
| `BITOP` | Perform `AND`, `OR`, `XOR`, or `NOT` across bitmaps |

Bitmaps are memory efficient when IDs map to reasonably dense integer offsets.
A very large sparse offset can allocate a large string.

## Bitfields

Bitfields pack several small signed or unsigned integers inside one Redis
string.

```redis
BITFIELD bike:1:stats \
  SET u32 #0 1000 \
  SET u32 #1 0
```

Increment both values atomically:

```redis
BITFIELD bike:1:stats \
  INCRBY u32 #0 -50 \
  INCRBY u32 #1 1
```

Read them:

```redis
BITFIELD bike:1:stats \
  GET u32 #0 \
  GET u32 #1
```

| Command | Purpose |
|---|---|
| `BITFIELD` | Get, set, or increment packed integers |
| `BITFIELD_RO` | Read packed integers without allowing writes |

Use bitfields when memory density matters and each value has a known bit width.

## Arrays

Redis arrays are sparse, index-addressable sequences of strings. An unset index
can remain empty without requiring every earlier index to contain a value.

```redis
ARSET temperatures 0 "19.5" "20.1" "20.7"
ARGET temperatures 1
```

Set and read non-contiguous indexes:

```redis
ARMSET sparse-values 10 ten 100 hundred
ARMGET sparse-values 10 100
```

| Command | Purpose |
|---|---|
| `ARSET` | Set one or more contiguous indexes |
| `ARGET` | Read one index |
| `ARMSET` | Set several arbitrary indexes |
| `ARMGET` | Read several arbitrary indexes |
| `ARGETRANGE` | Read an index range |
| `ARLEN` | Read the logical array length |
| `ARCOUNT` | Count non-empty elements |
| `ARSCAN` | Iterate populated indexes |
| `ARINSERT` | Insert through an auto-advancing cursor |
| `ARRING` | Configure/use a fixed-size ring buffer |

Use arrays for sparse indexed data or fixed-position records. Use lists when
insertion order and pushing or popping at the ends are the main operations.

## Geospatial indexes

Redis geospatial indexes store longitude, latitude, and a member name. Internally
they are based on sorted sets, but Redis exposes location-specific commands.

Add locations:

```redis
GEOADD bikes:rentable \
  -122.27652 37.805186 station:1 \
  -122.26746 37.806234 station:2
```

Find stations within 5 kilometers:

```redis
GEOSEARCH bikes:rentable \
  FROMLONLAT -122.27 37.80 \
  BYRADIUS 5 km \
  WITHDIST
```

| Command | Purpose |
|---|---|
| `GEOADD` | Add coordinates and members |
| `GEOPOS` | Read member coordinates |
| `GEODIST` | Calculate distance between two members |
| `GEOHASH` | Return geohash strings |
| `GEOSEARCH` | Search inside a radius or box |
| `GEOSEARCHSTORE` | Store search results in another key |

Use geospatial indexes for nearby stores, vehicles, delivery agents, or points
of interest.

## Hashes

Hashes store fields belonging to an object:

```redis
HSET user:42 name "Sam" plan "premium" login_count 5
```

| Command | Purpose | Example |
|---|---|---|
| `HSET` | Set one or more fields | `HSET user:42 name "Sam"` |
| `HGET` | Read one field | `HGET user:42 name` |
| `HMGET` | Read selected fields | `HMGET user:42 name plan` |
| `HGETALL` | Read all fields and values | `HGETALL user:42` |
| `HINCRBY` | Increment a numeric field | `HINCRBY user:42 login_count 1` |
| `HDEL` | Delete fields | `HDEL user:42 temporary_code` |
| `HEXISTS` | Check whether a field exists | `HEXISTS user:42 email` |
| `HLEN` | Count fields | `HLEN user:42` |

Use hashes for small records when individual fields need to be read or updated
without rewriting the complete object.

## JSON

Redis JSON stores nested JSON objects and arrays and allows operations on
individual paths.

```redis
JSON.SET user:42 $ '{"name":"Sam","plan":"premium","visits":5}'
```

Read the full document:

```redis
JSON.GET user:42 $
```

Read one path:

```redis
JSON.GET user:42 $.plan
```

Increment a number without rewriting the document:

```redis
JSON.NUMINCRBY user:42 $.visits 1
```

| Command | Purpose |
|---|---|
| `JSON.SET` | Create or replace a JSON path |
| `JSON.GET` | Read one or more paths |
| `JSON.DEL` | Delete a path |
| `JSON.TYPE` | Read the JSON type at a path |
| `JSON.NUMINCRBY` | Increment a JSON number |
| `JSON.ARRAPPEND` | Append to a JSON array |
| `JSON.OBJKEYS` | Read object field names |

Choose a hash for a flat field-value record. Choose JSON when nested arrays,
nested objects, and JSONPath operations are required.

## Lists

Lists are ordered collections that can operate as queues or stacks.

| Command | Purpose | Example |
|---|---|---|
| `LPUSH` | Add to the beginning | `LPUSH jobs job-1` |
| `RPUSH` | Add to the end | `RPUSH jobs job-2` |
| `LPOP` | Remove from the beginning | `LPOP jobs` |
| `RPOP` | Remove from the end | `RPOP jobs` |
| `LRANGE` | Read a range | `LRANGE jobs 0 -1` |
| `LLEN` | Get list length | `LLEN jobs` |
| `LTRIM` | Retain only a range | `LTRIM recent-events 0 99` |
| `BLPOP` | Wait and pop from the left | `BLPOP jobs 10` |
| `BRPOP` | Wait and pop from the right | `BRPOP jobs 10` |

Simple queue:

```redis
RPUSH jobs job-1
LPOP jobs
```

For durable messaging with multiple consumers and acknowledgements, Redis
Streams are generally preferable.

## Sets

Sets contain unique, unordered members.

| Command | Purpose | Example |
|---|---|---|
| `SADD` | Add members | `SADD user:42:roles admin editor` |
| `SREM` | Remove members | `SREM user:42:roles editor` |
| `SISMEMBER` | Test membership | `SISMEMBER user:42:roles admin` |
| `SMEMBERS` | Read all members | `SMEMBERS user:42:roles` |
| `SCARD` | Count members | `SCARD user:42:roles` |
| `SINTER` | Find intersection | `SINTER team:a team:b` |
| `SUNION` | Find union | `SUNION team:a team:b` |
| `SDIFF` | Find members in one set only | `SDIFF team:a team:b` |
| `SPOP` | Remove random members | `SPOP candidates 1` |
| `SRANDMEMBER` | Read random members | `SRANDMEMBER candidates 2` |

Use sets for roles, tags, unique visitors, relationships, and membership tests.

## Sorted sets

Sorted sets contain unique members ordered by numeric scores.

| Command | Purpose | Example |
|---|---|---|
| `ZADD` | Add a member and score | `ZADD leaderboard 1500 user-42` |
| `ZRANGE` | Read members in ascending order | `ZRANGE leaderboard 0 9 WITHSCORES` |
| `ZREVRANGE` | Read highest scores first | `ZREVRANGE leaderboard 0 9 WITHSCORES` |
| `ZRANGEBYSCORE` | Read a score range | `ZRANGEBYSCORE events 1000 2000` |
| `ZREM` | Remove members | `ZREM leaderboard user-42` |
| `ZREMRANGEBYSCORE` | Remove a score range | `ZREMRANGEBYSCORE events -inf 1000` |
| `ZCARD` | Count all members | `ZCARD leaderboard` |
| `ZCOUNT` | Count members in a score range | `ZCOUNT events 1000 2000` |
| `ZSCORE` | Read a member's score | `ZSCORE leaderboard user-42` |
| `ZRANK` | Read ascending rank | `ZRANK leaderboard user-42` |
| `ZREVRANK` | Read descending rank | `ZREVRANK leaderboard user-42` |
| `ZINCRBY` | Increment a score | `ZINCRBY leaderboard 20 user-42` |

Common uses include:

- Leaderboards
- Sliding-window rate limits
- Delayed jobs
- Event timelines
- Priority queues

## Probabilistic data structures

Probabilistic structures trade exact answers for much lower and usually bounded
memory usage. They are useful when small estimation errors are acceptable.

### Bloom filter

A Bloom filter answers:

```text
Might this item have been seen?
```

```redis
BF.ADD seen-emails sam@example.com
BF.EXISTS seen-emails sam@example.com
```

- `0` means definitely not present.
- `1` means probably present.
- False positives are possible.
- False negatives are not expected for inserted elements.

Common commands:

```text
BF.RESERVE, BF.ADD, BF.MADD, BF.EXISTS, BF.MEXISTS
```

### Cuckoo filter

A Cuckoo filter also performs approximate membership checks, but supports
deleting previously inserted items.

```redis
CF.ADD blocked-tokens token-123
CF.EXISTS blocked-tokens token-123
CF.DEL blocked-tokens token-123
```

Common commands:

```text
CF.RESERVE, CF.ADD, CF.ADDNX, CF.EXISTS, CF.DEL, CF.COUNT
```

### Count-min sketch

A count-min sketch estimates how often each item appears.

```redis
CMS.INITBYPROB query-frequency 0.001 0.01
CMS.INCRBY query-frequency redis 1 database 1 redis 1
CMS.QUERY query-frequency redis database
```

Common commands:

```text
CMS.INITBYDIM, CMS.INITBYPROB, CMS.INCRBY, CMS.QUERY, CMS.MERGE
```

Use it for approximate event, product, query, or IP frequencies.

### HyperLogLog

HyperLogLog estimates the number of unique elements using a small fixed amount
of memory.

```redis
PFADD visitors:2026-10-13 user-1 user-2 user-1
PFCOUNT visitors:2026-10-13
```

The result is approximately `2`, without storing a normal set containing every
visitor.

| Command | Purpose |
|---|---|
| `PFADD` | Add observed elements |
| `PFCOUNT` | Estimate unique count |
| `PFMERGE` | Merge multiple HyperLogLogs |

### t-digest

A t-digest estimates quantiles such as p50, p95, and p99.

```redis
TDIGEST.CREATE api:latency
TDIGEST.ADD api:latency 10 20 25 40 100
TDIGEST.QUANTILE api:latency 0.50 0.95 0.99
```

Use it for latency, response size, or other percentile distributions.

### Top-K

Top-K estimates the most frequent items in a stream.

```redis
TOPK.RESERVE trending-products 10
TOPK.ADD trending-products product-1 product-2 product-1
TOPK.LIST trending-products WITHCOUNT
```

Common commands:

```text
TOPK.RESERVE, TOPK.ADD, TOPK.INCRBY, TOPK.QUERY, TOPK.COUNT, TOPK.LIST
```

Use it for trending searches, popular products, or heavy hitters.

## Expiration and key management

| Command | Purpose | Example |
|---|---|---|
| `EXPIRE` | Set TTL in seconds | `EXPIRE session:abc 3600` |
| `PEXPIRE` | Set TTL in milliseconds | `PEXPIRE session:abc 5000` |
| `TTL` | Read remaining seconds | `TTL session:abc` |
| `PTTL` | Read remaining milliseconds | `PTTL session:abc` |
| `PERSIST` | Remove a TTL | `PERSIST session:abc` |
| `EXISTS` | Check key existence | `EXISTS user:42` |
| `TYPE` | Read the key's data type | `TYPE user:42` |
| `DEL` | Delete keys synchronously | `DEL cache:old` |
| `UNLINK` | Delete keys asynchronously | `UNLINK cache:large` |
| `RENAME` | Rename a key | `RENAME cache:v1 cache:v2` |
| `SCAN` | Incrementally find keys | `SCAN 0 MATCH user:* COUNT 100` |

Avoid this in production:

```redis
KEYS user:*
```

`KEYS` scans the complete keyspace synchronously and can block Redis. Use
cursor-based `SCAN` for operational iteration:

```redis
SCAN 0 MATCH user:* COUNT 100
```

Continue calling `SCAN` with the returned cursor until Redis returns cursor `0`.

## Transactions

| Command | Purpose |
|---|---|
| `MULTI` | Start queueing transaction commands |
| `EXEC` | Execute all queued commands |
| `DISCARD` | Abandon queued commands |
| `WATCH` | Watch keys for optimistic locking |
| `UNWATCH` | Stop watching keys |

Basic transaction:

```redis
MULTI
INCR orders:count
DECR inventory:item-9
EXEC
```

Optimistic transaction:

```redis
WATCH inventory:item-9
GET inventory:item-9
MULTI
DECR inventory:item-9
INCR orders:count
EXEC
```

If a watched key changes before `EXEC`, the transaction is aborted and the
application should read the current state and retry.

## Lua scripting and Redis Functions

| Command | Purpose |
|---|---|
| `EVAL` | Execute Lua source |
| `EVALSHA` | Execute a cached Lua script |
| `SCRIPT LOAD` | Load Lua and return its SHA |
| `SCRIPT EXISTS` | Check whether a SHA is cached |
| `SCRIPT FLUSH` | Remove cached scripts |
| `FUNCTION LOAD` | Install a Redis Function library |
| `FCALL` | Invoke a Redis Function |

Always pass accessed key names through `KEYS`:

```redis
EVAL "return redis.call('GET', KEYS[1])" 1 user:42:name
```

Pass ordinary values through `ARGV`:

```redis
EVAL "return redis.call('SET', KEYS[1], ARGV[1])" 1 user:42:name Sam
```

This is required for correct Redis Cluster key routing and script analysis.

## Streams

Streams provide append-only event records and consumer groups.

Append an event:

```redis
XADD orders * user_id 42 amount 100
```

| Command | Purpose |
|---|---|
| `XADD` | Append an event |
| `XRANGE` | Read an event range |
| `XREVRANGE` | Read an event range in reverse |
| `XREAD` | Read new events |
| `XTRIM` | Limit stream size |
| `XGROUP CREATE` | Create a consumer group |
| `XREADGROUP` | Consume through a group |
| `XACK` | Acknowledge processing |
| `XPENDING` | Inspect unacknowledged events |
| `XCLAIM` | Claim abandoned pending events |

Streams are useful for:

- Durable event delivery
- Worker queues
- Consumer groups
- Event history
- At-least-once processing

## Time series

Time series store timestamp-value samples and support retention, labels,
aggregation, and downsampling.

Create a series:

```redis
TS.CREATE temperature:room-1 \
  RETENTION 86400000 \
  LABELS room 1 metric temperature
```

Add a sample using the server timestamp:

```redis
TS.ADD temperature:room-1 * 22.4
```

Read samples:

```redis
TS.RANGE temperature:room-1 - +
```

Read one-minute averages:

```redis
TS.RANGE temperature:room-1 - + \
  AGGREGATION avg 60000
```

| Command | Purpose |
|---|---|
| `TS.CREATE` | Create and configure a time series |
| `TS.ADD` | Add one sample |
| `TS.MADD` | Add samples to multiple series |
| `TS.GET` | Read the latest sample |
| `TS.RANGE` | Read a timestamp range |
| `TS.MRANGE` | Query multiple labeled series |
| `TS.CREATERULE` | Create an automatic compaction rule |
| `TS.INFO` | Inspect series configuration and statistics |

Use time series for monitoring, IoT sensor readings, financial samples, and
other timestamped measurements.

## Vector sets

Vector sets store named high-dimensional vectors and provide approximate
similarity search using an HNSW index.

Conceptual example:

```redis
VADD product-vectors VALUES 3 0.10 0.20 0.30 product-42
VADD product-vectors VALUES 3 0.11 0.21 0.29 product-99
```

Find vectors similar to `product-42`:

```redis
VSIM product-vectors ELE product-42 COUNT 5 WITHSCORES
```

| Command | Purpose |
|---|---|
| `VADD` | Add a named vector |
| `VSIM` | Find similar vectors |
| `VREM` | Remove a vector |
| `VCARD` | Count vectors |
| `VDIM` | Read vector dimensions |
| `VEMB` | Read an element's vector |
| `VSETATTR` | Attach JSON attributes |
| `VGETATTR` | Read attached attributes |

Use vector sets for semantic search, recommendations, deduplication, and
nearest-neighbor lookup.

## Publish and subscribe

| Command | Purpose |
|---|---|
| `PUBLISH` | Publish a message |
| `SUBSCRIBE` | Subscribe to channels |
| `PSUBSCRIBE` | Subscribe using channel patterns |
| `UNSUBSCRIBE` | Stop receiving a channel |

Example:

```redis
SUBSCRIBE notifications
```

Another client publishes:

```redis
PUBLISH notifications "order completed"
```

Pub/Sub messages are not persisted. A disconnected subscriber misses messages.
Use Streams when persistence, acknowledgement, or replay is required.

## Server and diagnostics commands

| Command | Purpose | Example |
|---|---|---|
| `PING` | Test connectivity | `PING` |
| `INFO` | Inspect server statistics | `INFO memory` |
| `DBSIZE` | Count keys in the selected database | `DBSIZE` |
| `TIME` | Read Redis server time | `TIME` |
| `COMMAND INFO` | Inspect command metadata | `COMMAND INFO ZADD` |
| `MEMORY USAGE` | Estimate key memory | `MEMORY USAGE user:42` |
| `SLOWLOG GET` | Read slow-command records | `SLOWLOG GET 10` |

Be careful with administrative commands in shared or production Redis
instances. ACLs should restrict destructive and operational commands.

## Choosing a Redis data structure

| Requirement | Recommended structure |
|---|---|
| Cache one value | String |
| Atomic integer counter | String with `INCR` |
| Object with independently updated fields | Hash |
| Queue or stack | List |
| Unique membership and set operations | Set |
| Ranking or timestamp ordering | Sorted set |
| Durable event processing | Stream |
| Ephemeral fan-out notifications | Pub/Sub |
| Sparse numeric index | Array |
| Boolean state per integer ID | Bitmap |
| Several compact integer fields | Bitfield |
| Nearby-location query | Geospatial index |
| Nested document | JSON |
| Timestamped metrics | Time series |
| Semantic similarity | Vector set |
| Approximate existence check | Bloom or Cuckoo filter |
| Approximate unique count | HyperLogLog |
| Approximate frequencies | Count-min sketch |
| Approximate percentiles | t-digest |
| Approximate heavy hitters | Top-K |
| Exact sliding request log | Sorted set |
| Fixed-window rate limiter | String counter with expiry |

## Important operational practices

1. Set TTLs on cache, session, idempotency, and rate-limit keys.
2. Prefer `SCAN` over `KEYS` in production.
3. Prefer `UNLINK` over `DEL` when deleting very large values.
4. Keep Lua scripts short because scripts block other Redis work while running.
5. Use pipelining when several independent commands can be sent together.
6. Use transactions or Lua when a read-modify-write sequence must be atomic.
7. Use unique, structured key names such as `domain:entity:id:purpose`.
8. Avoid storing unbounded lists, sets, sorted sets, or streams.
9. Monitor memory, latency, evictions, slow logs, replication, and persistence.
10. Do not use Redis as the sole durable database unless the persistence and
    recovery tradeoffs have been deliberately accepted.

## References

Redis rate limiting:

```text
https://redis.io/docs/latest/develop/use-cases/rate-limiter/
```

Redis Lua scripting:

```text
https://redis.io/docs/latest/develop/programmability/eval-intro/
```

Redis command reference:

```text
https://redis.io/docs/latest/commands/
```

Redis sorted sets:

```text
https://redis.io/docs/latest/develop/data-types/sorted-sets/
```

Redis data types:

```text
https://redis.io/docs/latest/develop/data-types/
```
