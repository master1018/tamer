public class BasicAttributes implements Attributes {
    private boolean ignoreCase = false;
    transient Hashtable attrs = new Hashtable(11);
    public BasicAttributes() {
    }
    public BasicAttributes(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    public BasicAttributes(String attrID, Object val) {
        this();
        this.put(new BasicAttribute(attrID, val));
    }
    public BasicAttributes(String attrID, Object val, boolean ignoreCase) {
        this(ignoreCase);
        this.put(new BasicAttribute(attrID, val));
    }
    public Object clone() {
        BasicAttributes attrset;
        try {
            attrset = (BasicAttributes)super.clone();
        } catch (CloneNotSupportedException e) {
            attrset = new BasicAttributes(ignoreCase);
        }
        attrset.attrs = (Hashtable)attrs.clone();
        return attrset;
    }
    public boolean isCaseIgnored() {
        return ignoreCase;
    }
    public int size() {
        return attrs.size();
    }
    public Attribute get(String attrID) {
        Attribute attr = (Attribute) attrs.get(
                ignoreCase ? attrID.toLowerCase() : attrID);
        return (attr);
    }
    public NamingEnumeration<Attribute> getAll() {
        return new AttrEnumImpl();
    }
    public NamingEnumeration<String> getIDs() {
        return new IDEnumImpl();
    }
    public Attribute put(String attrID, Object val) {
        return this.put(new BasicAttribute(attrID, val));
    }
    public Attribute put(Attribute attr) {
        String id = attr.getID();
        if (ignoreCase) {
            id = id.toLowerCase();
        }
        return (Attribute)attrs.put(id, attr);
    }
    public Attribute remove(String attrID) {
        String id = (ignoreCase ? attrID.toLowerCase() : attrID);
        return (Attribute)attrs.remove(id);
    }
    public String toString() {
        if (attrs.size() == 0) {
            return("No attributes");
        } else {
            return attrs.toString();
        }
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Attributes)) {
            Attributes target = (Attributes)obj;
            if (ignoreCase != target.isCaseIgnored()) {
                return false;
            }
            if (size() == target.size()) {
                Attribute their, mine;
                try {
                    NamingEnumeration theirs = target.getAll();
                    while (theirs.hasMore()) {
                        their = (Attribute)theirs.next();
                        mine = get(their.getID());
                        if (!their.equals(mine)) {
                            return false;
                        }
                    }
                } catch (NamingException e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    public int hashCode() {
        int hash = (ignoreCase ? 1 : 0);
        try {
            NamingEnumeration all = getAll();
            while (all.hasMore()) {
                hash += all.next().hashCode();
            }
        } catch (NamingException e) {}
        return hash;
    }
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject(); 
        s.writeInt(attrs.size());
        Enumeration attrEnum = attrs.elements();
        while (attrEnum.hasMoreElements()) {
            s.writeObject(attrEnum.nextElement());
        }
    }
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();  
        int n = s.readInt();    
        attrs = (n >= 1)
            ? new Hashtable(n * 2)
            : new Hashtable(2); 
        while (--n >= 0) {
            put((Attribute)s.readObject());
        }
    }
class AttrEnumImpl implements NamingEnumeration<Attribute> {
    Enumeration<Attribute> elements;
    public AttrEnumImpl() {
        this.elements = attrs.elements();
    }
    public boolean hasMoreElements() {
        return elements.hasMoreElements();
    }
    public Attribute nextElement() {
        return elements.nextElement();
    }
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }
    public Attribute next() throws NamingException {
        return nextElement();
    }
    public void close() throws NamingException {
        elements = null;
    }
}
class IDEnumImpl implements NamingEnumeration<String> {
    Enumeration<Attribute> elements;
    public IDEnumImpl() {
        this.elements = attrs.elements();
    }
    public boolean hasMoreElements() {
        return elements.hasMoreElements();
    }
    public String nextElement() {
        Attribute attr = elements.nextElement();
        return attr.getID();
    }
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }
    public String next() throws NamingException {
        return nextElement();
    }
    public void close() throws NamingException {
        elements = null;
    }
}
    private static final long serialVersionUID = 4980164073184639448L;
}
