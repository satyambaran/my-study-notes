package docs.concurrency;

/**
 * ============================================================
 * COORDINATION MECHANISM COMPARISON
 * ============================================================
 *
 * This file compares four common Java coordination styles:
 * - wait/notify/notifyAll
 * - Lock + Condition
 * - BlockingQueue
 * - CompletableFuture
 *
 * THEY SOLVE DIFFERENT SHAPES OF PROBLEMS.
 * Picking the wrong one usually creates unnecessary complexity.
 */
public class CoordinationMechanismComparison {

    public static void main(String[] args) {
        printOverview();
        printDecisionGuide();
    }

    static void printOverview() {
        System.out.println("=== Coordination Mechanism Comparison ===");
        System.out.println();

        System.out.println("1. wait / notify / notifyAll");
        System.out.println("   Best for: low-level monitor coordination tied to synchronized blocks");
        System.out.println("   Model: threads sleep on an object's monitor until another thread signals");
        System.out.println("   Strengths: built into the language, no extra abstractions needed");
        System.out.println("   Weaknesses: easy to misuse, must hold monitor, must use while-loops");
        System.out.println("   Use when: learning fundamentals or maintaining old monitor-based code");
        System.out.println();

        System.out.println("2. Lock + Condition");
        System.out.println("   Best for: explicit locking with multiple wait-sets / conditions");
        System.out.println("   Model: ReentrantLock plus condition objects like notEmpty / notFull");
        System.out.println("   Strengths: more expressive than wait/notify, multiple condition queues");
        System.out.println("   Weaknesses: still low-level, still easy to get wrong if overused");
        System.out.println("   Use when: you need lock features like tryLock, fairness, or separate conditions");
        System.out.println();

        System.out.println("3. BlockingQueue");
        System.out.println("   Best for: producer-consumer pipelines");
        System.out.println("   Model: queue handles waiting, waking, and bounded capacity for you");
        System.out.println("   Strengths: high-level, safe, simpler than manual signaling");
        System.out.println("   Weaknesses: specialized to queue-shaped workflows");
        System.out.println("   Use when: one side produces units of work and another side consumes them");
        System.out.println();

        System.out.println("4. CompletableFuture");
        System.out.println("   Best for: async result pipelines and composition");
        System.out.println("   Model: tasks complete values, then dependent stages continue");
        System.out.println("   Strengths: composition, chaining, combining, error handling");
        System.out.println("   Weaknesses: not a drop-in replacement for shared-state coordination");
        System.out.println("   Use when: the problem is about async computation flow, not lock-based waiting");
        System.out.println();
    }

    static void printDecisionGuide() {
        System.out.println("=== Decision Guide ===");
        System.out.println(
                "If your problem is 'I have work items flowing from producer to consumer' -> use BlockingQueue.");
        System.out.println(
                "If your problem is 'I need to coordinate on a lock with several distinct conditions' -> use Condition.");
        System.out.println(
                "If your problem is 'I need to chain async computations that return results' -> use CompletableFuture.");
        System.out.println(
                "If your problem is 'I am inside old synchronized monitor code already' -> wait/notify may be appropriate.");
        System.out.println();
        System.out.println("Default preference in modern code:");
        System.out.println("BlockingQueue over manual producer-consumer wait/notify.");
        System.out.println("CompletableFuture over ad-hoc callback coordination.");
        System.out.println(
                "Condition only when monitor methods are too limiting and you truly need explicit lock control.");
    }
}