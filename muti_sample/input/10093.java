public class JavaObjectArray extends JavaLazyReadObject {
    private Object clazz;  
    public JavaObjectArray(long classID, long offset) {
        super(offset);
        this.clazz = makeId(classID);
    }
    public JavaClass getClazz() {
        return (JavaClass) clazz;
    }
    public void resolve(Snapshot snapshot) {
        if (clazz instanceof JavaClass) {
            return;
        }
        long classID = getIdValue((Number)clazz);
        if (snapshot.isNewStyleArrayClass()) {
            JavaThing t = snapshot.findThing(classID);
            if (t instanceof JavaClass) {
                clazz = (JavaClass) t;
            }
        }
        if (!(clazz instanceof JavaClass)) {
            JavaThing t = snapshot.findThing(classID);
            if (t != null && t instanceof JavaClass) {
                JavaClass el = (JavaClass) t;
                String nm = el.getName();
                if (!nm.startsWith("[")) {
                    nm = "L" + el.getName() + ";";
                }
                clazz = snapshot.getArrayClass(nm);
            }
        }
        if (!(clazz instanceof JavaClass)) {
            clazz = snapshot.getOtherArrayType();
        }
        ((JavaClass)clazz).addInstance(this);
        super.resolve(snapshot);
    }
    public JavaThing[] getValues() {
        return getElements();
    }
    public JavaThing[] getElements() {
        Snapshot snapshot = getClazz().getSnapshot();
        byte[] data = getValue();
        final int idSize = snapshot.getIdentifierSize();
        final int numElements = data.length / idSize;
        JavaThing[] elements = new JavaThing[numElements];
        int index = 0;
        for (int i = 0; i < elements.length; i++) {
            long id = objectIdAt(index, data);
            index += idSize;
            elements[i] = snapshot.findThing(id);
        }
        return elements;
    }
    public int compareTo(JavaThing other) {
        if (other instanceof JavaObjectArray) {
            return 0;
        }
        return super.compareTo(other);
    }
    public int getLength() {
        return getValueLength() / getClazz().getIdentifierSize();
    }
    public void visitReferencedObjects(JavaHeapObjectVisitor v) {
        super.visitReferencedObjects(v);
        JavaThing[] elements = getElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null && elements[i] instanceof JavaHeapObject) {
                v.visit((JavaHeapObject) elements[i]);
            }
        }
    }
    public String describeReferenceTo(JavaThing target, Snapshot ss) {
        JavaThing[] elements = getElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == target) {
                return "Element " + i + " of " + this;
            }
        }
        return super.describeReferenceTo(target, ss);
    }
    protected final int readValueLength() throws IOException {
        JavaClass cl = getClazz();
        ReadBuffer buf = cl.getReadBuffer();
        int idSize = cl.getIdentifierSize();
        long offset = getOffset() + idSize + 4;
        int len = buf.getInt(offset);
        return len * cl.getIdentifierSize();
    }
    protected final byte[] readValue() throws IOException {
        JavaClass cl = getClazz();
        ReadBuffer buf = cl.getReadBuffer();
        int idSize = cl.getIdentifierSize();
        long offset = getOffset() + idSize + 4;
        int len = buf.getInt(offset);
        if (len == 0) {
            return Snapshot.EMPTY_BYTE_ARRAY;
        } else {
            byte[] res = new byte[len * idSize];
            buf.get(offset + 4 + idSize, res);
            return res;
        }
    }
}
