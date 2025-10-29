/*
Hashing
    : It gets done by a Hash function. Will take input of arbitrary length and will return of fix length.
        ~ MD5       128 bit / 16 byte / 32 hexa bytes / 32 characters
        ~ SHA256    256 bit / 32 byte / 64 characters
        ~ SHA512 ....
        These functions are deterministic, Pre-image resistance, fast, Collision resistance
        Deterministic: Same output for same input
        Pre-image resistance: Can not get input using output
        Collision resistance: have lesser collisions with more bytes in them
        Avalanche effect: Small change in input produces a significantly different output

        @https://www.encryptionconsulting.com/education-center/sha-256/
Mod-Hash
    : Hashing technique used in hash tables or distributed systems for load balancing. Use the modulo operation to distribute data across a fixed number of buckets or servers.
    bucket_index = hash(key) % number_of_buckets
    Used in load-balancing, sharding, consistent hashing
    Works best when number of buckets is fixed, because addition of server will remap everything


Consistent Hashing
    : Motto -> rebalancing should be as low as possible
    Achieves rebalancing of one nth of total data where n is total number of server in ideal scenario

    Disadvantage: if all the server are placed very near to each other than all load will go to one server
        ~ Solution: Use virtual servers, basically each server will be placed at multiple places on ring
*/
