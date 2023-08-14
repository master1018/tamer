public class NTSidPrimaryGroupPrincipal extends NTSid {
    private static final long serialVersionUID = 8011978367305190527L;
    public NTSidPrimaryGroupPrincipal(String name) {
        super(name);
    }
    public String toString() {
        java.text.MessageFormat form = new java.text.MessageFormat
                (sun.security.util.ResourcesMgr.getString
                        ("NTSidPrimaryGroupPrincipal.name",
                        "sun.security.util.AuthResources"));
        Object[] source = {getName()};
        return form.format(source);
    }
    public boolean equals(Object o) {
            if (o == null)
                return false;
        if (this == o)
            return true;
        if (!(o instanceof NTSidPrimaryGroupPrincipal))
            return false;
        return super.equals(o);
    }
}
