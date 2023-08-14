public final class LdapPrincipal implements Principal, java.io.Serializable {
    private static final long serialVersionUID = 6820120005580754861L;
    private final String nameString;
    private final LdapName name;
    public LdapPrincipal(String name) throws InvalidNameException {
        if (name == null) {
            throw new NullPointerException("null name is illegal");
        }
        this.name = getLdapName(name);
        nameString = name;
    }
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof LdapPrincipal) {
            try {
                return
                    name.equals(getLdapName(((LdapPrincipal)object).getName()));
            } catch (InvalidNameException e) {
                return false;
            }
        }
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
    public String getName() {
        return nameString;
    }
    public String toString() {
        return name.toString();
    }
    private LdapName getLdapName(String name) throws InvalidNameException {
        return new LdapName(name);
    }
}
