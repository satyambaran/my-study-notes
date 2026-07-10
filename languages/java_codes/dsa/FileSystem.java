package dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * DESIGN IN-MEMORY FILE SYSTEM (LeetCode 588) — LLD refactor
 * ===========================================================
 *
 * PUBLIC API
 *   - ls(path)                       : if `path` is a directory, return its
 *                                      direct children (files and folders)
 *                                      in LEXICOGRAPHIC order. If `path` is
 *                                      a file, return [fileName].
 *   - mkdir(path)                    : create the directory, including any
 *                                      missing intermediate directories.
 *   - addContentToFile(path, content): create if missing, otherwise APPEND.
 *   - readContentFromFile(path)      : return the file's content (or "").
 *
 * DESIGN NOTES (LLD)
 * ------------------
 * 1) Type hierarchy:
 *       FileSystemNode (abstract) -- holds ONLY the shared identity: `name`.
 *       ├── Folder  -- owns children (TreeMap<String, FileSystemNode>)
 *       └── File    -- owns content  (StringBuilder)
 *
 *    Type-specific state lives on the type that needs it. This keeps LSP
 *    honest (no File carries an unused children map; no Folder carries an
 *    unused content buffer) and lets callers use real polymorphism instead
 *    of nullable-field tag checks like `node.child != null`.
 *
 * 2) Folder uses TreeMap so `ls` is O(k) over already-sorted keys
 *    (vs. HashMap + O(k log k) sort on every call).
 *
 * 3) File uses StringBuilder so appends are amortised O(m) per call
 *    (vs. O(n) per call with String concatenation).
 *
 * 4) Path parsing is isolated in PathUtils (Single Responsibility).
 *
 * 5) Traversal is a single private helper `traverse(parts, createMissing)`
 *    used by every public op (DRY — replaces the duplicated fetch/create
 *    walkers).
 *
 * 6) Bad inputs fail fast (IllegalArgumentException) instead of silently
 *    no-op'ing, which is the right default for a library API.
 *
 * COMPLEXITY (n = path depth, k = children at a level, m = content length)
 *   - ls                  : O(n log k + k)
 *   - mkdir               : O(n log k)
 *   - addContentToFile    : O(n log k + m)
 *   - readContentFromFile : O(n log k + m)
 */
public class FileSystem {

    private final Folder root;

    public FileSystem() {
        root = new Folder("");
    }

    public List<String> ls(String path) {
        FileSystemNode node = traverse(PathUtils.split(path), false);
        if (node == null) {
            return Collections.emptyList();
        }
        if (node instanceof File) {
            List<String> single = new ArrayList<>(1);
            single.add(node.getName());
            return single;
        }
        return ((Folder) node).listChildren();
    }

    public void mkdir(String path) {
        traverse(PathUtils.split(path), true);
    }

    public void addContentToFile(String path, String content) {
        String[] parts = PathUtils.split(path);
        if (parts.length == 0) {
            throw new IllegalArgumentException("cannot write to root: " + path);
        }
        String[] parentParts = new String[parts.length - 1];
        System.arraycopy(parts, 0, parentParts, 0, parentParts.length);

        FileSystemNode parentNode = traverse(parentParts, true);
        if (!(parentNode instanceof Folder)) {
            return; // a file sits where a folder was expected
        }
        Folder parent = (Folder) parentNode;
        String fileName = parts[parts.length - 1];

        FileSystemNode existing = parent.getChild(fileName);
        if (existing instanceof Folder) {
            return; // name collision with an existing folder
        }
        File file = (File) existing;
        if (file == null) {
            file = new File(fileName);
            parent.addChild(file);
        }
        file.append(content);
    }

    public String readContentFromFile(String path) {
        FileSystemNode node = traverse(PathUtils.split(path), false);
        if (!(node instanceof File)) {
            return "";
        }
        return ((File) node).read();
    }

    private FileSystemNode traverse(String[] parts, boolean createMissing) {
        FileSystemNode cur = root;
        for (String part : parts) {
            if (!(cur instanceof Folder)) {
                return null; // can't descend into a file
            }
            Folder folder = (Folder) cur;
            FileSystemNode next = folder.getChild(part);
            if (next == null) {
                if (!createMissing) {
                    return null;
                }
                next = new Folder(part);
                folder.addChild(next);
            }
            cur = next;
        }
        return cur;
    }
}

abstract class FileSystemNode {
    private final String name;

    protected FileSystemNode(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}

final class Folder extends FileSystemNode {
    private final TreeMap<String, FileSystemNode> children = new TreeMap<>();

    Folder(String name) {
        super(name);
    }

    FileSystemNode getChild(String name) {
        return children.get(name);
    }

    void addChild(FileSystemNode node) {
        children.put(node.getName(), node);
    }

    List<String> listChildren() {
        String[] childrenArray = children.keySet().toArray(new String[0]);
        return List.of(childrenArray);
        // return new ArrayList<>(children.keySet()); // true as well, but creates an unnecessary copy of the keys list
    }
}

final class File extends FileSystemNode {
    private final StringBuilder content = new StringBuilder();

    File(String name) {
        super(name);
    }

    void append(String s) {
        content.append(s);
    }

    String read() {
        return content.toString();
    }
}

final class PathUtils {
    private PathUtils() {}

    /**
     * Splits an absolute Unix-style path into its components, dropping the
     * leading empty token produced by the leading '/'.
     *
     *   "/"      -> []
     *   "/a"     -> ["a"]
     *   "/a/b"   -> ["a", "b"]
     *
     * @throws IllegalArgumentException if path is null, empty, or not absolute.
     */
    static String[] split(String path) {
        if (path == null || path.isEmpty() || path.charAt(0) != '/') {
            throw new IllegalArgumentException("path must be absolute: " + path);
        }
        if (path.equals("/")) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }
}
