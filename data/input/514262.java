public class ClassRef {
    private String mClassName;
    private ArrayList<FieldRef> mFieldRefs;
    private ArrayList<MethodRef> mMethodRefs;
    public ClassRef(String className) {
        mClassName = className;
        mFieldRefs = new ArrayList<FieldRef>();
        mMethodRefs = new ArrayList<MethodRef>();
    }
    public void addField(FieldRef fref) {
        mFieldRefs.add(fref);
    }
    public FieldRef[] getFieldArray() {
        return mFieldRefs.toArray(new FieldRef[mFieldRefs.size()]);
    }
    public void addMethod(MethodRef mref) {
        mMethodRefs.add(mref);
    }
    public MethodRef[] getMethodArray() {
        return mMethodRefs.toArray(new MethodRef[mMethodRefs.size()]);
    }
    public String getName() {
        return mClassName;
    }
}
