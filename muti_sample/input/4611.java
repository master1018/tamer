public class SimpleAttributeSet implements MutableAttributeSet, Serializable, Cloneable
{
    private static final long serialVersionUID = -6631553454711782652L;
    public static final AttributeSet EMPTY = new EmptyAttributeSet();
    private transient Hashtable<Object, Object> table = new Hashtable<Object, Object>(3);
    public SimpleAttributeSet() {
    }
    public SimpleAttributeSet(AttributeSet source) {
        addAttributes(source);
    }
    private SimpleAttributeSet(Hashtable<Object, Object> table) {
        this.table = table;
    }
    public boolean isEmpty()
    {
        return table.isEmpty();
    }
    public int getAttributeCount() {
        return table.size();
    }
    public boolean isDefined(Object attrName) {
        return table.containsKey(attrName);
    }
    public boolean isEqual(AttributeSet attr) {
        return ((getAttributeCount() == attr.getAttributeCount()) &&
                containsAttributes(attr));
    }
    public AttributeSet copyAttributes() {
        return (AttributeSet) clone();
    }
    public Enumeration<?> getAttributeNames() {
        return table.keys();
    }
    public Object getAttribute(Object name) {
        Object value = table.get(name);
        if (value == null) {
            AttributeSet parent = getResolveParent();
            if (parent != null) {
                value = parent.getAttribute(name);
            }
        }
        return value;
    }
    public boolean containsAttribute(Object name, Object value) {
        return value.equals(getAttribute(name));
    }
    public boolean containsAttributes(AttributeSet attributes) {
        boolean result = true;
        Enumeration names = attributes.getAttributeNames();
        while (result && names.hasMoreElements()) {
            Object name = names.nextElement();
            result = attributes.getAttribute(name).equals(getAttribute(name));
        }
        return result;
    }
    public void addAttribute(Object name, Object value) {
        table.put(name, value);
    }
    public void addAttributes(AttributeSet attributes) {
        Enumeration names = attributes.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            addAttribute(name, attributes.getAttribute(name));
        }
    }
    public void removeAttribute(Object name) {
        table.remove(name);
    }
    public void removeAttributes(Enumeration<?> names) {
        while (names.hasMoreElements())
            removeAttribute(names.nextElement());
    }
    public void removeAttributes(AttributeSet attributes) {
        if (attributes == this) {
            table.clear();
        }
        else {
            Enumeration names = attributes.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = attributes.getAttribute(name);
                if (value.equals(getAttribute(name)))
                    removeAttribute(name);
            }
        }
    }
    public AttributeSet getResolveParent() {
        return (AttributeSet) table.get(StyleConstants.ResolveAttribute);
    }
    public void setResolveParent(AttributeSet parent) {
        addAttribute(StyleConstants.ResolveAttribute, parent);
    }
    public Object clone() {
        SimpleAttributeSet attr;
        try {
            attr = (SimpleAttributeSet) super.clone();
            attr.table = (Hashtable) table.clone();
        } catch (CloneNotSupportedException cnse) {
            attr = null;
        }
        return attr;
    }
    public int hashCode() {
        return table.hashCode();
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AttributeSet) {
            AttributeSet attrs = (AttributeSet) obj;
            return isEqual(attrs);
        }
        return false;
    }
    public String toString() {
        String s = "";
        Enumeration names = getAttributeNames();
        while (names.hasMoreElements()) {
            Object key = names.nextElement();
            Object value = getAttribute(key);
            if (value instanceof AttributeSet) {
                s = s + key + "=**AttributeSet** ";
            } else {
                s = s + key + "=" + value + " ";
            }
        }
        return s;
    }
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        StyleContext.writeAttributeSet(s, this);
    }
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        table = new Hashtable<Object, Object>(3);
        StyleContext.readAttributeSet(s, this);
    }
    static class EmptyAttributeSet implements AttributeSet, Serializable {
        static final long serialVersionUID = -8714803568785904228L;
        public int getAttributeCount() {
            return 0;
        }
        public boolean isDefined(Object attrName) {
            return false;
        }
        public boolean isEqual(AttributeSet attr) {
            return (attr.getAttributeCount() == 0);
        }
        public AttributeSet copyAttributes() {
            return this;
        }
        public Object getAttribute(Object key) {
            return null;
        }
        public Enumeration getAttributeNames() {
            return Collections.emptyEnumeration();
        }
        public boolean containsAttribute(Object name, Object value) {
            return false;
        }
        public boolean containsAttributes(AttributeSet attributes) {
            return (attributes.getAttributeCount() == 0);
        }
        public AttributeSet getResolveParent() {
            return null;
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            return ((obj instanceof AttributeSet) &&
                    (((AttributeSet)obj).getAttributeCount() == 0));
        }
        public int hashCode() {
            return 0;
        }
    }
}
