public final class SerializablePermission extends BasicPermission {
    private static final long serialVersionUID = 8537212141160296410L;
    @SuppressWarnings("unused")
    private String actions;
    public SerializablePermission(String permissionName) {
        super(permissionName);
    }
    public SerializablePermission(String name, String actions) {
        super(name, actions);
    }
}
