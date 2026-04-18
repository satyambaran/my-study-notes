package filesystem.entities.interfaces;

import filesystem.entities.implementations.filesystemnodes.Directory;

public interface ListingStrategy {
    void list(Directory directory);
}
