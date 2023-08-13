public final class AccessControlContext {
    ProtectionDomain[] context;
    DomainCombiner combiner;
    private AccessControlContext inherited;
    public AccessControlContext(AccessControlContext acc,
            DomainCombiner combiner) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission(
                    "createAccessControlContext"));
        }
        this.context = acc.context;
        this.combiner = combiner;
    }
    public AccessControlContext(ProtectionDomain[] context) {
        if (context == null) {
            throw new NullPointerException("context can not be null");
        }
        if (context.length != 0) {
            ArrayList<ProtectionDomain> a = new ArrayList<ProtectionDomain>();
            for (int i = 0; i < context.length; i++) {
                if (context[i] != null && !a.contains(context[i])) {
                    a.add(context[i]);
                }
            }
            if (a.size() != 0) {
                this.context = new ProtectionDomain[a.size()];
                a.toArray(this.context);
            }
        }
        if (this.context == null) {
            this.context = new ProtectionDomain[0];
        }
    }
    AccessControlContext(ProtectionDomain[] stack,
            AccessControlContext inherited) {
        this(stack); 
        this.inherited = inherited;
    }
    AccessControlContext(ProtectionDomain[] stack,
            DomainCombiner combiner) {
        this(stack); 
        this.combiner = combiner;
    }
    public void checkPermission(Permission perm) throws AccessControlException {
        if (perm == null) {
            throw new NullPointerException("Permission cannot be null");
        }
        for (int i = 0; i < context.length; i++) {
            if (!context[i].implies(perm)) {
                throw new AccessControlException("Permission check failed "
                        + perm, perm);
            }
        }
        if (inherited != null) {
            inherited.checkPermission(perm);
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AccessControlContext) {
            AccessControlContext that = (AccessControlContext) obj;
            if (!(PolicyUtils.matchSubset(context, that.context) && PolicyUtils
                    .matchSubset(that.context, context))) {
                return false;
            }
            if(combiner != null) {
                return combiner.equals(that.combiner);
            }
            return that.combiner == null;
        }
        return false;
    }
    public DomainCombiner getDomainCombiner() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getDomainCombiner"));
        }
        return combiner;
    }
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < context.length; i++) {
            hash ^= context[i].hashCode();
        }
        return hash;
    }
}
