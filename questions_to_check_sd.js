/*
    Load Shedding(503 :- retry with exponential backoff) vs Rate Limitter(429 :- Should stop retry)
        Rate limitter can be sidecar per instance(local fairness) and can also be a api gateway(global fairness). Local fairness can be used in multi-tenant system

    
    Thunderhird problem
    Celebrity Problem
    Multi-Tenant service 
        (No fairness on quota of qps per tenant in maersk project, 429 when quota exceeds)

    qps/opm/tps


    Load Shedding (fine with low qps)
    1. CPU load
    2. In flight request i.e. L = lambda * W
        number of inflight concurrency = qps * avg time for request per second(latency)
        L = 100 qps * 0.5 s = 50 in flight request
    3. Memory resources
*/

/*
Memoization vs Top bottom
https://leetcode.com/problems/best-time-to-buy-and-sell-stock-v/submissions/1975671354/
https://leetcode.com/problems/best-time-to-buy-and-sell-stock-v/submissions/1992015262/

https://algo.monster/liteproblems/2371#code

*/
