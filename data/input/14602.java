public class JavaClass extends JavaHeapObject {
    private long id;
    private String name;
    private JavaThing superclass;
    private JavaThing loader;
    private JavaThing signers;
    private JavaThing protectionDomain;
    private JavaField[] fields;
    private JavaStatic[] statics;
    private static final JavaClass[] EMPTY_CLASS_ARRAY = new JavaClass[0];
    private JavaClass[] subclasses = EMPTY_CLASS_ARRAY;
    private Vector<JavaHeapObject> instances = new Vector<JavaHeapObject>();
    private Snapshot mySnapshot;
    private int instanceSize;
    private int totalNumFields;
    public JavaClass(long id, String name, long superclassId, long loaderId,
                     long signersId, long protDomainId,
                     JavaField[] fields, JavaStatic[] statics,
                     int instanceSize) {
        this.id = id;
        this.name = name;
        this.superclass = new JavaObjectRef(superclassId);
        this.loader = new JavaObjectRef(loaderId);
        this.signers = new JavaObjectRef(signersId);
        this.protectionDomain = new JavaObjectRef(protDomainId);
        this.fields = fields;
        this.statics = statics;
        this.instanceSize = instanceSize;
    }
    public JavaClass(String name, long superclassId, long loaderId,
                     long signersId, long protDomainId,
                     JavaField[] fields, JavaStatic[] statics,
                     int instanceSize) {
        this(-1L, name, superclassId, loaderId, signersId,
             protDomainId, fields, statics, instanceSize);
    }
    public final JavaClass getClazz() {
        return mySnapshot.getJavaLangClass();
    }
    public final int getIdentifierSize() {
        return mySnapshot.getIdentifierSize();
    }
    public final int getMinimumObjectSize() {
        return mySnapshot.getMinimumObjectSize();
    }
    public void resolve(Snapshot snapshot) {
        if (mySnapshot != null) {
            return;
        }
        mySnapshot = snapshot;
        resolveSuperclass(snapshot);
        if (superclass != null) {
            ((JavaClass) superclass).addSubclass(this);
        }
        loader  = loader.dereference(snapshot, null);
        signers  = signers.dereference(snapshot, null);
        protectionDomain  = protectionDomain.dereference(snapshot, null);
        for (int i = 0; i < statics.length; i++) {
            statics[i].resolve(this, snapshot);
        }
        snapshot.getJavaLangClass().addInstance(this);
        super.resolve(snapshot);
        return;
    }
    public void resolveSuperclass(Snapshot snapshot) {
        if (superclass == null) {
        } else {
            totalNumFields = fields.length;
            superclass = superclass.dereference(snapshot, null);
            if (superclass == snapshot.getNullThing()) {
                superclass = null;
            } else {
                try {
                    JavaClass sc = (JavaClass) superclass;
                    sc.resolveSuperclass(snapshot);
                    totalNumFields += sc.totalNumFields;
                } catch (ClassCastException ex) {
                    System.out.println("Warning!  Superclass of " + name + " is " + superclass);
                    superclass = null;
                }
            }
        }
    }
    public boolean isString() {
        return mySnapshot.getJavaLangString() == this;
    }
    public boolean isClassLoader() {
        return mySnapshot.getJavaLangClassLoader().isAssignableFrom(this);
    }
    public JavaField getField(int i) {
        if (i < 0 || i >= fields.length) {
            throw new Error("No field " + i + " for " + name);
        }
        return fields[i];
    }
    public int getNumFieldsForInstance() {
        return totalNumFields;
    }
    public JavaField getFieldForInstance(int i) {
        if (superclass != null) {
            JavaClass sc = (JavaClass) superclass;
            if (i < sc.totalNumFields) {
                return sc.getFieldForInstance(i);
            }
            i -= sc.totalNumFields;
        }
        return getField(i);
    }
    public JavaClass getClassForField(int i) {
        if (superclass != null) {
            JavaClass sc = (JavaClass) superclass;
            if (i < sc.totalNumFields) {
                return sc.getClassForField(i);
            }
        }
        return this;
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isArray() {
        return name.indexOf('[') != -1;
    }
    public Enumeration getInstances(boolean includeSubclasses) {
        if (includeSubclasses) {
            Enumeration res = instances.elements();
            for (int i = 0; i < subclasses.length; i++) {
                res = new CompositeEnumeration(res,
                              subclasses[i].getInstances(true));
            }
            return res;
        } else {
            return instances.elements();
        }
    }
    public int getInstancesCount(boolean includeSubclasses) {
        int result = instances.size();
        if (includeSubclasses) {
            for (int i = 0; i < subclasses.length; i++) {
                result += subclasses[i].getInstancesCount(includeSubclasses);
            }
        }
        return result;
    }
    public JavaClass[] getSubclasses() {
        return subclasses;
    }
    public JavaClass getSuperclass() {
        return (JavaClass) superclass;
    }
    public JavaThing getLoader() {
        return loader;
    }
    public boolean isBootstrap() {
        return loader == mySnapshot.getNullThing();
    }
    public JavaThing getSigners() {
        return signers;
    }
    public JavaThing getProtectionDomain() {
        return protectionDomain;
    }
    public JavaField[] getFields() {
        return fields;
    }
    public JavaField[] getFieldsForInstance() {
        Vector<JavaField> v = new Vector<JavaField>();
        addFields(v);
        JavaField[] result = new JavaField[v.size()];
        for (int i = 0; i < v.size(); i++) {
            result[i] =  v.elementAt(i);
        }
        return result;
    }
    public JavaStatic[] getStatics() {
        return statics;
    }
    public JavaThing getStaticField(String name) {
        for (int i = 0; i < statics.length; i++) {
            JavaStatic s = statics[i];
            if (s.getField().getName().equals(name)) {
                return s.getValue();
            }
        }
        return null;
    }
    public String toString() {
        return "class " + name;
    }
    public int compareTo(JavaThing other) {
        if (other instanceof JavaClass) {
            return name.compareTo(((JavaClass) other).name);
        }
        return super.compareTo(other);
    }
    public boolean isAssignableFrom(JavaClass other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else {
            return isAssignableFrom((JavaClass) other.superclass);
        }
    }
     public String describeReferenceTo(JavaThing target, Snapshot ss) {
        for (int i = 0; i < statics.length; i++) {
            JavaField f = statics[i].getField();
            if (f.hasId()) {
                JavaThing other = statics[i].getValue();
                if (other == target) {
                    return "static field " + f.getName();
                }
            }
        }
        return super.describeReferenceTo(target, ss);
    }
    public int getInstanceSize() {
        return instanceSize + mySnapshot.getMinimumObjectSize();
    }
    public long getTotalInstanceSize() {
        int count = instances.size();
        if (count == 0 || !isArray()) {
            return count * instanceSize;
        }
        long result = 0;
        for (int i = 0; i < count; i++) {
            JavaThing t = (JavaThing) instances.elementAt(i);
            result += t.getSize();
        }
        return result;
    }
    public int getSize() {
        JavaClass cl = mySnapshot.getJavaLangClass();
        if (cl == null) {
            return 0;
        } else {
            return cl.getInstanceSize();
        }
    }
    public void visitReferencedObjects(JavaHeapObjectVisitor v) {
        super.visitReferencedObjects(v);
        JavaHeapObject sc = getSuperclass();
        if (sc != null) v.visit(getSuperclass());
        JavaThing other;
        other = getLoader();
        if (other instanceof JavaHeapObject) {
            v.visit((JavaHeapObject)other);
        }
        other = getSigners();
        if (other instanceof JavaHeapObject) {
            v.visit((JavaHeapObject)other);
        }
        other = getProtectionDomain();
        if (other instanceof JavaHeapObject) {
            v.visit((JavaHeapObject)other);
        }
        for (int i = 0; i < statics.length; i++) {
            JavaField f = statics[i].getField();
            if (!v.exclude(this, f) && f.hasId()) {
                other = statics[i].getValue();
                if (other instanceof JavaHeapObject) {
                    v.visit((JavaHeapObject) other);
                }
            }
        }
    }
    final ReadBuffer getReadBuffer() {
        return mySnapshot.getReadBuffer();
    }
    final void setNew(JavaHeapObject obj, boolean flag) {
        mySnapshot.setNew(obj, flag);
    }
    final boolean isNew(JavaHeapObject obj) {
        return mySnapshot.isNew(obj);
    }
    final StackTrace getSiteTrace(JavaHeapObject obj) {
        return mySnapshot.getSiteTrace(obj);
    }
    final void addReferenceFromRoot(Root root, JavaHeapObject obj) {
        mySnapshot.addReferenceFromRoot(root, obj);
    }
    final Root getRoot(JavaHeapObject obj) {
        return mySnapshot.getRoot(obj);
    }
    final Snapshot getSnapshot() {
        return mySnapshot;
    }
    void addInstance(JavaHeapObject inst) {
        instances.addElement(inst);
    }
    private void addFields(Vector<JavaField> v) {
        if (superclass != null) {
            ((JavaClass) superclass).addFields(v);
        }
        for (int i = 0; i < fields.length; i++) {
            v.addElement(fields[i]);
        }
    }
    private void addSubclassInstances(Vector<JavaHeapObject> v) {
        for (int i = 0; i < subclasses.length; i++) {
            subclasses[i].addSubclassInstances(v);
        }
        for (int i = 0; i < instances.size(); i++) {
            v.addElement(instances.elementAt(i));
        }
    }
    private void addSubclass(JavaClass sub) {
        JavaClass newValue[] = new JavaClass[subclasses.length + 1];
        System.arraycopy(subclasses, 0, newValue, 0, subclasses.length);
        newValue[subclasses.length] = sub;
        subclasses = newValue;
    }
}
