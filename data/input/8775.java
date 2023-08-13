public class DebugPermission1 extends java.security.Permission {
    public DebugPermission1(String name) {
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
