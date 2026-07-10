package dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class FileSystem2 {
    private final Folder root;

    FileSystem2() {
        root = new Folder("/");
    }

    private FileSystemNode traverse(String filePath, boolean createMissing) {
        String[] path = splitPath(filePath);
        FileSystemNode cur = root;
        for (int i = 1; i < path.length; i++) {
            if (!(cur instanceof Folder)) {
                return null;
            }
            Folder folder = (Folder) cur;
            FileSystemNode next = folder.getChild(path[i]);
            if (next == null) {
                if (!createMissing) {
                    return null;
                }
                next = new Folder(path[i]);
                folder.addChild(next);
            }
            cur = next;
        }
        return cur;
    }

    private String[] splitPath(String path) {
        if (path == "/")
            return new String[0];
        return path.split("/");
    }
}

abstract class FileSystemNode {
    String name;

    protected FileSystemNode(String name) {
        this.name = name;
    }

    // public void setName(String name) {
    // this.name = name;
    // }

    public String getName() {
        return name;
    }
}

class File extends FileSystemNode {
    private final StringBuilder content = new StringBuilder();

    File(String name) {
        super(name);
    }

    public String read() {
        return content.toString();
    }

    public void append(String content) {
        this.content.append(content);
    }

}

class Folder extends FileSystemNode {
    private final TreeMap<String, FileSystemNode> children = new TreeMap<>();

    Folder(String name) {
        super(name);
    }

    public FileSystemNode getChild(String name) {
        return children.get(name);
    }

    public void addChild(FileSystemNode node) {
        children.put(node.name, node);
    }

    public List<String> getAllChild() {
        String[] arrayList = children.keySet().toArray(new String[0]);
        return List.of(arrayList);
        // return new ArrayList<>(children.keySet());
    }
}