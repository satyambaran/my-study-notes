/*
    Size in Bytes:
        char: 2 Bytes
        long/double: 8 bytes
        image: 300 KB 
    Cheat Sheet:
        Always keep in multiple of 1000
        Traffic: 1K, 1M, 1B, 1T, 1Q
        Storage: 1B, 1KB, 1MB, 1GB, 1TB

    Need to find these following things:
        No. of Servers, RAM, Storage capacity, Trade-Off (CAP)

    Estimation:
        ~ Traffic:
            Total User:- 1B
            DAU:- 25% = 250M (daily active user)
            Query per user:- 5Reads + 2Writes = 8 queries
            Query per second:- 250M*8/86400 = 2T/0.08M = 25K QPS
        ~ Storage:
            Post per day:- 2 posts (250 chars per post)
            Text per post:- 250 chars = 500 B = 0.5KB
            Images per post:- 1 = 300KB = 0.3MB

            Storage per day:- DAU * Post per day * post size
                            = 250M * 2 * 0.5KB = 250 GB = 0.25 TB
            Blob storage per day = 250M * 1 * 0.3 MB = 75 TB = 80 TB
        ~ RAM:
            Cached post per user:- 5 = 2.5 KB  // Cached post per user:- 5 = 1.5 MB
            Memory space required:- 250M * 2.5KB = 750 GB
            1 Machine capacity = 75GB
            so total 10 machines for caching

            Threads per server:- 50
            Requests per thread:- 2
            Server can serve 100 request per second
            No. of Server = 25K/100 = 250 servers
        ~ Latency:
            For 95% requests, latency is 500ms

        ~ Trade-Off: CAP theorem
            Discuss the trade-offs. For facebook AP
*/
