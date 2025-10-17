/*
\\ All about Consistency
    ^ Consistency in Programming:
        In single threaded servers, consistency is guaranteed by default
        However, we need certain guarantees from the system regarding how different operations would be ordered, how updates would be visible and essentially what would be the impact on performance and correctness of the system in the presence of multiple threads of execution or processes
        
        ` Consistency model defines that abstraction
            -> It's basically trade-offs between Concurrency and Ordering of operations
            ->                           between Performance and Correctness of the operations

    ^ Consistency in Database Systems:
        Data base entities are in healthy state
    ^ Consistency in Distributed Systems:
        Every node/replica has the same view of data(otherwise an error) at a given point in time irrespective of whichever client has updated the data
        ~ So that from the outside, it looks like there is a single node performing all the operations


        Handling consistency in a distributed database system is a complex challenge due to the inherent trade-offs between consistency, availability, and partition tolerance (CAP theorem)

        Distributed systems are all about choosing the right trade-offs

        @ https://kousiknath.medium.com/consistency-guarantees-in-distributed-systems-explained-simply-720caa034116

        * Need of Consistency
            To serve massive online traffic (due to which we need replicas/partition, to increase system's availability)
            To connect the user world-wide
            To keep all the data correctly in the replicas if the main server fails
            Different data have different priorities, like transactions and social media posts
                One needs to be highly consistent but delayed consistency will work for other one

            \\ Different system needs different type of consistency, hence system should we able to implement and handle this requirement to get better trade-offs on availability
        * Consistency Guarantees

        
        * CAP Theorem:
            Depending on application's requirement, we can design our systems to favor consistency, availability, or partition tolerance
        * Consistency Models:
            # Strong Consistency
                Every read receives the most recent write
                    ~ Account balance after money Transfer
            # Eventual Consistency
                Updates propagate to all nodes eventually, so reads may return older data
                    ~ Social media posts and their likes and comments
            # Causal Consistency
                If one operation happens before another, the system ensures that everyone sees them in that order
                    ~ Google docs update/delete/comment shouls follow this
            # Read-Your-Writes Consistency
                After a user writes data, they will not see older versions of that data
                    ~ Updating the profile picture
            # Monotonic Reads Consistency
                Once a user has seen a version of the data, they will not see an older version in subsequent reads
                    ~ Email
        * Consensus Algorithms
            # Paxos
            # Raft
            # ZooKeeper Atomic Broadcast
        * Quorum-Based Voting
        * Data Partitioning and Replication
        * Distributed Transactions
        * Consistency Techniques
        * Database Systems with Built-in Consistency Models
        * Eventual Consistency with Converging State
        * Hybrid Approaches
        * 




*/