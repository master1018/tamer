public final class AllPermission extends Permission {
    private static final long serialVersionUID = -2916474571451318075L;
    private static final String ALL_PERMISSIONS = "<all permissions>"; 
    private static final String ALL_ACTIONS = "<all actions>"; 
    public AllPermission(String name, String actions) {
        super(ALL_PERMISSIONS);
    }
    public AllPermission() {
        super(ALL_PERMISSIONS);
    }
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AllPermission);
    }
    @Override
    public int hashCode() {
        return 1;
    }
    @Override
    public String getActions() {
        return ALL_ACTIONS;
    }
    @Override
    public boolean implies(Permission permission) {
        return true;
    }
    @Override
    public PermissionCollection newPermissionCollection() {
        return new AllPermissionCollection();
    }
}
