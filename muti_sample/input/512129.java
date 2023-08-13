final class FilePermissionCollection extends PermissionCollection implements
        Serializable {
    private static final long serialVersionUID = 2202956749081564585L;
    Vector<Permission> permissions = new Vector<Permission>();
    public FilePermissionCollection() {
        super();
    }
    @Override
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new IllegalStateException();
        }
        if (permission instanceof FilePermission) {
            permissions.addElement(permission);
        } else {
            throw new IllegalArgumentException(permission.toString());
        }
    }
    @Override
    public Enumeration<Permission> elements() {
        return permissions.elements();
    }
    @Override
    public boolean implies(Permission permission) {
        if (permission instanceof FilePermission) {
            FilePermission fp = (FilePermission) permission;
            int matchedMask = 0;
            int i = 0;
            while (i < permissions.size()
                    && ((matchedMask & fp.mask) != fp.mask)) {
                matchedMask |= ((FilePermission) permissions.elementAt(i))
                        .impliesMask(permission);
                i++;
            }
            return ((matchedMask & fp.mask) == fp.mask);
        }
        return false;
    }
}
