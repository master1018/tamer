public class Support_IOTestSecurityManager extends SecurityManager {
    private ArrayList<Permission> permissions;
    public Support_IOTestSecurityManager() {
        permissions = new ArrayList<Permission>(2);
        permissions.add(new SerializablePermission("enableSubclassImplementation"));
        permissions.add(new SerializablePermission("enableSubstitution"));
    }
    public void checkPermission(Permission p) {
        for (Iterator<Permission> i = permissions.iterator(); i.hasNext(); ) {
            if (i.next().equals(p)) {
                throw new SecurityException();
            }
        }
    }        
}
