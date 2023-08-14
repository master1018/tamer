public class DelegateBeforePermissionCheck {
    private final static String tabooCodebase = "http:
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4191926\n");
        TestLibrary.suggestSecurityManager(null);
        try {
            String localClassName = Foo.class.getName();
            System.err.println("Attempting to load local class \"" +
                localClassName + "\" from codebase " + tabooCodebase);
            Class cl = RMIClassLoader.loadClass(
                tabooCodebase, localClassName);
            System.err.println("TEST PASSED: loaded " + cl + " locally");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
}
