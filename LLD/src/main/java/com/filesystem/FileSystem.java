package filesystem;

import filesystem.entities.implementations.filesystemnodes.Directory;

public class FileSystem {
    private static volatile FileSystem instance;
    private final Directory root;
    private Directory currentDirectory;

    private FileSystem() {
        this.root = new Directory("/", null);
        this.currentDirectory = root;
    }

    public FileSystem getInstance() {
        if (instance == null) {
            synchronized (FileSystem.class) {
                if (instance == null) {
                    instance = new FileSystem();
                }
            }
        }
        return instance;
    }

    public void createDirectory(String name) { createNode(name, true); }

    public void createFile(String name) { createNode(name, false); }

    private void createNode(String name, boolean isDirectory) {}

    public String readFile(String path) {}
}
