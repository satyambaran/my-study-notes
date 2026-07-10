package dsa;

import java.util.HashMap;
import java.util.List;

public class FileSystem {
    FileSystemNode root;

    public FileSystem() {
        root = new Folder("");
    }

    private FileSystemNode fetchFolder(String path) {
        List<string> pathParsed = path.split("/");
        FileSystemNode cur = root;
        int i = 0;
        while (i < pathParsed.size() && cur.name == pathParsed.get(i)) {
            i++;
            cur = cur.child.get(pathParsed.get(i));
            if (cur == null)
                return cur;
        }
        if (i == pathParsed.size())
            return cur;
        return null;
    }

    private FileSystemNode createFolder(String path) {
        List<string> pathParsed = path.split("/");
        FileSystemNode cur = root;
        int i = 0;
        while (i < pathParsed.size() && cur.name == pathParsed.get(i)) {
            i++;
            if (pathParsed.get(i) == "")
                return null;
            cur = cur.child.get(pathParsed.get(i));
            if (cur == null) {
                if (cur.child == null)
                    return null;
                cur = new Folder(pathParsed.get(i));
            }
        }
        return cur;
    }

    public List<String> ls(String path) {
        cur = fetchFolder(path);
        if (cur == null || cur.child == null)
            return new ArryaList<>();
        else
            return cur.child.keySet().toList();
    }

    public void mkdir(String path) {
        if (path.get(0) != '/')
            return;
        createFolder(path);
    }

    public void addContentToFile(String path, String content) {
        int pos = path.lastIndexOf('/');
        string fileName = path.substring(pos);
        string filePath = path.substring(0, pos);
        FileSystemNode folder = createFolder(filePath);
        if(folder==null) return;
        if(folder.child.containsKey(fileName)){
            FileSystemNode node = folder.child.get(fileName);
            if(node.child!=null)return;
            // string prevContent= folder.child.get(fileName);
            node.content = node.content+content;
            folder.child.put(fileName, node);
        }else{
            File file= new File(fileName, content); 
            folder.child.put(fileName, file)
        }
    }

    public String readContentFromFile(String path) {
        int pos = path.lastIndexOf('/');
        string fileName = path.substring(pos);
        string filePath = path.substring(0, pos);
        FileSystemNode folder = fetchFolder(filePath);
        if (folder == null || folder.child == null || !folder.containsKey(fileName))
            return "";
        FileSystemNode node = folder.containsKey(fileName);
        if (node.content != null) {
            return node.content;
        }
        return "";
    }

}

class FileSystemNode {
    string name;
    string content;
    HashMap<string, FileSystemNode> child;

    FileSystemNode(string name) {
        this.name = name;
    }
}

class File extends FileSystemNode {
    File(string name, string content) {
        // if(name.contains(".")){
        // System.out.println("incorrect path");
        // return;
        // }
        super(name);
        this.content = content;
    }
}

class Folder extends FileSystemNode {
    Folder(string name) {
        super(name);
        child = new HashMap<>();
    }
}
