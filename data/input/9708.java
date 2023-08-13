public class CheckWhatYouGet {
    public static void main(String[] args) throws Exception {
        CodeSource codesource =
            new CodeSource(null, (java.security.cert.Certificate[]) null);
        Permissions perms = null;
        ProtectionDomain pd = new ProtectionDomain(codesource, perms);
        if (pd.getPermissions() != null) {
            System.err.println("TEST FAILED: incorrect Permissions returned");
            throw new RuntimeException("test failed: incorrect Permissions returned");
        }
        perms = new Permissions();
        pd = new ProtectionDomain(codesource, perms);
        PermissionCollection pc = pd.getPermissions();
        Enumeration e = pc.elements();
        if (e.hasMoreElements()) {
            System.err.println("TEST FAILED: incorrect Permissions returned");
            throw new RuntimeException("test failed: incorrect Permissions returned");
        }
    }
}
