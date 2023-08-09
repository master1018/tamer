public class SolarisPrincipal implements Principal, java.io.Serializable {
    private static final long serialVersionUID = -7840670002439379038L;
    private static final java.util.ResourceBundle rb =
          java.security.AccessController.doPrivileged
          (new java.security.PrivilegedAction<java.util.ResourceBundle>() {
              public java.util.ResourceBundle run() {
                  return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.AuthResources"));
              }
          });
    private String name;
    public SolarisPrincipal(String name) {
        if (name == null)
            throw new NullPointerException(rb.getString("provided.null.name"));
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return(rb.getString("SolarisPrincipal.") + name);
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof SolarisPrincipal))
            return false;
        SolarisPrincipal that = (SolarisPrincipal)o;
        if (this.getName().equals(that.getName()))
            return true;
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
}
