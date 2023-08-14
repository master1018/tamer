public class MixedBasicPermissions {
    public static void main(String[] args) {
        try {
            new java.net.NetPermission("1.1.1.1", "connect").newPermissionCollection().add(new java.util.PropertyPermission("j", "read"));
        } catch (Exception e) {
            return;
        }
        throw new RuntimeException("Should not be here");
    }
}
