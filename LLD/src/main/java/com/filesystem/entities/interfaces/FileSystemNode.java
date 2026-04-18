package filesystem.entities.interfaces;

import java.time.Instant;

import filesystem.entities.implementations.filesystemnodes.Directory;

public abstract class FileSystemNode {
    private String name;
    protected Directory parent;
    protected Instant createTime;

    public FileSystemNode(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
        createTime = Instant.now();
    }

    public String getPath() {
        if (parent == null)
            return name;
        return parent.getPath() + "/" + name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Directory getParent() { return parent; }

    public Instant getCreateTime() { return createTime; }
}
