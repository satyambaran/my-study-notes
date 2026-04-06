/*
Steps for 0 to 1 Million
    * Single server
    * Application and DB servers
    * Load balancer + multiple servers
    * DB replication
    * Caching
    * CDN
    * Data Centre
    * Message Queue
    * DB Scaling

    Single server: 
        ~ 2-tier
        ~ Application and DB in same server for client
    Application and DB servers:
        ~ 3-tier
        ~ Seperated Application and DB servers
        ~ Now client interacts with Application and that interacts with DB later
    Load balancer and multiple servers with DB:
        ~ 4-tier
        ~ Multiple application server
        ~ Added security as LB and server can now talk on private IP
    DB Replication:
        ~ Now this includes Master-Slave architecture in DB
        ~ Write queries will go to Master, read queries will go to slave. Later, master will update the slave
        ~ More security, as if master fails then any slave can raise to be master
    Caching:
        ~ Includes to reduce expensive DB read calls
        ~ Reads from slave, if data not present in cache
    CDN:
        ~ CDN does cachin, but all those who does caching are not CDN
        ~ If  user is far from data centre, they will delayed response for everything
        ~ Can place CDN nodes all over the world, which will do caching of static/pseudo static data
        ~ Client will call to nearest CDN, then will check with nearest CDN and then will go to main Data Centre
        ~ Avoids DDoS attack
        ~ Less loads on DB
    Data Centres:
        ~ Now, we will add multiple data centre to further reduce latency
        ~ Load balancer will use Data Centre using geo location of the user
        ~ For each data centre, there will be replication with each other
    Message Queue:
        ~ Rabbit MQ, Kafa
        ~ Producer -> Topics/Queue -> Consumer (with DLQ)
        ~ Adds asynchronous nature to our application (like sending notification, message, email)
        ~ More details in excalidraw
    DB Scaling:
        ~ Two types 
            ? Vertical Scaling: 
                -> Increase CPU, RAM capability for higher traffics
                -> There is a limit for these capabilities
            ? Horizontal Scaling: (Sharding)
                -> Put similar nodes for scaling
                -> Again two types
                    Horizontal: 
                        Divide table in multiple small table row-wise (divided on id: 1-1000, 1001-2000 etc)
                        If sharded based on name starting letter, then name with 'A' and 'S' will fill up fast and will have to reshard it. Which will create shard tree (solved by consist hashing).
                        We wont be able to join these sharded tables, will have to do denoramiling so that all the data is in same table and won't have to join
                    Vertical: Divide by column wise but contain all the rows.
*/