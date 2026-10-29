/*
    Transaction: 
        Set of operations which needs to performed against the DB

        It has 4 properties ACID:
        Atomicity:  All operations in a single trxns should be success or all should fail.
        Consistency:DB should be in consistent state before and after the trxn
        Isolation:  More than one trxn can occur concurrently
        Durability: Trxn once committed, should persist


        begin
            deduct 100 from a
            send 100 to b
            if all success:
                commit
            else
                rollback
        end

    How to handle it in Distributed system, where operations involves multiple DB?
        Transaction is local to a particular DB

        It wont work, because two different DB
        begin
            update order DB
            update inventory DB
            if all success
                commit
            else
                rollback
        end

    Three ways:
    1. 2 Phase commit: (very popular)
        1st phase: Voting/Prepare phase
        2nd phase: Decision/Commit phase

        There will be an transaction co-ordinator, which will send update query to both Order and Inventory DB. Both db will put a lock for this trxn
        Phase 1: Then it'll ask both of them. Prepared? 
        Phase 2: If both are OK then Commit
        Close trxn

        Transaction co-ordinator(TC) can fail
        Participant DB can fail
        Communication can break
        both DB and TC maintains a Log and Decision file. Now TC asks for commit from DB.
        
        After receiving update query, both DB locks.
            If prepare message gets lost, then after waiting DB will abort       //safe
            If response of prepare gets lost, then after waiting TC will abort   //safe
            If commit gets lost, some DB received commit and some didn't. then Participant DB can not take their own decision. To solve this we have 3 Phase commit.
        
    2. 3 Phase commit: (Each Participant DB can query each other)
        1st phase: Voting/Prepare phase
        2nd phase: Pre-Commit phase (sharing the decision)
        2nd phase: Commit phase



    2. 3 Phase commit: (not used much because of complexity)

    3. SAGA pattern:   (used very much)
        Async in nature
        Event based, sequential in nature.
            if success, create event for next
            if fails, create rollback event for previous one

*/
