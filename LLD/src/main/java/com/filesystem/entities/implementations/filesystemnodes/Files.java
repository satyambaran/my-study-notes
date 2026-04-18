package filesystem.entities.implementations.filesystemnodes;

import filesystem.entities.interfaces.FileSystemNode;

public class Files extends FileSystemNode {
    private String content = "";

    public Files(String name, Directory parent) {
        super(name, parent);
        this.content = "";
    }

    public String getContent() { return this.content; }

    public void setContent(String content) { this.content = content; }
}
