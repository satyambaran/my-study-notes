/*
    Multi-Tasking:
        Allows multiple activites to occur concurrently at a computer.
        Type:
            Thread-based mutlitasking:
                Task happening in a process
                    ~ Formatting and error correcting in MS Word
            Process-based mutlitasking:
                Running processes
    Threads vs Processes:
        Threads:
            An independent sequential path of execution within a program.
            Many threads can run concurrently within a program
            Threads in a program exists in common memory space, hence can share both data and code
            Hence, context switching between threads is less expensive than between process
            Higher communication between threads
            Light weighted compare to process
    Single Threading:
        Only one task at a time can be performed
        CPU cycles are wasted
            e.g. while waiting for user input
    Multi-Threading:
        Allows idle CPU time to good use

    Threads:
        * Creating threads
        * Accessing common data and code through synchronization
        * Transitioning between thread states
    ` Main Thread:
        A thread automatically gets created automatically to execute the main() method.
        If no other user threads are spawned, program terminates when main() method finishes executing
        All other thread are spawned from main thread, called child thread
        Main() method can finish, but program will keep running until all user threads have completed

        Runtime`
    
    







 */
