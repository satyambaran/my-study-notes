package filesystem.entities.implementations.listingstrategies;

import filesystem.entities.implementations.filesystemnodes.Directory;
import filesystem.entities.interfaces.ListingStrategy;

public class SimpleListingSrategy implements ListingStrategy {
    @Override
    public void list(Directory directory) {
        directory.getChildren().keySet().forEach((name) -> System.out.println(name + ""));
        System.out.println();
    }
}
