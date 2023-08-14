public class CompositeName implements Name {
    private transient NameImpl impl;
    protected CompositeName(Enumeration<String> comps) {
        impl = new NameImpl(null, comps); 
    }
    public CompositeName(String n) throws InvalidNameException {
        impl = new NameImpl(null, n);  
    }
    public CompositeName() {
        impl = new NameImpl(null);  
    }
    public String toString() {
        return impl.toString();
    }
    public boolean equals(Object obj) {
        return (obj != null &&
                obj instanceof CompositeName &&
                impl.equals(((CompositeName)obj).impl));
    }
    public int hashCode() {
        return impl.hashCode();
    }
    public int compareTo(Object obj) {
        if (!(obj instanceof CompositeName)) {
            throw new ClassCastException("Not a CompositeName");
        }
        return impl.compareTo(((CompositeName)obj).impl);
    }
    public Object clone() {
        return (new CompositeName(getAll()));
    }
    public int size() {
        return (impl.size());
    }
    public boolean isEmpty() {
        return (impl.isEmpty());
    }
    public Enumeration<String> getAll() {
        return (impl.getAll());
    }
    public String get(int posn) {
        return (impl.get(posn));
    }
    public Name getPrefix(int posn) {
        Enumeration comps = impl.getPrefix(posn);
        return (new CompositeName(comps));
    }
    public Name getSuffix(int posn) {
        Enumeration comps = impl.getSuffix(posn);
        return (new CompositeName(comps));
    }
    public boolean startsWith(Name n) {
        if (n instanceof CompositeName) {
            return (impl.startsWith(n.size(), n.getAll()));
        } else {
            return false;
        }
    }
    public boolean endsWith(Name n) {
        if (n instanceof CompositeName) {
            return (impl.endsWith(n.size(), n.getAll()));
        } else {
            return false;
        }
    }
    public Name addAll(Name suffix)
        throws InvalidNameException
    {
        if (suffix instanceof CompositeName) {
            impl.addAll(suffix.getAll());
            return this;
        } else {
            throw new InvalidNameException("Not a composite name: " +
                suffix.toString());
        }
    }
    public Name addAll(int posn, Name n)
        throws InvalidNameException
    {
        if (n instanceof CompositeName) {
            impl.addAll(posn, n.getAll());
            return this;
        } else {
            throw new InvalidNameException("Not a composite name: " +
                n.toString());
        }
    }
    public Name add(String comp) throws InvalidNameException {
        impl.add(comp);
        return this;
    }
    public Name add(int posn, String comp)
        throws InvalidNameException
    {
        impl.add(posn, comp);
        return this;
    }
    public Object remove(int posn) throws InvalidNameException{
        return impl.remove(posn);
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeInt(size());
        Enumeration comps = getAll();
        while (comps.hasMoreElements()) {
            s.writeObject(comps.nextElement());
        }
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        impl = new NameImpl(null);  
        int n = s.readInt();    
        try {
            while (--n >= 0) {
                add((String)s.readObject());
            }
        } catch (InvalidNameException e) {
            throw (new java.io.StreamCorruptedException("Invalid name"));
        }
    }
    private static final long serialVersionUID = 1667768148915813118L;
}
