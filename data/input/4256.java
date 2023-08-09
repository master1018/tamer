public class CompoundName implements Name {
    protected transient NameImpl impl;
    protected transient Properties mySyntax;
    protected CompoundName(Enumeration<String> comps, Properties syntax) {
        if (syntax == null) {
            throw new NullPointerException();
        }
        mySyntax = syntax;
        impl = new NameImpl(syntax, comps);
    }
    public CompoundName(String n, Properties syntax) throws InvalidNameException {
        if (syntax == null) {
            throw new NullPointerException();
        }
        mySyntax = syntax;
        impl = new NameImpl(syntax, n);
    }
    public String toString() {
        return (impl.toString());
    }
    public boolean equals(Object obj) {
        return (obj != null &&
                obj instanceof CompoundName &&
                impl.equals(((CompoundName)obj).impl));
    }
    public int hashCode() {
        return impl.hashCode();
    }
    public Object clone() {
        return (new CompoundName(getAll(), mySyntax));
    }
    public int compareTo(Object obj) {
        if (!(obj instanceof CompoundName)) {
            throw new ClassCastException("Not a CompoundName");
        }
        return impl.compareTo(((CompoundName)obj).impl);
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
        return (new CompoundName(comps, mySyntax));
    }
    public Name getSuffix(int posn) {
        Enumeration comps = impl.getSuffix(posn);
        return (new CompoundName(comps, mySyntax));
    }
    public boolean startsWith(Name n) {
        if (n instanceof CompoundName) {
            return (impl.startsWith(n.size(), n.getAll()));
        } else {
            return false;
        }
    }
    public boolean endsWith(Name n) {
        if (n instanceof CompoundName) {
            return (impl.endsWith(n.size(), n.getAll()));
        } else {
            return false;
        }
    }
    public Name addAll(Name suffix) throws InvalidNameException {
        if (suffix instanceof CompoundName) {
            impl.addAll(suffix.getAll());
            return this;
        } else {
            throw new InvalidNameException("Not a compound name: " +
                suffix.toString());
        }
    }
    public Name addAll(int posn, Name n) throws InvalidNameException {
        if (n instanceof CompoundName) {
            impl.addAll(posn, n.getAll());
            return this;
        } else {
            throw new InvalidNameException("Not a compound name: " +
                n.toString());
        }
    }
    public Name add(String comp) throws InvalidNameException{
        impl.add(comp);
        return this;
    }
    public Name add(int posn, String comp) throws InvalidNameException{
        impl.add(posn, comp);
        return this;
    }
    public Object remove(int posn) throws InvalidNameException {
        return impl.remove(posn);
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.writeObject(mySyntax);
        s.writeInt(size());
        Enumeration comps = getAll();
        while (comps.hasMoreElements()) {
            s.writeObject(comps.nextElement());
        }
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        mySyntax = (Properties)s.readObject();
        impl = new NameImpl(mySyntax);
        int n = s.readInt();    
        try {
            while (--n >= 0) {
                add((String)s.readObject());
            }
        } catch (InvalidNameException e) {
            throw (new java.io.StreamCorruptedException("Invalid name"));
        }
    }
    private static final long serialVersionUID = 3513100557083972036L;
}
