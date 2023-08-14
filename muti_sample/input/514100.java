public class ProtectionDomain {
    private CodeSource codeSource;
    private PermissionCollection permissions;
    private ClassLoader classLoader;
    private Principal[] principals;
    private boolean dynamicPerms;
    public ProtectionDomain(CodeSource cs, PermissionCollection permissions) {
        this.codeSource = cs;
        if (permissions != null) {
            permissions.setReadOnly();
        }
        this.permissions = permissions;
    }
    public ProtectionDomain(CodeSource cs, PermissionCollection permissions,
            ClassLoader cl, Principal[] principals) {
        this.codeSource = cs;
        if (permissions != null) {
            permissions.setReadOnly();
        }
        this.permissions = permissions;
        this.classLoader = cl;
        if (principals != null) {
            this.principals = new Principal[principals.length];
            System.arraycopy(principals, 0, this.principals, 0,
                    this.principals.length);
        }
        dynamicPerms = true;
    }
    public final ClassLoader getClassLoader() {
        return classLoader;
    }
    public final CodeSource getCodeSource() {
        return codeSource;
    }
    public final PermissionCollection getPermissions() {
        return permissions;
    }
    public final Principal[] getPrincipals() {
        if( principals == null ) {
            return new Principal[0];
        }
        Principal[] tmp = new Principal[principals.length];
        System.arraycopy(principals, 0, tmp, 0, tmp.length);
        return tmp;
    }
    public boolean implies(Permission permission) {
        if (dynamicPerms
                && Policy.getAccessiblePolicy().implies(this, permission)) {
            return true;
        }
        return permissions == null ? false : permissions.implies(permission);
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(200);
        buf.append("ProtectionDomain\n"); 
        buf.append("CodeSource=").append( 
                codeSource == null ? "<null>" : codeSource.toString()).append( 
                "\n"); 
        buf.append("ClassLoader=").append( 
                classLoader == null ? "<null>" : classLoader.toString()) 
                .append("\n"); 
        if (principals == null || principals.length == 0) {
            buf.append("<no principals>\n"); 
        } else {
            buf.append("Principals: <\n"); 
            for (int i = 0; i < principals.length; i++) {
                buf.append("\t").append( 
                        principals[i] == null ? "<null>" : principals[i] 
                                .toString()).append("\n"); 
            }
            buf.append(">"); 
        }
        buf.append("Permissions:\n"); 
        if (permissions == null) {
            buf.append("\t\t<no static permissions>\n"); 
        } else {
            buf.append("\t\tstatic: ").append(permissions.toString()).append( 
                    "\n"); 
        }
        if (dynamicPerms) {
            if (Policy.isSet()) {
                PermissionCollection perms;
                perms = Policy.getAccessiblePolicy().getPermissions(this);
                if (perms == null) {
                    buf.append("\t\t<no dynamic permissions>\n"); 
                } else {
                    buf.append("\t\tdynamic: ").append(perms.toString()) 
                            .append("\n"); 
                }
            } else {
                buf.append("\t\t<no dynamic permissions>\n"); 
            }
        }
        return buf.toString();
    }
}
