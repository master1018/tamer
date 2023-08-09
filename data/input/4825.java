public class PermissionEqualsHashCode {
    public static void main(String[] args) throws Exception {
        PermissionImpl p1 = new PermissionImpl("permissionPermission");
        PermissionImpl p2 = new PermissionImpl("permissionPermission");
        if ( (p1.equals(p2)) == (p1.hashCode()==p2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception("Failed equals()/hashCode() contract");
    }
}
