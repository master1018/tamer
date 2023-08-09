public class NTUserPrincipal implements Principal, java.io.Serializable {
    private static final long serialVersionUID = -8737649811939033735L;
    private String name;
    public NTUserPrincipal(String name) {
        if (name == null) {
            java.text.MessageFormat form = new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("invalid.null.input.value",
                        "sun.security.util.AuthResources"));
            Object[] source = {"name"};
            throw new NullPointerException(form.format(source));
        }
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        java.text.MessageFormat form = new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTUserPrincipal.name",
                        "sun.security.util.AuthResources"));
        Object[] source = {name};
        return form.format(source);
    }
    public boolean equals(Object o) {
            if (o == null)
                return false;
        if (this == o)
            return true;
        if (!(o instanceof NTUserPrincipal))
            return false;
        NTUserPrincipal that = (NTUserPrincipal)o;
            if (name.equals(that.getName()))
                return true;
            return false;
    }
    public int hashCode() {
            return this.getName().hashCode();
    }
}
