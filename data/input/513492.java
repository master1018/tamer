public class AclEntryImpl implements AclEntry {
    private Principal user;
    private Vector permissionSet;
    private boolean negative;
    public AclEntryImpl(Principal principal) {
        user = null;
        permissionSet = new Vector(10, 10);
        negative = false;
        user = principal;
    }
    public AclEntryImpl() {
        user = null;
        permissionSet = new Vector(10, 10);
        negative = false;
    }
    public boolean setPrincipal(Principal principal) {
        if(user != null) {
            return false;
        } else {
            user = principal;
            return true;
        }
    }
    public void setNegativePermissions() {
        negative = true;
    }
    public boolean isNegative() {
        return negative;
    }
    public boolean addPermission(Permission permission) {
        if(permissionSet.contains(permission)) {
            return false;
        } else {
            permissionSet.addElement(permission);
            return true;
        }
    }
    public boolean removePermission(Permission permission) {
        return permissionSet.removeElement(permission);
    }
    public boolean checkPermission(Permission permission) {
        return permissionSet.contains(permission);
    }
    public Enumeration permissions() {
        return permissionSet.elements();
    }
    public String toString() {
        StringBuffer stringbuffer = new StringBuffer();
        if(negative)
            stringbuffer.append("-");
        else
            stringbuffer.append("+");
        if(user instanceof Group)
            stringbuffer.append("Group.");
        else
            stringbuffer.append("User.");
        stringbuffer.append((new StringBuilder()).append(user).append("=").toString());
        Enumeration enumeration = permissions();
        do {
            if(!enumeration.hasMoreElements())
                break;
            Permission permission = (Permission)enumeration.nextElement();
            stringbuffer.append(permission);
            if(enumeration.hasMoreElements())
                stringbuffer.append(",");
        } while(true);
        return new String(stringbuffer);
    }
    public synchronized Object clone() {
        AclEntryImpl aclentryimpl = new AclEntryImpl(user);
        aclentryimpl.permissionSet = (Vector)permissionSet.clone();
        aclentryimpl.negative = negative;
        return aclentryimpl;
    }
    public Principal getPrincipal() {
        return user;
    }
}