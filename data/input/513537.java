class FieldSourcer implements FieldVisitor {
    private final Output mOutput;
    private final int mAccess;
    private final String mName;
    private final String mDesc;
    private final String mSignature;
    public FieldSourcer(Output output, int access, String name, String desc, String signature) {
        mOutput = output;
        mAccess = access;
        mName = name;
        mDesc = desc;
        mSignature = signature;
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        mOutput.write("@%s", desc);
        return new AnnotationSourcer(mOutput);
    }
    public void visitAttribute(Attribute attr) {
        mOutput.write("%s  ", attr.type);
    }
    public void visitEnd() {
        AccessSourcer as = new AccessSourcer(mOutput);
        as.write(mAccess, AccessSourcer.IS_FIELD);
        if (mSignature == null) {
            mOutput.write(" %s", Type.getType(mDesc).getClassName());
        } else {
            mOutput.write(" ");
            SignatureReader sigReader = new SignatureReader(mSignature);
            SignatureSourcer sigSourcer = new SignatureSourcer();
            sigReader.acceptType(sigSourcer);
            mOutput.write(sigSourcer.toString());
        }
        mOutput.write(" %s", mName);
        mOutput.write(";\n");
    }
}
