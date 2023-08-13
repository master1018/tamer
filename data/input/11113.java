public class EmptyName {
    public static void main(String[] args) throws Exception {
        try {
            PrivateCredentialPermission perm =
                        new PrivateCredentialPermission("", "read");
            throw new SecurityException("test 1 failed");
        } catch (IllegalArgumentException iae) {
        }
        try {
            PrivateCredentialPermission perm =
                        new PrivateCredentialPermission(null, "read");
            throw new SecurityException("test 2 failed");
        } catch (IllegalArgumentException iae) {
        }
        System.out.println("test passed");
    }
}
