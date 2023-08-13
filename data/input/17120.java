public final class UserPrincipal implements Principal, java.io.Serializable {
    private static final long serialVersionUID = 892106070870210969L;
    private final String name;
    public UserPrincipal(String name) {
        if (name == null) {
            throw new NullPointerException("null name is illegal");
        }
        this.name = name;
    }
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof UserPrincipal) {
            return name.equals(((UserPrincipal)object).getName());
        }
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return name;
    }
}
