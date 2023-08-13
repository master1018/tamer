public class PermissionImpl implements Permission {
    private String permission;
    public PermissionImpl(String s) {
        permission = s;
    }
    public boolean equals(Object obj) {
        if(obj instanceof Permission) {
            Permission permission1 = (Permission)obj;
            return permission.equals(permission1.toString());
        } else {
            return false;
        }
    }
    public String toString() {
        return permission;
    }
}