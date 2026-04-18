package filesystem.entities.implementations.filesystemnodes;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import filesystem.entities.interfaces.FileSystemNode;

public class Directory extends FileSystemNode {
    private ConcurrentHashMap<String, FileSystemNode> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new ConcurrentHashMap<>();
    }

    public void addChild(FileSystemNode child) { children.put(child.getName(), child); }

    public FileSystemNode getChild(String name) { return children.get(name); }

    public Map<String, FileSystemNode> getChildren() { return Collections.unmodifiableMap(children); }
}
