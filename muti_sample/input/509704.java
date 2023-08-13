final class SocketPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = 2787186408602843674L;
    private Vector<Permission> permissions = new Vector<Permission>();
    public SocketPermissionCollection() {
        super();
    }
    @Override
    public void add(Permission permission) {
        if (isReadOnly()) {
            throw new IllegalStateException();
        }
        if (!(permission instanceof SocketPermission)) {
            throw new IllegalArgumentException(permission.toString());
        }
        permissions.addElement(permission);
    }
    @Override
    public Enumeration<Permission> elements() {
        return permissions.elements();
    }
    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof SocketPermission)) {
            return false;
        }
        SocketPermission sp, argPerm = (SocketPermission) permission;
        int pmask = argPerm.actionsMask;
        int allMask = 0;
        int i = 0, count = permissions.size();
        while ((i < count) && ((allMask & pmask) != pmask)) {
            sp = (SocketPermission) permissions.elementAt(i);
            if (sp.checkHost(argPerm)) {
                if ((sp.actionsMask & SocketPermission.SP_RESOLVE) == SocketPermission.SP_RESOLVE) {
                    allMask |= SocketPermission.SP_RESOLVE;
                }
                if ((argPerm.portMin >= sp.portMin)
                        && (argPerm.portMax <= sp.portMax)) {
                    if ((sp.actionsMask & SocketPermission.SP_CONNECT) == SocketPermission.SP_CONNECT) {
                        allMask |= SocketPermission.SP_CONNECT;
                    }
                    if ((sp.actionsMask & SocketPermission.SP_ACCEPT) == SocketPermission.SP_ACCEPT) {
                        allMask |= SocketPermission.SP_ACCEPT;
                    }
                    if ((sp.actionsMask & SocketPermission.SP_LISTEN) == SocketPermission.SP_LISTEN) {
                        allMask |= SocketPermission.SP_LISTEN;
                    }
                }
            }
            ++i;
        }
        return (allMask & pmask) == pmask;
    }
}
