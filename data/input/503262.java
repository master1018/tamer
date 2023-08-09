public final class SQLPermission extends BasicPermission implements Guard,
        Serializable {
    private static final long serialVersionUID = -1439323187199563495L;
    public SQLPermission(String name) {
        super(name);
    }
    public SQLPermission(String name, String actions) {
        super(name, null);
    }
}
