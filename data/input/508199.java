public class ObjectStreamField implements Comparable<Object> {
    static final int FIELD_IS_NOT_RESOLVED = -1;
    static final int FIELD_IS_ABSENT = -2;
    private String name;
    private Object type;
    int offset;
    private String typeString;
    private boolean unshared;
    private boolean isDeserialized;
    private long assocFieldID = FIELD_IS_NOT_RESOLVED;
    public ObjectStreamField(String name, Class<?> cl) {
        if (name == null || cl == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = new WeakReference<Class<?>>(cl);
    }
    public ObjectStreamField(String name, Class<?> cl, boolean unshared) {
        if (name == null || cl == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = (cl.getClassLoader() == null) ? cl
                : new WeakReference<Class<?>>(cl);
        this.unshared = unshared;
    }
    ObjectStreamField(String signature, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.typeString = signature.replace('.', '/').intern();
        this.isDeserialized = true;
    }
    public int compareTo(Object o) {
        ObjectStreamField f = (ObjectStreamField) o;
        boolean thisPrimitive = this.isPrimitive();
        boolean fPrimitive = f.isPrimitive();
        if (thisPrimitive != fPrimitive) {
            return thisPrimitive ? -1 : 1;
        }
        return this.getName().compareTo(f.getName());
    }
    public String getName() {
        return name;
    }
    public int getOffset() {
        return offset;
    }
     Class<?> getTypeInternal() {
        if (type instanceof WeakReference) {
            return (Class<?>) ((WeakReference<?>) type).get();
        }
        return (Class<?>) type;
    }
    public Class<?> getType() {
        Class<?> cl = getTypeInternal();
        if (isDeserialized && !cl.isPrimitive()) {
            return Object.class;
        }
        return cl;
    }
    public char getTypeCode() {
        Class<?> t = getTypeInternal();
        if (t == Integer.TYPE) {
            return 'I';
        }
        if (t == Byte.TYPE) {
            return 'B';
        }
        if (t == Character.TYPE) {
            return 'C';
        }
        if (t == Short.TYPE) {
            return 'S';
        }
        if (t == Boolean.TYPE) {
            return 'Z';
        }
        if (t == Long.TYPE) {
            return 'J';
        }
        if (t == Float.TYPE) {
            return 'F';
        }
        if (t == Double.TYPE) {
            return 'D';
        }
        if (t.isArray()) {
            return '[';
        }
        return 'L';
    }
    public String getTypeString() {
        if (isPrimitive()) {
            return null;
        }
        if (typeString == null) {
            Class<?> t = getTypeInternal();
            String typeName = t.getName().replace('.', '/');
            String str = (t.isArray()) ? typeName : ("L" + typeName + ';'); 
            typeString = str.intern();
        }
        return typeString;
    }
    public boolean isPrimitive() {
        Class<?> t = getTypeInternal();
        return t != null && t.isPrimitive();
    }
    protected void setOffset(int newValue) {
        this.offset = newValue;
    }
    @Override
    public String toString() {
        return this.getClass().getName() + '(' + getName() + ':'
                + getTypeInternal() + ')';
    }
    static void sortFields(ObjectStreamField[] fields) {
        if (fields.length > 1) {
            Comparator<ObjectStreamField> fieldDescComparator = new Comparator<ObjectStreamField>() {
                public int compare(ObjectStreamField f1, ObjectStreamField f2) {
                    return f1.compareTo(f2);
                }
            };
            Arrays.sort(fields, fieldDescComparator);
        }
    }
    void resolve(ClassLoader loader) {
        if (typeString.length() == 1) {
            switch (typeString.charAt(0)) {
                case 'I':
                    type = Integer.TYPE;
                    return;
                case 'B':
                    type = Byte.TYPE;
                    return;
                case 'C':
                    type = Character.TYPE;
                    return;
                case 'S':
                    type = Short.TYPE;
                    return;
                case 'Z':
                    type = Boolean.TYPE;
                    return;
                case 'J':
                    type = Long.TYPE;
                    return;
                case 'F':
                    type = Float.TYPE;
                    return;
                case 'D':
                    type = Double.TYPE;
                    return;
            }
        }
        String className = typeString.replace('/', '.');
        if (className.charAt(0) == 'L') {
            className = className.substring(1, className.length() - 1);
        }
        try {
            Class<?> cl = Class.forName(className, false, loader);
            type = (cl.getClassLoader() == null) ? cl
                    : new WeakReference<Class<?>>(cl);
        } catch (ClassNotFoundException e) {
        }
    }
    public boolean isUnshared() {
        return unshared;
    }
    void setUnshared(boolean unshared) {
        this.unshared = unshared;
    }
}
