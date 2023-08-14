public class TestParams {
    public static String testSrc = null;
    public static String testClasses = null;
    public static String defaultPolicy = null;
    public static String defaultRmidPolicy = null;
    public static String defaultGroupPolicy = null;
    public static String defaultSecurityManager =
        "java.rmi.RMISecurityManager";
    static {
        try {
            testSrc = TestLibrary.
                getProperty("test.src", ".");
            testClasses = TestLibrary.
                getProperty("test.classes", ".");
            defaultPolicy = TestLibrary.
                getProperty("java.security.policy",
                            defaultPolicy);
            if (defaultPolicy == null) {
                defaultPolicy = testSrc + File.separatorChar +
                    "security.policy";
            }
            defaultSecurityManager = TestLibrary.
                getProperty("java.security.manager",
                            defaultSecurityManager);
            defaultRmidPolicy =
                testSrc + File.separatorChar + "rmid.security.policy";
            defaultGroupPolicy = testSrc +
                File.separatorChar + "group.security.policy";
        } catch (SecurityException se) {
            TestLibrary.bomb("Security exception received" +
                             " during test initialization:",
                             se);
        }
    }
}
