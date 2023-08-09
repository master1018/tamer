public abstract class JavaHeapObject extends JavaThing {
    private JavaThing[] referers = null;
    private int referersLen = 0;        
    public abstract JavaClass getClazz();
    public abstract int getSize();
    public abstract long getId();
    public void resolve(Snapshot snapshot) {
        StackTrace trace = snapshot.getSiteTrace(this);
        if (trace != null) {
            trace.resolve(snapshot);
        }
    }
    void setupReferers() {
        if (referersLen > 1) {
            Map<JavaThing, JavaThing> map = new HashMap<JavaThing, JavaThing>();
            for (int i = 0; i < referersLen; i++) {
                if (map.get(referers[i]) == null) {
                    map.put(referers[i], referers[i]);
                }
            }
            referers = new JavaThing[map.size()];
            map.keySet().toArray(referers);
        }
        referersLen = -1;
    }
    public String getIdString() {
        return Misc.toHex(getId());
    }
    public String toString() {
        return getClazz().getName() + "@" + getIdString();
    }
    public StackTrace getAllocatedFrom() {
        return getClazz().getSiteTrace(this);
    }
    public boolean isNew() {
        return getClazz().isNew(this);
    }
    void setNew(boolean flag) {
        getClazz().setNew(this, flag);
    }
    public void visitReferencedObjects(JavaHeapObjectVisitor v) {
        v.visit(getClazz());
    }
    void addReferenceFrom(JavaHeapObject other) {
        if (referersLen == 0) {
            referers = new JavaThing[1];        
        } else if (referersLen == referers.length) {
            JavaThing[] copy = new JavaThing[(3 * (referersLen + 1)) / 2];
            System.arraycopy(referers, 0, copy, 0, referersLen);
            referers = copy;
        }
        referers[referersLen++] = other;
    }
    void addReferenceFromRoot(Root r) {
        getClazz().addReferenceFromRoot(r, this);
    }
    public Root getRoot() {
        return getClazz().getRoot(this);
    }
    public Enumeration getReferers() {
        if (referersLen != -1) {
            throw new RuntimeException("not resolved: " + getIdString());
        }
        return new Enumeration() {
            private int num = 0;
            public boolean hasMoreElements() {
                return referers != null && num < referers.length;
            }
            public Object nextElement() {
                return referers[num++];
            }
        };
    }
    public boolean refersOnlyWeaklyTo(Snapshot ss, JavaThing other) {
        return false;
    }
    public String describeReferenceTo(JavaThing target, Snapshot ss) {
        return "??";
    }
    public boolean isHeapAllocated() {
        return true;
    }
}
