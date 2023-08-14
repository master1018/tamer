public class TestUtil {
    private boolean isUnlimited;
    private static TestUtil instance = null;
    static {
        instance = new TestUtil();
    }
    private TestUtil() {
        try {
            isUnlimited = isUnlimitedPolicy();
        } catch (Exception ex) {
            RuntimeException re = new RuntimeException
                ("Cannot locate the jurisdiction policy files");
            re.initCause(ex);
            throw re;
        }
    }
    private static boolean isUnlimitedPolicy() throws IOException {
        if (instance == null) {
            String jreDir = System.getProperty("java.home");
            String localPolicyPath = jreDir + File.separator + "lib" +
                File.separator + "security" + File.separator +
                "local_policy.jar";
            JarFile localPolicy = new JarFile(localPolicyPath);
            if (localPolicy.getEntry("exempt_local.policy") == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return instance.isUnlimited;
        }
    }
    public static void handleSE(SecurityException ex)
        throws SecurityException {
        if (instance == null) {
            instance = new TestUtil();
        }
        if ((instance.isUnlimited) ||
            !(ex.getMessage().startsWith("Unsupported keysize"))) {
            throw ex;
        }
    }
}
