public class SelfWildcard {
    private static final String SELF_ONE =
        "javax.security.auth.x500.X500Principal \"CN=foo\"";
    private static final String SELF_TWOTHREE =
        "javax.security.auth.x500.X500Principal \"CN=foo\", " +
        "javax.security.auth.x500.X500Principal \"CN=bar\"";
    private static final String SELF_FOURFIVE =
        "javax.security.auth.x500.X500Principal \"CN=foo\", " +
        "javax.security.auth.x500.X500Principal \"CN=bar\", " +
        "com.sun.security.auth.UnixPrincipal \"foobar\"";
    public static void main(String[] args) throws Exception {
        if (System.getProperty("test.src") == null) {
            System.setProperty("test.src", ".");
        }
        System.setProperty("java.security.policy",
                "file:${test.src}/SelfWildcard.policy");
        Principal[] ps = {
                new X500Principal("CN=foo"),
                new X500Principal("CN=bar"),
                new com.sun.security.auth.UnixPrincipal("foobar") };
        ProtectionDomain pd = new ProtectionDomain
                (new CodeSource(null, (java.security.cert.Certificate[]) null),
                    null, null, ps);
        PermissionCollection perms = Policy.getPolicy().getPermissions(pd);
        System.out.println("perms = " + perms);
        System.out.println();
        Enumeration e = perms.elements();
        while (e.hasMoreElements()) {
            Permission p = (Permission)e.nextElement();
            if (p instanceof UnresolvedPermission &&
                p.toString().indexOf(SELF_ONE) < 0 &&
                p.toString().indexOf(SELF_TWOTHREE) < 0 &&
                p.toString().indexOf(SELF_FOURFIVE) < 0) {
                throw new SecurityException("Test Failed");
            }
        }
        System.out.println("Test Succeeded");
    }
}
