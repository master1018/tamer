public final class MyPermission extends Permission {
    private static final long serialVersionUID = 4208595188308189251L;
    public MyPermission(String name) {
        super(name);
    }
    public boolean equals(Object obj) {
        if (obj instanceof MyPermission) {
            String name = ((MyPermission) obj).getName();
            if (name == null) {
                return getName() == null;
            }
            return name.equals(getName());
        }
        return false;
    }
    public String getActions() {
        return null;
    }
    public int hashCode() {
        return 0;
    }
    public boolean implies(Permission permission) {
        return false;
    }
}
