public class Homogeneity {
    public static void main(String[] args) {
        java.lang.RuntimePermission rp = new java.lang.RuntimePermission
                                        ("*");
        java.lang.RuntimePermission rp2 = new java.lang.RuntimePermission
                                        ("exitVM");
        java.net.NetPermission np = new java.net.NetPermission
                                        ("setDefaultAuthenticator");
        java.security.PermissionCollection perms = rp.newPermissionCollection();
        try {
            perms.add(rp);
            perms.add(rp2);
        } catch (IllegalArgumentException iae) {
            throw new SecurityException("GOOD ADD TEST FAILED");
        }
        try {
            perms.add(np);
            throw new SecurityException("BAD ADD TEST FAILED");
        } catch (IllegalArgumentException iae) {
        }
        if (perms.implies(np)) {
            throw new SecurityException("IMPLIES TEST FAILED");
        }
    }
}
