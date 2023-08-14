public class SetLogPermission {
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4533390\n");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        CodeSource codesource = new CodeSource(null, (Certificate[]) null);
        Permissions perms = null;
        ProtectionDomain pd = new ProtectionDomain(codesource, perms);
        AccessControlContext acc =
            new AccessControlContext(new ProtectionDomain[] { pd });
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
                try {
                    System.err.println(
                        "Attempt to set log without permission");
                    RemoteServer.setLog(new ByteArrayOutputStream());
                    throw new RuntimeException(
                        "TEST FAILED: set log without permission");
                } catch (SecurityException e) {
                    System.err.println(
                        "TEST PASSED: unable to set log without permission");
                }
                return null;
            }}, acc);
        try {
            System.err.println("Attempt to set log with permission");
            RemoteServer.setLog(new ByteArrayOutputStream());
            System.err.println(
                "TEST PASSED: sufficient permission to set log");
        } catch (SecurityException e) {
            System.err.println("TEST FAILED: unable to set log");
            throw e;
        }
    }
}
