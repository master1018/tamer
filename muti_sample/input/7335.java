public class NTSidDomainPrincipal extends NTSid {
    private static final long serialVersionUID = 5247810785821650912L;
    public NTSidDomainPrincipal(String name) {
        super(name);
    }
    public String toString() {
        java.text.MessageFormat form = new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTSidDomainPrincipal.name",
                        "sun.security.util.AuthResources"));
        Object[] source = {getName()};
        return form.format(source);
    }
    public boolean equals(Object o) {
            if (o == null)
                return false;
        if (this == o)
            return true;
        if (!(o instanceof NTSidDomainPrincipal))
            return false;
        return super.equals(o);
    }
}
