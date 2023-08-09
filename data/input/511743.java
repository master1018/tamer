final class PermissionsHash extends PermissionCollection {
    private static final long serialVersionUID = -8491988220802933440L;
    private final Hashtable perms = new Hashtable();
    public void add(Permission permission) {
        perms.put(permission, permission);
    }
    public Enumeration elements() {
        return perms.elements();
    }
    public boolean implies(Permission permission) {
        for (Enumeration elements = elements(); elements.hasMoreElements();) {
            if (((Permission)elements.nextElement()).implies(permission)) {
                return true;
            }
        }
        return false;
    }
}
