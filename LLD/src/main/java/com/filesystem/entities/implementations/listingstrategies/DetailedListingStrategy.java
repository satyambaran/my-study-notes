package filesystem.entities.implementations.listingstrategies;

import filesystem.entities.implementations.filesystemnodes.Directory;
import filesystem.entities.interfaces.FileSystemNode;
import filesystem.entities.interfaces.ListingStrategy;

public class DetailedListingStrategy implements ListingStrategy {
    @Override
    public void list(Directory directory) {
        for (FileSystemNode node : directory.getChildren().values()) {
            char type = (node instanceof Directory) ? 'd' : 'f';
            System.out.println(type + "\t" + node.getName() + "\t" + node.getCreateTime());
        }
    }
}
