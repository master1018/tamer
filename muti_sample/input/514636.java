public final class UnresolvedPrincipal implements Principal {
    public static final String WILDCARD = DefaultPolicyScanner.PrincipalEntry.WILDCARD;
    private final String klass;
    private final String name;
    public UnresolvedPrincipal(String klass, String name) {
        if (klass == null || klass.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("security.91")); 
        }
        this.klass = klass;
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getClassName() {
        return klass;
    }
    public boolean equals(Object that) {
        if (that instanceof UnresolvedPrincipal) {
            UnresolvedPrincipal up = (UnresolvedPrincipal) that;
            return klass.equals(up.klass)
                    && (name == null ? up.name == null : name.equals(up.name));
        }
        if (that instanceof Principal) {
            return implies((Principal) that);
        }
        return false;
    }
    public boolean implies(Principal another) {
        return (another != null)
                && (WILDCARD.equals(klass) 
                    || klass.equals(another.getClass().getName())
                && (WILDCARD.equals(name) 
                    || (name == null ? another.getName() == null 
                        : name.equals(another.getName()))));
    }
    public int hashCode() {
        int hash = 0;
        if (name != null) {
            hash ^= name.hashCode();
        }
        if (klass != null) {
            hash ^= klass.hashCode();
        }
        return hash;
    }
    public String toString() {
        return "Principal " + klass + " \"" + name + "\""; 
    }
}
