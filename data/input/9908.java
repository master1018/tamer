public class MyPrincipal implements Principal {
    private String name;
    public MyPrincipal(String name) {
        if (name == null)
            throw new NullPointerException("illegal null input");
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return("MyPrincipal:  " + name);
    }
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof MyPrincipal))
            return false;
        MyPrincipal that = (MyPrincipal)o;
        if (this.getName().equals(that.getName()))
            return true;
        return false;
    }
    public int hashCode() {
        return name.hashCode();
    }
}
