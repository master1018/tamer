public class AliasExpansion {
    public static void main(String[] args) throws Exception {
        PrivateCredentialPermission pcp = new PrivateCredentialPermission
                ("java.lang.String javax.security.auth.x500.X500Principal " +
                "\"CN=x509\"", "read");
        SecurityManager sm = System.getSecurityManager();
        sm.checkPermission(pcp);
        java.security.SecurityPermission sp =
                new java.security.SecurityPermission
                        ("abcde javax.security.auth.x500.X500Principal " +
                        "\"CN=x509\" fghij " +
                        "javax.security.auth.x500.X500Principal " +
                        "\"CN=x509\"");
        sm.checkPermission(sp);
        sp = new java.security.SecurityPermission
                ("javax.security.auth.x500.X500Principal \"CN=x509\"");
        sm.checkPermission(sp);
        sp = new java.security.SecurityPermission
                ("javax.security.auth.x500.X500Principal \"CN=x509\" abc");
        sm.checkPermission(sp);
        System.out.println("test passed");
    }
}
