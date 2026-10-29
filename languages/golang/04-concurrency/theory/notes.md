
# Concurrency Patterns
    Concurrency patterns are well-established solutions to common problems encountered in concurrent programming.
    Go’s philosophy on concurrency can be summed up like this: aim for simplicity, use
channels when possible, and treat goroutines like a free resource.

## Why are Concurrency Patterns Important?
Concurrency patterns offer several benefits:
1. Efficient Resource Utilization: They enable the efficient use of system resources, including CPU cores and memory.
2. Responsiveness: Concurrency ensures that applications remain responsive to user inputs while performing background tasks.
3. Reduced Bugs: Patterns help prevent common concurrency issues like race conditions and deadlocks, enhancing code reliability.

### Race condition 
occurs when two or more operations must execute in the correct
order, but the program has not been written so that this order is guaranteed to be
maintained

### Atomicity
When something is considered atomic, or to have the property of atomicity, this
means that within the context that it is operating, it is indivisible, or uninterruptible.

### Memory Access Synchronization


### Deadlocks, Livelocks, and Starvation
- Deadlock
    - A  deadlocked  program  is  one  in  which  all  concurrent  processes  are  waiting  on  one another. In this state, the program will never recover without outside intervention.
    ```go
        var wg sync.WaitGroup
        printSum := func(v1, v2 *value) {
            defer wg.Done()
            v1.mu.Lock() 
            defer v1.mu.Unlock() 
            time.Sleep(2*time.Second) 
            v2.mu.Lock()
            defer v2.mu.Unlock()
            fmt.Printf("sum=%v\n", v1.value + v2.value)
        }
        var a, b value
        wg.Add(2)
        go printSum(&a, &b)
        go printSum(&b, &a)
        wg.Wait()
    ```
    - Deadlock occurs if all these conditions hold
        - Edgar Coffman Conditions
            1. Mutual Exclusion
                A concurrent process holds exclusive rights to a resource at any one time.
            2. Wait For Condition
                A concurrent process must simultaneously hold a resource and be waiting for an additional resource.
            3. No Preemption
                A resource held by a concurrent process can only be released by that process, so it fulfills this condition.
            4. Circular Wait
                A concurrent process (P1) must be waiting on a chain of other concurrent processes (P2), which are in turn waiting on it (P1), so it fulfills this final condition too.
- Livelock
- Starvation
    - Starvation is any situation where a concurrent process cannot get all the resources it needs to perform work.
    - greedy worker greedily holds onto the shared lock for the entirety of its work
loop, whereas the polite worker attempts to only lock when it needs to. Both workers
do the same amount of simulated work (sleeping for three nanoseconds), but as you
can see in the same amount of time, the greedy worker got almost twice the amount
of work done

## Concurrency vs Parallelism
Concurrency is a property of the code; parallelism is a property of the running
program.

## Communicating Sequential Processes

## Decision Tree
- Each data has an owner, and one way to make concurrent programs safe is to ensure only one concurrent context has ownership of data at a time. Channels help us communicate this concept by encoding that intent into the channel’s type
- 
Primitive vs Channels
```
    if(Performance Critical){
        Primitive
    }else{
        if(trying to transfer ownership of data){
            Channel
        }else{
            if(guard internal state of struct){
                Primitive
            }else{
                if(co-ordinating multiple piece of logic){
                    channel
                }else{
                    primitive
                }
            }
        }
    }
```
Are you trying to coordinate multiple pieces of logic?
    Remember that channels are inherently more composable than memory access
    synchronization primitives. Having locks scattered throughout your object-graph
    sounds like a nightmare, but having channels everywhere is expected and
    encouraged! I can compose channels, but I can’t easily compose locks or methods
    that return values.
    You will find it much easier to control the emergent complexity that arises in
    your software if you use channels because of Go’s select statement, and their
    ability to serve as queues and be safely passed around. If you find yourself strug‐
    gling to understand how your concurrent code works, why a deadlock or race is
    occurring, and you’re using primitives, this is probably a good indicator that you
    should switch to channels.
Is it a performance-critical section?
    This absolutely does not mean, “I want my program to be performant, therefore I will only use mutexes.” Rather, if you have a section of your program that you have profiled, and it turns out to be a major bottleneck that is orders of magnitude slower than the rest of the program, using memory access synchronization primitives may help this critical section perform under load. This is because channels use memory access synchronization to operate, therefore they can only be slower. Before we even consider this, however, a performance-critical section might be hinting that we need to restructure our program.
*/