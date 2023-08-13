public class RecursionDebug {
    public static class RecursionSM extends SecurityManager {
        public void checkPermission(Permission p) {
            super.checkPermission(p);
        }
    }
    public static void main(String[] args) throws Exception {
        try {
            System.getProperty("foo.bar");
        } catch (Exception e) {
        }
        Permissions staticPerms = new Permissions();
        staticPerms.add(new java.util.PropertyPermission("static.foo", "read"));
        ProtectionDomain pd = new ProtectionDomain
                        (new CodeSource
                                (new URL("http:
                                (java.security.cert.Certificate[])null),
                        staticPerms,
                        null,
                        null);
        if (pd.toString().indexOf("merged.foo") < 0) {
            throw new Exception("Test with bootclass SecurityManager failed");
        }
        ProtectionDomain pd2 = new ProtectionDomain
                        (new CodeSource
                                (new URL("http:
                                (java.security.cert.Certificate[])null),
                        staticPerms,
                        null,
                        null);
        System.setSecurityManager(new RecursionDebug.RecursionSM());
        if (pd2.toString().indexOf("merged.foo") >= 0) {
            throw new Exception
                ("Test with custom non-bootclass SecurityManager failed");
        }
        System.setSecurityManager(null);
    }
}
