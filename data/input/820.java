public class EmailAddress {
    public static void main(String[] args) {
        Principal[] principals = new Principal[1];
        principals[0] = new javax.security.auth.x500.X500Principal
                                        ("emailaddress=duke@sun");
        java.net.URL url = null;
        try {
            url = new java.net.URL("http:
        } catch (java.net.MalformedURLException mue) {
            System.out.println("test 1 failed");
            throw new SecurityException(mue.getMessage());
        }
        CodeSource cs =
            new CodeSource(url, (java.security.cert.Certificate[]) null);
        ProtectionDomain pd = new ProtectionDomain
                (cs,
                null,
                null,
                principals);
        PermissionCollection perms = Policy.getPolicy().getPermissions(pd);
        if (perms.implies(new SecurityPermission("EMAILADDRESS"))) {
            System.out.println("test succeeded");
        } else {
            System.out.println("test 2 failed");
            throw new SecurityException("test failed");
        }
    }
}
