public class DebugPermissionBad extends java.security.Permission {
    public DebugPermissionBad(String name, int x) {
        super(name);
    }
    public boolean implies(java.security.Permission perm) {
        return true;
    }
    public boolean equals(Object obj) {
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public String getActions() {
        return null;
    }
};
