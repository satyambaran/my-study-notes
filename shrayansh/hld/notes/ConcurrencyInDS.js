/*


Concurrency control:
    Crtical section: where we are accessing shared resource

    Same shared resource can be assigned to multiple user
    # use Synchronize
        Wont work in distributed system

    Distributed concurrency control
        > optimistic vs pessimistic concurrency control

    Transaction:
        Helps us achieve integrity
            any failure leads to rollbacks
    Locking:
        Make sures that no other transaction updates the locked rows
        > Shared Lock:
            Read lock, update can't be done locked rows but read can happen
        > Exclusive Lock:
            Write Lock, no other transaction can put any lock or read it
    Transaction Isolation Level:
        When multiple transaction are happening, each transaction feels they are working alone.

        Dirty Read:
            Uncommitted updated data, can be rollbacked so the other data have dirty read
        Non-repeatble read:
            If a transaction try to read a data, there might be possibility that the same transaction reads different through the transaction
        Phantom read:
            If a trasaction executes same query multiple, they should read the same number of data
            for <, >, between queries

        ~ These levels can be used if following problems are acceptable
        | Isolation Level   | Dirty Read | Non-repeatble | Phantom Read | Concurrency
        |-------------------|------------|---------------|--------------|--------------
        | Read Uncommitted  |   Yes      |      Yes      |    Yes       | Highest Concurrency
        | Read Committed    |   No       |      Yes      |    Yes       |
        | Repeatable Read   |   No       |      No       |    Yes       |
        | Serializble       |   No       |      No       |    No        | Lowest

        Isolation Level   ->  Locking Strategy
        |--------------------------------------------|
        Read Uncommitted  ->   No Lock required
                                Transacion B can read uncommited data of transction A which can be rollbacked later by A
        Read Committed    ->   Read: Shared lock required and released as soon as read is done
                                Write: Exclusive lock required and keeps till the end of the transaction
        Repeatable Read   ->   Read: Shared lock required and released only at the end of the transaction
                                Write: Exclusive lock required and keeps till the end of the transaction
        Serializble       ->   Same locks as repeatble read but puts range lock as well which satisfies the given where condition (for which phantom read has happened)


        Using Distributed Concurrency control
        * Optimistic concurrency control:  
            It doesnt use any locks just checks the version before transaction starts (just lock while very first read, dont need but recommended)
            Once the transaction completes, it wants to commit but first will verify version, now if version is not matching some one might have updated that. So current transaction will roll back or restart
        * Pessimistic
            Keeps read/write lock through the transaction (very much similar to Serializble)
            Can lead to deadlock

                                        Optimistic                      vs                 Pessimistic
            Isolation Level used:   Read Committed & Read Uncommitted               Repeatable vs Serializble
                                    High Concurrency                                Low Concurrency
                                    No deadlock                                     Deadlock possible, and stuck transcations needs to rollbcak then
                                    Conflict: Transaction rollback & retry          No Conflict possible: putting lock however can lead to timeout and needs to retry

            
    Deadlock happens:
        When a transaction already has a lock on A and want lock on B & the other transaction has lock on B and want lock on A.
        When both trxns have shared lock on A and want Exclusive lock now, then both will keep waiting no one will release shared lock and exclusive lock wont happen

    2 Phase Lock:
        There'll be two phases, 
            one phase will be the time where we last took any kind of lock
            seccond phase will only releases the the locks
        
        In 2 phase lock,
            second transaction will keep waiting till the first phase of first transaction. Once first trxn's second phase starts only to take lock then it can take the lock
        Trxn1: begin, lock(a), lock(b), calc, unlock(a), unlock(b) commit
        Trxn2:                                begin,     lock(b), lock(a), calc, unlock(a), unlock(b) commit

        Deadlock Prevention Strategy:
        1. Timeout: 
            Scheduler checks if a trxn is waiting for too long to get a lock, it assumes that it's in deadlock and aborts it.
                ! Scheduler can make mistakes
                    Where trxn 2 waits for the lock but trxn1 is taking long time so it'll wrong abort.
        2. Wait for graph (direct graph): (Topological sort)
            There will be an directed edge from node Ti and Tj if Ti is waiting for Tj to release some lock 
            Scheduler deletes those edges when Tj releases the lock
            Scheduler detects a cycle and when deadlock is idetified, some victim trxn identified which gets aborted
                Scheduler check for the below criteria to abort the trxns:
                    Amount of effort put in by each trxn
                    Amount of effort required by each trxn
                    Cost of aborting required by each trxn
                    The number of cycles that contain each trxn


        Conservative 2 phase locking:
            Acquire all the locks at the start of the trxn itself
            Each trxn predeclared its read and write operation to Scheduler
            Scheduler then tries to get all the locks, if any lock is unavailable then none of the lock will be taken and it'll wait 
            Cons:
                Less concurrency
                Extra overhead for Scheduler


        Timestamp based Deadlock prevention:
            Old Timestamp of trxn = Higher priority trxn
            Wait-Die:
                Old trxn wait for the new trxn to release the lock (but only old would wait if somehow new trxn got the lock)
                If old trxn has thee lock, then new trxn will jsut die
            Wound-Wait:
                Old trxn makes new trxn aborted

        Cascading Aborts:
            Trxn1: begin, lock(a), calc, unlock(a)          abort/rollback
            Trxn2:                       begin,    lock(a)
            In this case, trxn2 has dirty read, so if trxn1 is getting aborted then trxn2 also needs to be aborted
        
        Strong strict 2PL:
            Trxn not allowed to get any lock after first phase
            Release all the locks at the end of the trxn only

            Cons:
                Less concurrent
                Deadlock

        Basic 2PL: Deadlock and Cascading Aborts
        Conservative 2PL: Cascading Aborts
*/
