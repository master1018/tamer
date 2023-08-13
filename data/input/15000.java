class x123 {
    static {
        InitArg.x123Initialized = true;
    }
}
public class InitArg {
    public static boolean x123Initialized = false;
    public static void main(String[] args) throws Exception {
        Class c = Class.forName("x123", false,
                                InitArg.class.getClassLoader());
        if (x123Initialized) {
            throw new Exception("forName should not run initializer");
        }
        Class d = Class.forName("x123", true,
                                InitArg.class.getClassLoader());
        if (!x123Initialized) {
            throw new Exception("forName not running initializer");
        }
    }
}
