public class Recursion {
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
        System.setSecurityManager(null);
        if (pd.toString().indexOf("merged.foo") < 0) {
            throw new Exception("Test without SecurityManager failed");
        }
        ProtectionDomain pd2 = new ProtectionDomain
                        (new CodeSource
                                (new URL("http:
                                (java.security.cert.Certificate[])null),
                        staticPerms,
                        null,
                        null);
        System.setSecurityManager(new SecurityManager());
        if (pd2.toString().indexOf("merged.foo") < 0) {
            throw new Exception("Test with bootclass SecurityManager failed");
        }
    }
}
