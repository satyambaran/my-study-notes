public class Singleton {
    private static volatile Singleton singleton = null;
    private static Object obj = new Object();

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (singleton == null) {
            synchronized (obj) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }
}
