public final class SubjectDelegationPermission extends BasicPermission {
    private static final long serialVersionUID = 1481618113008682343L;
    public SubjectDelegationPermission(String name) {
        super(name);
    }
    public SubjectDelegationPermission(String name, String actions) {
        super(name, actions);
        if (actions != null)
            throw new IllegalArgumentException("Non-null actions");
    }
}
