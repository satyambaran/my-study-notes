package docs.concurrency;

/**
 * ============================================================
 * JAVA CONCURRENCY INTERVIEW Q&A
 * ============================================================
 *
 * This file is intentionally direct and revision-oriented.
 * Read the printed answers first, then go back to the deeper parts for code.
 */
public class ConcurrencyInterviewQA {

    public static void main(String[] args) {
        qa("What is the difference between process and thread?",
                "A process is an OS-level running program. A thread is an execution path inside a process. Threads in the same process share heap memory, which makes communication easy but also creates race-condition risk.");

        qa("What is the difference between Thread and Runnable?",
                "Thread is the worker mechanism. Runnable is the task. Prefer Runnable so task definition stays separate from execution policy.");

        qa("When should you use Callable instead of Runnable?",
                "Use Callable when the task must return a result or throw a checked exception. Runnable has only run() and no result.");

        qa("What does Future represent?",
                "Future is a handle to the result of asynchronous work. You can wait using get(), cancel the task, and inspect done/cancelled status.");

        qa("Why is ExecutorService preferred over creating raw threads everywhere?",
                "It reuses worker threads, centralizes task submission, reduces thread-creation overhead, and gives lifecycle control like shutdown and awaitTermination.");

        qa("What problem does synchronized solve?",
                "Mutual exclusion and visibility. It prevents multiple threads from executing a protected critical section at the same time, and monitor exit/entry creates a happens-before visibility guarantee.");

        qa("What is a race condition?",
                "A race condition happens when correctness depends on unpredictable timing between threads, usually because shared mutable state is accessed without proper coordination.");

        qa("Why is count++ unsafe across threads?",
                "Because it is not atomic. It expands to read, add, write. Two threads can read the same old value and one increment gets lost.");

        qa("What does volatile guarantee, and what does it not guarantee?",
                "volatile guarantees visibility of the latest write and ordering constraints around that variable. It does not make compound actions like count++ atomic.");

        qa("What is happens-before in simple terms?",
                "It is the Java Memory Model guarantee that one action's effects become visible to another action. Without happens-before, another thread may observe stale data.");

        qa("What is the difference between sleep() and wait()?",
                "sleep() pauses a thread for time and does not release locks. wait() must be called while holding a monitor, releases that monitor, and is used for condition-based coordination.");

        qa("Why should wait() almost always be inside a while-loop?",
                "Because of spurious wakeups, notifyAll waking many threads, and races where another thread consumes the condition before the current thread reacquires the lock.");

        qa("When is notifyAll() safer than notify()?",
                "When multiple kinds of waiters may be blocked or when you are not certain exactly one correct waiter exists. notify() can wake the wrong waiter and stall progress.");

        qa("When would you use BlockingQueue instead of wait/notify?",
                "For producer-consumer pipelines. BlockingQueue already handles capacity, waiting, waking, and thread safety, so it is less error-prone.");

        qa("When would you use CompletableFuture?",
                "When you need asynchronous computations that return values and need composition: transform, combine, recover from errors, or chain dependent stages.");

        qa("What are common concurrency interview traps?",
                "Confusing visibility with atomicity, swallowing InterruptedException, using if instead of while around wait, overusing shared mutable state, assuming HashMap is thread-safe, and thinking volatile fixes all thread-safety issues.");

        qa("What are good default best practices?",
                "Prefer immutable data, minimize shared state, use higher-level utilities like ExecutorService and BlockingQueue, restore interrupts when caught, and choose the simplest synchronization mechanism that matches the problem shape.");
    }

    static void qa(String question, String answer) {
        System.out.println("Q: " + question);
        System.out.println("A: " + answer);
        System.out.println();
    }
}