package new_notes.shrayansh.lld.src.com.singleton;

public class Main {

    public static void main(String[] args) {
        Singleton singleton = Singleton.getInstance();
        singleton.showMessage(); // Outputs: "Singleton instance method called!"
    }
}

class Singleton {
    // Volatile keyword ensures that multiple threads handle the uniqueInstance
    // variable correctly
    private static volatile Singleton uniqueInstance = null;

    // Private constructor prevents instantiation from other classes
    private Singleton() {
        // Initialization code here
    }

    // Method to return the Singleton instance with double-checked locking
    public static Singleton getInstance() {
        if (uniqueInstance == null) { // First check without locking
            synchronized (Singleton.class) {
                if (uniqueInstance == null) { // Second check with locking
                    uniqueInstance = new Singleton();
                }
            }
        }
        return uniqueInstance;
    }

    // Other methods of the Singleton class
    public void showMessage() {
        System.out.println("Singleton instance method called!");
    }
}