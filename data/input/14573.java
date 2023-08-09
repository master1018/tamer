public class JMXPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = -4184480100214577411L;
    private String name;
    public JMXPrincipal(String name) {
        if (name == null)
            throw new NullPointerException("illegal null input");
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return("JMXPrincipal:  " + name);
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof JMXPrincipal))
            return false;
        JMXPrincipal that = (JMXPrincipal)o;
        return (this.getName().equals(that.getName()));
    }
    public int hashCode() {
        return name.hashCode();
    }
}
