public abstract class PolicySpi {
    protected abstract boolean engineImplies
        (ProtectionDomain domain, Permission permission);
    protected void engineRefresh() { }
    protected PermissionCollection engineGetPermissions
                                        (CodeSource codesource) {
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }
    protected PermissionCollection engineGetPermissions
                                        (ProtectionDomain domain) {
        return Policy.UNSUPPORTED_EMPTY_COLLECTION;
    }
}
