public final class AttributeSetUtilities {
    private AttributeSetUtilities() {
    }
    private static class UnmodifiableAttributeSet
        implements AttributeSet, Serializable {
        private AttributeSet attrset;
        public UnmodifiableAttributeSet(AttributeSet attributeSet) {
            attrset = attributeSet;
        }
        public Attribute get(Class<?> key) {
            return attrset.get(key);
        }
        public boolean add(Attribute attribute) {
            throw new UnmodifiableSetException();
        }
        public synchronized boolean remove(Class<?> category) {
            throw new UnmodifiableSetException();
        }
        public boolean remove(Attribute attribute) {
            throw new UnmodifiableSetException();
        }
        public boolean containsKey(Class<?> category) {
            return attrset.containsKey(category);
        }
        public boolean containsValue(Attribute attribute) {
            return attrset.containsValue(attribute);
        }
        public boolean addAll(AttributeSet attributes) {
            throw new UnmodifiableSetException();
        }
        public int size() {
            return attrset.size();
        }
        public Attribute[] toArray() {
            return attrset.toArray();
        }
        public void clear() {
            throw new UnmodifiableSetException();
        }
        public boolean isEmpty() {
            return attrset.isEmpty();
        }
        public boolean equals(Object o) {
            return attrset.equals (o);
        }
        public int hashCode() {
            return attrset.hashCode();
        }
    }
    private static class UnmodifiableDocAttributeSet
        extends UnmodifiableAttributeSet
        implements DocAttributeSet, Serializable {
        public UnmodifiableDocAttributeSet(DocAttributeSet attributeSet) {
            super (attributeSet);
        }
    }
    private static class UnmodifiablePrintRequestAttributeSet
        extends UnmodifiableAttributeSet
        implements PrintRequestAttributeSet, Serializable
    {
        public UnmodifiablePrintRequestAttributeSet
            (PrintRequestAttributeSet attributeSet) {
            super (attributeSet);
        }
    }
    private static class UnmodifiablePrintJobAttributeSet
        extends UnmodifiableAttributeSet
        implements PrintJobAttributeSet, Serializable
    {
        public UnmodifiablePrintJobAttributeSet
            (PrintJobAttributeSet attributeSet) {
            super (attributeSet);
        }
    }
    private static class UnmodifiablePrintServiceAttributeSet
        extends UnmodifiableAttributeSet
        implements PrintServiceAttributeSet, Serializable
    {
        public UnmodifiablePrintServiceAttributeSet
            (PrintServiceAttributeSet attributeSet) {
            super (attributeSet);
        }
    }
    public static AttributeSet unmodifiableView(AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableAttributeSet(attributeSet);
    }
    public static DocAttributeSet unmodifiableView
        (DocAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableDocAttributeSet(attributeSet);
    }
    public static PrintRequestAttributeSet
        unmodifiableView(PrintRequestAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new UnmodifiablePrintRequestAttributeSet(attributeSet);
    }
    public static PrintJobAttributeSet
        unmodifiableView(PrintJobAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new UnmodifiablePrintJobAttributeSet(attributeSet);
    }
    public static PrintServiceAttributeSet
        unmodifiableView(PrintServiceAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new UnmodifiablePrintServiceAttributeSet (attributeSet);
    }
    private static class SynchronizedAttributeSet
                        implements AttributeSet, Serializable {
        private AttributeSet attrset;
        public SynchronizedAttributeSet(AttributeSet attributeSet) {
            attrset = attributeSet;
        }
        public synchronized Attribute get(Class<?> category) {
            return attrset.get(category);
        }
        public synchronized boolean add(Attribute attribute) {
            return attrset.add(attribute);
        }
        public synchronized boolean remove(Class<?> category) {
            return attrset.remove(category);
        }
        public synchronized boolean remove(Attribute attribute) {
            return attrset.remove(attribute);
        }
        public synchronized boolean containsKey(Class<?> category) {
            return attrset.containsKey(category);
        }
        public synchronized boolean containsValue(Attribute attribute) {
            return attrset.containsValue(attribute);
        }
        public synchronized boolean addAll(AttributeSet attributes) {
            return attrset.addAll(attributes);
        }
        public synchronized int size() {
            return attrset.size();
        }
        public synchronized Attribute[] toArray() {
            return attrset.toArray();
        }
        public synchronized void clear() {
            attrset.clear();
        }
        public synchronized boolean isEmpty() {
            return attrset.isEmpty();
        }
        public synchronized boolean equals(Object o) {
            return attrset.equals (o);
        }
        public synchronized int hashCode() {
            return attrset.hashCode();
        }
    }
    private static class SynchronizedDocAttributeSet
        extends SynchronizedAttributeSet
        implements DocAttributeSet, Serializable {
        public SynchronizedDocAttributeSet(DocAttributeSet attributeSet) {
            super(attributeSet);
        }
    }
    private static class SynchronizedPrintRequestAttributeSet
        extends SynchronizedAttributeSet
        implements PrintRequestAttributeSet, Serializable {
        public SynchronizedPrintRequestAttributeSet
            (PrintRequestAttributeSet attributeSet) {
            super(attributeSet);
        }
    }
    private static class SynchronizedPrintJobAttributeSet
        extends SynchronizedAttributeSet
        implements PrintJobAttributeSet, Serializable {
        public SynchronizedPrintJobAttributeSet
            (PrintJobAttributeSet attributeSet) {
            super(attributeSet);
        }
    }
    private static class SynchronizedPrintServiceAttributeSet
        extends SynchronizedAttributeSet
        implements PrintServiceAttributeSet, Serializable {
        public SynchronizedPrintServiceAttributeSet
            (PrintServiceAttributeSet attributeSet) {
            super(attributeSet);
        }
    }
    public static AttributeSet synchronizedView
        (AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new SynchronizedAttributeSet(attributeSet);
    }
    public static DocAttributeSet
        synchronizedView(DocAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new SynchronizedDocAttributeSet(attributeSet);
    }
    public static PrintRequestAttributeSet
        synchronizedView(PrintRequestAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new SynchronizedPrintRequestAttributeSet(attributeSet);
    }
    public static PrintJobAttributeSet
        synchronizedView(PrintJobAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new SynchronizedPrintJobAttributeSet(attributeSet);
    }
    public static PrintServiceAttributeSet
        synchronizedView(PrintServiceAttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new NullPointerException();
        }
        return new SynchronizedPrintServiceAttributeSet(attributeSet);
    }
    public static Class<?>
        verifyAttributeCategory(Object object, Class<?> interfaceName) {
        Class result = (Class) object;
        if (interfaceName.isAssignableFrom (result)) {
            return result;
        }
        else {
            throw new ClassCastException();
        }
    }
    public static Attribute
        verifyAttributeValue(Object object, Class<?> interfaceName) {
        if (object == null) {
            throw new NullPointerException();
        }
        else if (interfaceName.isInstance (object)) {
            return (Attribute) object;
        } else {
            throw new ClassCastException();
        }
    }
    public static void
        verifyCategoryForValue(Class<?> category, Attribute attribute) {
        if (!category.equals (attribute.getCategory())) {
            throw new IllegalArgumentException();
        }
    }
}
