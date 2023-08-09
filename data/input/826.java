public class NullOrEmptyName {
    public static void main(String[]args) throws Exception {
        NullOrEmptyName noe = new NullOrEmptyName();
        noe.run();
        SecurityManager sm = new SecurityManager();
        System.setSecurityManager(sm);
        noe.run();
        try {
            sm.checkPropertyAccess(null);
            throw new Exception("Expected NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
        try {
            sm.checkPropertyAccess("");
            throw new Exception("Expected IllegalArgumentException not " +
                                "thrown");
        } catch (IllegalArgumentException iae) {
        }
    }
    void run() throws Exception {
        try {
            System.getProperty(null);
            throw new Exception("Expected NullPointerException not " +
                                "thrown");
        } catch (NullPointerException npe) {
        }
        try {
            System.getProperty(null, "value");
            throw new Exception("Expected NullPointerException not " +
                                "thrown");
        } catch (NullPointerException npe) {
        }
        try {
            System.getProperty("");
            throw new Exception("Expected IllegalArgumentException not " +
                                "thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            System.getProperty("", "value");
            throw new Exception("Expected IllegalArgumentException not " +
                                "thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            System.setProperty(null, "value");
            throw new Exception("Expected NullPointerException not " +
                                "thrown");
        } catch (NullPointerException npe) {
        }
        try {
            System.setProperty("", "value");
            throw new Exception("Expected IllegalArgumentException not " +
                                "thrown");
        } catch (IllegalArgumentException iae) {
        }
    }
}
