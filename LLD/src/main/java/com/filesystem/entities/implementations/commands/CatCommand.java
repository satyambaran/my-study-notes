package filesystem.entities.implementations.commands;

import filesystem.FileSystem;
import filesystem.entities.interfaces.Command;

public class CatCommand implements Command {
    private final FileSystem fs;
    private final String path;

    CatCommand(FileSystem fs, String path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public void execute() {
        String content = fs.readFile(path);
        if (content != null && !content.isEmpty()) {
            System.out.println(content);
        }
    }
}
