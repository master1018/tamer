public class SolarisNumericGroupPrincipal implements
                                        Principal,
                                        java.io.Serializable {
    private static final long serialVersionUID = 2345199581042573224L;
    private static final java.util.ResourceBundle rb =
          java.security.AccessController.doPrivileged
          (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
              public java.util.ResourceBundle run() {
                  return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.AuthResources"));
              }
          });
    private String name;
    private boolean primaryGroup;
    public SolarisNumericGroupPrincipal(String name, boolean primaryGroup) {
        if (name == null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        this.name = name;
        this.primaryGroup = primaryGroup;
    }
    public SolarisNumericGroupPrincipal(long name, boolean primaryGroup) {
        this.name = (new Long(name)).toString();
        this.primaryGroup = primaryGroup;
    }
    public String getName() {
        return name;
    }
    public long longValue() {
        return ((new Long(name)).longValue());
    }
    public boolean isPrimaryGroup() {
        return primaryGroup;
    }
    public String toString() {
        return((primaryGroup ?
            rb.getString
            ("SolarisNumericGroupPrincipal.Primary.Group.") + name :
            rb.getString
            ("SolarisNumericGroupPrincipal.Supplementary.Group.") + name));
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof SolarisNumericGroupPrincipal))
            return false;
        SolarisNumericGroupPrincipal that = (SolarisNumericGroupPrincipal)o;
        if (this.getName().equals(that.getName()) &&
            this.isPrimaryGroup() == that.isPrimaryGroup())
            return true;
        return false;
    }
    public int hashCode() {
        return toString().hashCode();
    }
}
