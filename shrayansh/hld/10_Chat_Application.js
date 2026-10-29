/*
    Requirement Gathering
        Functional:
            1 to 1 send/receive messages (text for now)
            Group messaging should be supported
            Last seen
            Login and Authentication

        Non-functional:
            Scalability
                Huge traffic
                High volume of data
                Available
                Low latency
    Back of the envelope estimation
        Total user: 2B
        DAU: 50M
        1 user sends 10 messages to 4 people per day
        
        Messages per day:
            50M*4*10 = 2B
        Storage:
            2B * 100B = 200 GB per day
        
        Chat History:
            200GB*365*10 years = 730 TB

    Design:
        * Peer to Peer
            Not scalable
            Group messaging is very costly to handle
            Last seen not possible
        * Chat server
            User A and User B will talking via server
            Client-Server architecture
            This server will now be responsible for
                Scalability
                Chat History
                Grouping
                Availability

            Network Protocol:
                HTTP:
                    It's request->response basis
                    Will work fine while sending 
                    Wont work as receiving
                Polling:
                    Client asks for message at regular frequency to server
                    Connection creation and close everytime
                    Not scalable, waste of resources
                    High latency
                Long Polling:
                    Server waits for thresold before close the connection
                    Less waste of resources (some enhancement)
                Web Socket:
                    Bi-directional persistent connection
                    Will only break when one of them breaks it
            
            When user A (connected to server A) sends message to user B (connected to server B)
                We need a user-server mapping service
                    User A sends a message to Server A.
                    Server A will search for user B in user-server mapping and find server B.
                    Server A will pass the message to server B.
                    Server B will send the message to user B.
                It'll be recorded in the DB as well
            

            Web-sockets will only be used for chatting, rest will be done using HTTP only

            When a user is offline and tries to connect, 
        * Database:
            Uses:
                Read:
                    User read user details
                    Chat History
                    Group member's details
                Write:
                    Send message
                    Update details

            No complex joins
            Very long chat history
            Search in chat history
            Low latency search capability
            # All of them leads to Column wise NoSQL like Cassandra
                Message Table:
                    MessageId, From, To, TimeStamp
                Now the data is sharded horizontally

                Partition Key:     
                    CREATE TABLE chat_messages (
                        conversation_id TEXT,  -- Partition Key (needs be unique globally)
                        message_timestamp TIMESTAMP,  -- Clustering Key
                        message_id UUID,  -- Unique identifier for each message (can be unique locally)
                        sender_id TEXT,
                        message_body TEXT,
                        PRIMARY KEY (conversation_id, message_timestamp, message_id)
                    ) WITH CLUSTERING ORDER BY (message_timestamp ASC);

                    To get the keys bidirectionally we can use order of the key
                    if message_id<sender_id:
                        conversation_id = Hash(sender_id+message_id)
                    else:
                        conversation_id = Hash(message_id+sender_id)
        * User B offline or server B is down:
            User A message will go to server A
            Then server A will ask for user B to user-server mapping service
            It won't find it and will put the data in the DB

            Once user B logs in or server is up, it'll go to user mapping service and find one server C to user B
            Now connection is maintained
            Now it'll check in NoSQL DB if any message is available
        * Group:
            Group Table:
                group_id, user_id, message_id, timestamp

                group_id: partition key
                timestamp: composite key
        * Last seen
            Present service
                We will receive heartbeat each couple of seconds
                It'll keep last heartbeat time for each user
                If a user doesn't send some pre configured time, it'll be marked as offline



*/
