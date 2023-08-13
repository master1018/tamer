public class NTDomainPrincipal implements Principal, java.io.Serializable {
    private static final long serialVersionUID = -4408637351440771220L;
    private String name;
    public NTDomainPrincipal(String name) {
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
                        ("NTDomainPrincipal.name",
                        "sun.security.util.AuthResources"));
        Object[] source = {name};
        return form.format(source);
    }
    public boolean equals(Object o) {
        if (o == null)
                return false;
        if (this == o)
            return true;
        if (!(o instanceof NTDomainPrincipal))
            return false;
        NTDomainPrincipal that = (NTDomainPrincipal)o;
            if (name.equals(that.getName()))
                return true;
            return false;
    }
    public int hashCode() {
        return this.getName().hashCode();
    }
}
