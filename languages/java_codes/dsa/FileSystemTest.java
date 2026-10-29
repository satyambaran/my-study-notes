package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Functional unit tests for {@link FileSystem} (LeetCode 588 — Design
 * In-Memory File System).
 *
 * Run with:
 *   cd /Users/satyambaran/Documents/my-study-notes/languages/java_codes
 *   javac dsa/FileSystem.java dsa/FileSystemTest.java
 *   java dsa.FileSystemTest
 *
 * What we test (and why):
 *   1.  ls on a fresh root returns an empty list.
 *   2.  LeetCode 588's worked example end-to-end (mkdir / addContent /
 *       read / ls round-trip).
 *   3.  ls of a file path returns [fileName] (not the file's content).
 *   4.  ls returns children in LEXICOGRAPHIC order (TreeMap contract).
 *   5.  mkdir is idempotent — re-creating an existing dir keeps its
 *       contents intact.
 *   6.  mkdir creates all missing intermediate directories ("mkdir -p").
 *   7.  addContentToFile creates the file when it is missing.
 *   8.  addContentToFile APPENDS (does not overwrite) on subsequent calls.
 *   9.  addContentToFile auto-creates missing parent directories.
 *   10. addContentToFile is a no-op when the leaf name is already a folder
 *       (no silent corruption of the folder).
 *   11. mkdir under a file is a no-op and leaves the file's content intact
 *       (no silent corruption of the file).
 *   12. readContentFromFile returns "" for a missing file (LeetCode
 *       contract — empty, not null, not an exception).
 *   13. readContentFromFile returns "" when the path resolves to a folder
 *       (folders have no readable content).
 *   14. Root-level files (e.g. "/file") work for both write and read.
 *   15. ls on a missing directory returns an empty list.
 *   16. Many siblings stay lex-sorted regardless of insertion order
 *       (stress for the TreeMap contract).
 *   17. Deep nesting (long paths) works without stack/loop bugs.
 *   18. Repeated small appends produce the right concatenation
 *       (regression for the old O(n^2) String-concat append).
 *   19. Fail-fast on bad input: null / empty / non-absolute paths throw
 *       IllegalArgumentException from every public method.
 *   20. addContentToFile("/", ...) is rejected — root isn't a file.
 *   21. The list returned by ls is UNMODIFIABLE — callers cannot mutate it
 *       to corrupt the file-system's internal state.
 */
public final class FileSystemTest {

    // ---- tiny test harness (no JUnit dependency) ----
    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        run("freshRootIsEmpty",                FileSystemTest::freshRootIsEmpty);
        run("leetcode588Example",              FileSystemTest::leetcode588Example);
        run("lsOfFilePathReturnsFileName",     FileSystemTest::lsOfFilePathReturnsFileName);
        run("lsReturnsLexicographicOrder",     FileSystemTest::lsReturnsLexicographicOrder);
        run("mkdirIsIdempotent",               FileSystemTest::mkdirIsIdempotent);
        run("mkdirCreatesIntermediateDirs",    FileSystemTest::mkdirCreatesIntermediateDirs);
        run("addContentCreatesMissingFile",    FileSystemTest::addContentCreatesMissingFile);
        run("addContentAppendsDoesNotReplace", FileSystemTest::addContentAppendsDoesNotReplace);
        run("addContentCreatesParentDirs",     FileSystemTest::addContentCreatesParentDirs);
        run("addContentNoOpWhenNameIsFolder",  FileSystemTest::addContentNoOpWhenNameIsFolder);
        run("mkdirUnderFileIsNoOp",            FileSystemTest::mkdirUnderFileIsNoOp);
        run("readMissingFileReturnsEmpty",     FileSystemTest::readMissingFileReturnsEmpty);
        run("readFolderReturnsEmpty",          FileSystemTest::readFolderReturnsEmpty);
        run("rootLevelFileRoundTrip",          FileSystemTest::rootLevelFileRoundTrip);
        run("lsMissingDirReturnsEmpty",        FileSystemTest::lsMissingDirReturnsEmpty);
        run("manySiblingsStayLexSorted",       FileSystemTest::manySiblingsStayLexSorted);
        run("deepNestingWorks",                FileSystemTest::deepNestingWorks);
        run("manyAppendsAccumulate",           FileSystemTest::manyAppendsAccumulate);
        run("failFastOnBadInputs",             FileSystemTest::failFastOnBadInputs);
        run("addContentToRootIsRejected",      FileSystemTest::addContentToRootIsRejected);
        run("lsReturnIsUnmodifiable",          FileSystemTest::lsReturnIsUnmodifiable);

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.out.println("Failures:");
            for (String f : failures)
                System.out.println("  - " + f);
            System.exit(1);
        }
    }

    // ---- tests ----

    static void freshRootIsEmpty() {
        FileSystem fs = new FileSystem();
        assertEquals(Collections.emptyList(), fs.ls("/"), "fresh fs ls(\"/\") is empty");
    }

    static void leetcode588Example() {
        FileSystem fs = new FileSystem();
        assertEquals(Collections.emptyList(), fs.ls("/"), "initial ls /");
        fs.mkdir("/a/b/c");
        fs.addContentToFile("/a/b/c/d", "hello");
        assertEquals(Arrays.asList("a"), fs.ls("/"), "ls / after mkdir+write");
        assertEquals("hello", fs.readContentFromFile("/a/b/c/d"), "read after write");
        fs.addContentToFile("/a/b/c/d", " world");
        assertEquals("hello world", fs.readContentFromFile("/a/b/c/d"), "append concatenates");
        assertEquals(Arrays.asList("d"), fs.ls("/a/b/c"), "ls of containing dir lists the file");
    }

    static void lsOfFilePathReturnsFileName() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/dir/note.txt", "x");
        assertEquals(Arrays.asList("note.txt"), fs.ls("/dir/note.txt"),
                "ls on a file path returns [fileName]");
    }

    static void lsReturnsLexicographicOrder() {
        FileSystem fs = new FileSystem();
        // Insert deliberately out of order; ls must sort.
        fs.mkdir("/x/z");
        fs.mkdir("/x/a");
        fs.mkdir("/x/m");
        fs.addContentToFile("/x/file", "x");
        assertEquals(Arrays.asList("a", "file", "m", "z"), fs.ls("/x"),
                "ls returns children in lexicographic order");
    }

    static void mkdirIsIdempotent() {
        FileSystem fs = new FileSystem();
        fs.mkdir("/a/b");
        fs.addContentToFile("/a/b/file", "data");
        fs.mkdir("/a/b"); // must not wipe contents
        assertEquals(Arrays.asList("file"), fs.ls("/a/b"), "contents preserved after re-mkdir");
        assertEquals("data", fs.readContentFromFile("/a/b/file"), "file preserved after re-mkdir");
    }

    static void mkdirCreatesIntermediateDirs() {
        FileSystem fs = new FileSystem();
        fs.mkdir("/p/q/r/s");
        assertEquals(Arrays.asList("p"), fs.ls("/"), "p exists at root");
        assertEquals(Arrays.asList("q"), fs.ls("/p"), "q exists under p");
        assertEquals(Arrays.asList("r"), fs.ls("/p/q"), "r exists under p/q");
        assertEquals(Arrays.asList("s"), fs.ls("/p/q/r"), "s exists under p/q/r");
        assertEquals(Collections.emptyList(), fs.ls("/p/q/r/s"), "s is empty");
    }

    static void addContentCreatesMissingFile() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/a/b/file", "first");
        assertEquals("first", fs.readContentFromFile("/a/b/file"), "missing file created on first write");
    }

    static void addContentAppendsDoesNotReplace() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/log", "one ");
        fs.addContentToFile("/log", "two ");
        fs.addContentToFile("/log", "three");
        assertEquals("one two three", fs.readContentFromFile("/log"),
                "repeated writes APPEND in order");
    }

    static void addContentCreatesParentDirs() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/auto/created/dirs/file", "payload");
        assertEquals(Arrays.asList("created"), fs.ls("/auto"), "parent /auto auto-created");
        assertEquals(Arrays.asList("dirs"),    fs.ls("/auto/created"), "parent /auto/created auto-created");
        assertEquals(Arrays.asList("file"),    fs.ls("/auto/created/dirs"), "file present in deepest dir");
        assertEquals("payload", fs.readContentFromFile("/auto/created/dirs/file"), "content readable");
    }

    static void addContentNoOpWhenNameIsFolder() {
        FileSystem fs = new FileSystem();
        fs.mkdir("/shared");
        fs.addContentToFile("/shared", "should not stick"); // /shared is a folder
        // The folder must still exist as a folder (ls returns children, not [fileName]).
        assertEquals(Collections.emptyList(), fs.ls("/shared"),
                "folder /shared remains a folder, not silently overwritten");
        assertEquals("", fs.readContentFromFile("/shared"), "reading a folder yields empty");
    }

    static void mkdirUnderFileIsNoOp() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/a/file", "important");
        fs.mkdir("/a/file/sub"); // /a/file is a file — must not corrupt
        assertEquals("important", fs.readContentFromFile("/a/file"),
                "file content intact after mkdir under it");
        assertEquals(Arrays.asList("file"), fs.ls("/a"),
                "no new entry leaked into parent");
        // ls on the file path still returns [fileName] (not a children list).
        assertEquals(Arrays.asList("file"), fs.ls("/a/file"), "file still recognized as a file");
    }

    static void readMissingFileReturnsEmpty() {
        FileSystem fs = new FileSystem();
        assertEquals("", fs.readContentFromFile("/nope"), "missing root-level file -> empty");
        assertEquals("", fs.readContentFromFile("/a/b/c/d"), "missing nested file -> empty");
        fs.mkdir("/a/b");
        assertEquals("", fs.readContentFromFile("/a/b/missing"), "missing leaf under existing dir -> empty");
    }

    static void readFolderReturnsEmpty() {
        FileSystem fs = new FileSystem();
        fs.mkdir("/dir");
        assertEquals("", fs.readContentFromFile("/dir"), "reading a folder path returns \"\"");
    }

    static void rootLevelFileRoundTrip() {
        FileSystem fs = new FileSystem();
        fs.addContentToFile("/rootfile", "rf");
        assertEquals("rf", fs.readContentFromFile("/rootfile"), "root-level file readable");
        assertEquals(Arrays.asList("rootfile"), fs.ls("/"), "root-level file listed in ls /");
    }

    static void lsMissingDirReturnsEmpty() {
        FileSystem fs = new FileSystem();
        assertEquals(Collections.emptyList(), fs.ls("/no/such/path"), "missing path -> empty list");
    }

    static void manySiblingsStayLexSorted() {
        FileSystem fs = new FileSystem();
        // Insert 50 siblings in random-ish order.
        String[] inserted = {"m", "a", "z", "c", "b", "y", "n", "d", "x", "e",
                             "w", "f", "v", "g", "u", "h", "t", "i", "s", "j",
                             "r", "k", "q", "l", "p", "o", "aa", "az", "ab", "ay",
                             "ba", "bz", "ca", "cz", "da", "dz", "ea", "ez", "fa", "fz",
                             "ga", "gz", "ha", "hz", "ia", "iz", "ja", "jz", "ka", "kz"};
        for (String n : inserted) fs.mkdir("/big/" + n);
        List<String> got = fs.ls("/big");
        List<String> expected = new ArrayList<>(Arrays.asList(inserted));
        Collections.sort(expected);
        assertEquals(expected, got, "all " + inserted.length + " siblings lex-sorted");
    }

    static void deepNestingWorks() {
        FileSystem fs = new FileSystem();
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < 100; i++) path.append("/d").append(i);
        fs.mkdir(path.toString());
        fs.addContentToFile(path + "/leaf", "deep");
        assertEquals("deep", fs.readContentFromFile(path + "/leaf"),
                "100-level deep file is reachable");
        assertEquals(Arrays.asList("leaf"), fs.ls(path.toString()),
                "deep dir lists its single file");
    }

    static void manyAppendsAccumulate() {
        FileSystem fs = new FileSystem();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String chunk = "[" + i + "]";
            fs.addContentToFile("/big.log", chunk);
            expected.append(chunk);
        }
        assertEquals(expected.toString(), fs.readContentFromFile("/big.log"),
                "1000 appends concatenate in order");
    }

    static void failFastOnBadInputs() {
        FileSystem fs = new FileSystem();
        // ls
        assertThrows(IllegalArgumentException.class, () -> fs.ls(null), "ls(null) throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.ls(""),   "ls(\"\") throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.ls("relative"), "ls(relative) throws IAE");
        // mkdir
        assertThrows(IllegalArgumentException.class, () -> fs.mkdir(null), "mkdir(null) throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.mkdir(""),   "mkdir(\"\") throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.mkdir("a/b"), "mkdir(relative) throws IAE");
        // addContentToFile
        assertThrows(IllegalArgumentException.class, () -> fs.addContentToFile(null, "x"), "addContent(null) throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.addContentToFile("",   "x"), "addContent(\"\") throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.addContentToFile("rel", "x"), "addContent(relative) throws IAE");
        // readContentFromFile
        assertThrows(IllegalArgumentException.class, () -> fs.readContentFromFile(null), "read(null) throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.readContentFromFile(""),   "read(\"\") throws IAE");
        assertThrows(IllegalArgumentException.class, () -> fs.readContentFromFile("rel"), "read(relative) throws IAE");
    }

    static void addContentToRootIsRejected() {
        FileSystem fs = new FileSystem();
        assertThrows(IllegalArgumentException.class,
                () -> fs.addContentToFile("/", "x"),
                "writing to root \"/\" is rejected (root is not a file)");
    }

    static void lsReturnIsUnmodifiable() {
        FileSystem fs = new FileSystem();
        fs.mkdir("/a");
        fs.mkdir("/b");
        List<String> snapshot = fs.ls("/");
        assertEquals(Arrays.asList("a", "b"), snapshot, "initial ls snapshot");
        // The returned list is unmodifiable so callers cannot corrupt the
        // internal directory structure by mutating it.
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add("ghost"),
                "ls() result must reject add()");
        assertThrows(UnsupportedOperationException.class,
                snapshot::clear,
                "ls() result must reject clear()");
        // And of course the internal state is untouched either way.
        assertEquals(Arrays.asList("a", "b"), fs.ls("/"),
                "internal state preserved after attempted mutation");
    }

    // ---- assertion helpers ----

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (AssertionError ae) {
            failed++;
            failures.add(name + ": " + ae.getMessage());
            System.out.println("FAIL  " + name + " -> " + ae.getMessage());
        } catch (Throwable t) {
            failed++;
            failures.add(name + ": unexpected " + t);
            System.out.println("ERROR " + name + " -> " + t);
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    @SuppressWarnings("unused")
    private static void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new AssertionError(msg);
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) return;
            throw new AssertionError(msg + " (expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
        }
        throw new AssertionError(msg + " (expected " + expected.getSimpleName() + " but nothing was thrown)");
    }
}
