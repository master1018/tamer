public final class ReflectPermission extends BasicPermission {
    private static final long serialVersionUID = 7412737110241507485L;
    public ReflectPermission(String permissionName) {
        super(permissionName);
    }
    public ReflectPermission(String name, String actions) {
        super(name, actions);
    }
}
