/*
    Manages the pool of reuasble objects
    borrow from pool, use and return


    client -> poolManager -> resource

    list<resource> freeResource, inUse;
    pool_size(initial and max), getResource(), releaseResource()
    
    Pool design pattern should be used with singleton design pattern and should be thread safe 
*/

/*
    Payment Gateway
        Peer to Peer, Peer to Merchant(adds refund and multi-tenancy)

        Peer-to-Peer
    Requirements:
        User(add, delete, update), Instrument(banks, cards, etc), 
        Payment(search user, select amount and Instrument, pass information to transaction processor)
    Notification for operations
    Transaction history

    Object:
        User, instrument, transaction, transaction history, Notification, processor
*/
/*
Multi-tenancy is a software architecture that allows multiple users or groups of users, called tenants, to share the same software instance
*/