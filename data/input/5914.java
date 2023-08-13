public class SolarisNumericUserPrincipal implements
                                        Principal,
                                        java.io.Serializable {
    private static final long serialVersionUID = -3178578484679887104L;
    private static final java.util.ResourceBundle rb =
          java.security.AccessController.doPrivileged
          (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
              public java.util.ResourceBundle run() {
                  return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.AuthResources"));
              }
           });
    private String name;
    public SolarisNumericUserPrincipal(String name) {
        if (name == null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        this.name = name;
    }
    public SolarisNumericUserPrincipal(long name) {
        this.name = (new Long(name)).toString();
    }
    public String getName() {
        return name;
    }
    public long longValue() {
        return ((new Long(name)).longValue());
    }
    public String toString() {
        return(rb.getString("SolarisNumericUserPrincipal.") + name);
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof SolarisNumericUserPrincipal))
            return false;
        SolarisNumericUserPrincipal that = (SolarisNumericUserPrincipal)o;
        if (this.getName().equals(that.getName()))
            return true;
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
}
