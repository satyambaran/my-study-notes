/*
Snake - ladder:
    deque<Player> 
    Try to maintain cell details
*/
/*
    Singleton pattern issue:
        Reordering: 
            # order 1 (memory over head)
                Allocate memory
                Initialize variable
                assign pointer
            # order 2 (other thread might use un-assigned ones)
                Allocate memory
                assign pointer
                Initialize variable
        L1 Cache:
            Each core have their own cache
            Each core cache sync up with memory and other cache at a frequency and dump their data to each other in memory
        use VOLATILE keyword, which only stays in memory(read/write)
            Can only be read and write from memory
            Before assigning to valatile key, all the instruction before must be completed and cache must be done in memory
            Brings ordering
    Singleton without Synchronize:


    Factory Pattern:
        ShapeFactory{
            switch(shapeType){
                case ShapeType.Cirlce: return new Circle();
                case ShapeType.Rectangle: return new Circle();
                default: return null;
            }
        }
    Abstract Factory:
        factory of factory    
*/