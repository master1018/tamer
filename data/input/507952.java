public final class SecurityPermission extends BasicPermission {
    private static final long serialVersionUID = 5236109936224050470L;
    public SecurityPermission(String name) {
        super(name);
    }
    public SecurityPermission(String name, String action) {
        super(name, action);
    }
}
