
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class r1 {

    enum Priority {
        HIGH,
        LOW
    }

    static class Task {
        final String id;
        final int start;
        final int end;
        final int cpu;
        final Priority priority;

        Task(String id, int start, int end, int cpu, Priority priority) {
            if (start >= end) {
                throw new IllegalArgumentException("start must be smaller than end");
            }
            if (cpu <= 0) {
                throw new IllegalArgumentException("cpu must be positive");
            }
            this.id = id;
            this.start = start;
            this.end = end;
            this.cpu = cpu;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return id + "[" + start + ", " + end + ") cpu=" + cpu + " " + priority;
        }
    }

    private static class Solver {
        private final int[] remainingCapacity;
        private final int[] currentLowLoad;
        private final List<Task> lowTasks;
        private final int[][] taskSegments;
        private final boolean[] removed;
        private int bestRemovals;

        Solver(int[] remainingCapacity, int[] currentLowLoad, List<Task> lowTasks, int[][] taskSegments) {
            this.remainingCapacity = remainingCapacity;
            this.currentLowLoad = currentLowLoad;
            this.lowTasks = lowTasks;
            this.taskSegments = taskSegments;
            this.removed = new boolean[lowTasks.size()];
            this.bestRemovals = lowTasks.size() + 1;
        }

        int solve() {
            dfs(0);
            return bestRemovals == lowTasks.size() + 1 ? -1 : bestRemovals;
        }

        private void dfs(int removedCount) {
            if (removedCount >= bestRemovals) {
                return;
            }

            int overloadedSegment = findMostOverloadedSegment();
            if (overloadedSegment == -1) {
                bestRemovals = removedCount;
                return;
            }

            int overload = currentLowLoad[overloadedSegment] - remainingCapacity[overloadedSegment];
            int[] candidates = buildCandidates(overloadedSegment);
            if (candidates.length == 0) {
                return;
            }

            int releasedByCandidates = 0;
            for (int candidate : candidates) {
                releasedByCandidates += lowTasks.get(candidate).cpu;
            }
            if (releasedByCandidates < overload) {
                return;
            }

            for (int index = candidates.length - 1; index >= 0; index--) {
                int taskIndex = candidates[index];
                if (removed[taskIndex]) {
                    continue;
                }
                removeTask(taskIndex);
                dfs(removedCount + 1);
                restoreTask(taskIndex);
            }
        }

        private int findMostOverloadedSegment() {
            int selected = -1;
            int maxOverload = 0;
            for (int segment = 0; segment < remainingCapacity.length; segment++) {
                int overload = currentLowLoad[segment] - remainingCapacity[segment];
                if (overload > maxOverload) {
                    maxOverload = overload;
                    selected = segment;
                }
            }
            return selected;
        }

        private int[] buildCandidates(int segment) {
            List<Integer> candidates = new ArrayList<>();
            for (int taskIndex = 0; taskIndex < lowTasks.size(); taskIndex++) {
                if (removed[taskIndex]) {
                    continue;
                }
                for (int taskSegment : taskSegments[taskIndex]) {
                    if (taskSegment == segment) {
                        candidates.add(taskIndex);
                        break;
                    }
                }
            }

            candidates.sort((left, right) -> {
                Task first = lowTasks.get(left);
                Task second = lowTasks.get(right);
                if (first.cpu != second.cpu) {
                    return Integer.compare(first.cpu, second.cpu);
                }
                return Integer.compare(taskSegments[left].length, taskSegments[right].length);
            });

            int[] result = new int[candidates.size()];
            for (int i = 0; i < candidates.size(); i++) {
                result[i] = candidates.get(i);
            }
            return result;
        }

        private void removeTask(int taskIndex) {
            removed[taskIndex] = true;
            int cpu = lowTasks.get(taskIndex).cpu;
            for (int segment : taskSegments[taskIndex]) {
                currentLowLoad[segment] -= cpu;
            }
        }

        private void restoreTask(int taskIndex) {
            removed[taskIndex] = false;
            int cpu = lowTasks.get(taskIndex).cpu;
            for (int segment : taskSegments[taskIndex]) {
                currentLowLoad[segment] += cpu;
            }
        }
    }

    public static int minimumLowPriorityRemovals(List<Task> tasks, int totalCpuCores) {
        if (totalCpuCores < 0) {
            throw new IllegalArgumentException("totalCpuCores cannot be negative");
        }
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }

        int[] times = collectTimes(tasks);
        if (times.length < 2) {
            return 0;
        }

        Map<Integer, Integer> timeToIndex = new HashMap<>();
        for (int index = 0; index < times.length; index++) {
            timeToIndex.put(times[index], index);
        }

        int segmentCount = times.length - 1;
        int[] remainingCapacity = new int[segmentCount];
        Arrays.fill(remainingCapacity, totalCpuCores);
        List<Task> lowTasks = new ArrayList<>();
        List<int[]> lowTaskSegments = new ArrayList<>();
        int[] currentLowLoad = new int[segmentCount];

        for (Task task : tasks) {
            int startIndex = timeToIndex.get(task.start);
            int endIndex = timeToIndex.get(task.end);
            if (task.priority == Priority.HIGH) {
                for (int segment = startIndex; segment < endIndex; segment++) {
                    remainingCapacity[segment] -= task.cpu;
                    if (remainingCapacity[segment] < 0) {
                        return -1;
                    }
                }
            } else {
                lowTasks.add(task);
                int[] segments = new int[endIndex - startIndex];
                int pointer = 0;
                for (int segment = startIndex; segment < endIndex; segment++) {
                    currentLowLoad[segment] += task.cpu;
                    segments[pointer++] = segment;
                }
                lowTaskSegments.add(segments);
            }
        }

        if (lowTasks.isEmpty()) {
            return 0;
        }

        Solver solver = new Solver(
                remainingCapacity,
                currentLowLoad,
                lowTasks,
                lowTaskSegments.toArray(new int[0][]));
        return solver.solve();
    }

    private static int[] collectTimes(List<Task> tasks) {
        int[] rawTimes = new int[tasks.size() * 2];
        int pointer = 0;
        for (Task task : tasks) {
            rawTimes[pointer++] = task.start;
            rawTimes[pointer++] = task.end;
        }
        Arrays.sort(rawTimes);

        int unique = 0;
        for (int time : rawTimes) {
            if (unique == 0 || rawTimes[unique - 1] != time) {
                rawTimes[unique++] = time;
            }
        }
        return Arrays.copyOf(rawTimes, unique);
    }

    private static void runTest(String name, List<Task> tasks, int totalCpuCores, int expected) {
        int actual = minimumLowPriorityRemovals(tasks, totalCpuCores);
        if (actual != expected) {
            throw new AssertionError(name + " failed: expected=" + expected + ", actual=" + actual);
        }
        System.out.println(name + " passed");
    }

    private static void runExceptionTest(String name, Runnable runnable, Class<? extends Throwable> expected) {
        try {
            runnable.run();
            throw new AssertionError(name + " failed: expected exception " + expected.getSimpleName());
        } catch (Throwable throwable) {
            if (!expected.isInstance(throwable)) {
                throw new AssertionError(
                        name + " failed: expected exception " + expected.getSimpleName() + ", actual="
                                + throwable.getClass().getSimpleName(),
                        throwable);
            }
            System.out.println(name + " passed");
        }
    }

    public static void main(String[] args) {
        runTest(
                "no-removal-needed",
                Arrays.asList(
                        new Task("high-a", 1, 4, 3, Priority.HIGH),
                        new Task("low-a", 1, 2, 2, Priority.LOW),
                        new Task("low-b", 2, 4, 4, Priority.LOW)),
                10,
                0);

        runTest(
                "one-low-task-removal",
                Arrays.asList(
                        new Task("high-a", 1, 5, 3, Priority.HIGH),
                        new Task("low-a", 1, 2, 4, Priority.LOW),
                        new Task("low-b", 1, 4, 6, Priority.LOW),
                        new Task("low-c", 2, 4, 1, Priority.LOW)),
                10,
                1);

        runTest(
                "two-low-task-removals",
                Arrays.asList(
                        new Task("high-a", 1, 4, 5, Priority.HIGH),
                        new Task("low-a", 1, 4, 3, Priority.LOW),
                        new Task("low-b", 1, 4, 3, Priority.LOW),
                        new Task("low-c", 1, 4, 3, Priority.LOW)),
                10,
                2);

        runTest(
                "high-priority-alone-overloads",
                Arrays.asList(
                        new Task("high-a", 1, 3, 7, Priority.HIGH),
                        new Task("high-b", 1, 3, 5, Priority.HIGH),
                        new Task("low-a", 1, 3, 1, Priority.LOW)),
                10,
                -1);

        runTest("empty-task-list", Arrays.asList(), 8, 0);

        runExceptionTest(
                "negative-total-capacity",
                () -> minimumLowPriorityRemovals(Arrays.asList(), -1),
                IllegalArgumentException.class);

        runExceptionTest(
                "invalid-task-window",
                () -> new Task("bad", 3, 3, 1, Priority.LOW),
                IllegalArgumentException.class);

        System.out.println("all tests passed");
    }
}
/*
 * class tasks: start, end, cpu, priority(high or low)
 * input: list<tasks>, totalCpuCores
 * 
 * we can remove low priority tasks
 * figure out the minimum number of tasks to remove
 * to ensure the total CPU usage does not exceed the available cores
 */