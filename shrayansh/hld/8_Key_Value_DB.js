/*  
    ^ Key - Value DB
        e.g. DyanamoDB

        ` Goals
            Scalability
            Decentralization
            Eventual Consistency

        ` Steps
            Partition
            Durability/Replication
            Get and Put operation
            Data versioning
            Gossip Protocols
            Merkle Tree

        `Where we use it?
            Add to cart
    
        * Partition:
            Can use Hash-Map, but need to make sure that it works on distributed enviroments
            Need to use consistent hashing
        * Durability:
            Each server is a Single point of failure
            Need to replicate data of each server
            Replicated data should be on next and next to next server of the ring (Any way request will be going there only in case of failure)
            These next servers are stored as Preference list in each server
        * Get and Put operation:
            ~ Put:
                Find hash key -> write into the server -> put in replicas in async manner
                Any 'W' async replication sends success response, we'll return success response to Query
            ~ Get:
                Find hash key -> read from the co-ordinator -> read from replicas in async manner -> match the data and take some decision if not matches
                Any 'R' replica sends success response, we'll return success response to Query
            & R + W > N
                R: number of replica response we need to wait while get operation
                W: number of replica response we need to wait while put operation
                N: number of replicas

                If W + R > N, strong consistency is guaranteed (Usually N = 3, W = R = 2).
                If W + R <= N, strong consistency is not guaranteed.
                If R = 1 and W = N, the system is optimized for a fast read.
                If W = 1 and R = N, the system is optimized for fast write.
                Depending on the requirement, we can tune the values of W, R, N to achieve
                the desired level of consistency.

            @Load balancer:
                Can be of two types here.
                i) Generic: Can send the request to any server and that server will utilize the Preference List
                    High latency
                ii) Partition Aware LB: Will pass the request to correct server. Will have to implement the logic.
                    Low latency
        * Data versioning:
            If communication breaks somehow or co-ordinator server is down, we can have the different versions of data in different servers
            Uses Vector Clock for data versioning
            Vector Clock = list[(server, version) for each server]

            s1: (s1 v2)
            s2: (s1 v2), (s2 v1)
            s3: (s1 v2), (s3,v1)
            We can be sure that s1 doesn't have the correct data, but either s2 or s3 have.
            Conflict
            Will return both of them to resolve the Conflict
            Now once conflict is resolved by client using some algo (Last write win), client sends (s1 v3) which will be propagated to other servers
        * Eventual Consistency:
            Sacrificed C to get AP
        * Gossip Protocol:
            Each server shares each server a heart-beat at a regular interval, showing they are up. If multiple servers are not receiving heartbeat from a server then its marked as down.
            To do the above, called gossip Protocol.
            Server will talk to each other saying this particular server's not updating its heart-beat count or some other predefined data. Then it'll be marked as down.
        * Merkle Tree:
            It's used to check if replica have the correct data or not.
            We need to calculate hash of every 2 node. Recalculate hash of every two hash from the previous step till we have one hash. This will create a tree like structure.
            Now we can compare hashes at each step and find which datas are off in logarithmic scale
            
*/
