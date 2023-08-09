class AnnotationSourcer implements AnnotationVisitor {
    private final String mOpenChar;
    private final String mCloseChar;
    private final Output mOutput;
    private boolean mNeedClose;
    public AnnotationSourcer(Output output) {
        this(output, false );
    }
    public AnnotationSourcer(Output output, boolean isArray) {
        mOutput = output;
        mOpenChar = isArray ? "[" : "(";
        mCloseChar = isArray ? "]" : ")";
    }
    public void visit(String name, Object value) {
        startOpen();
        if (name != null) {
            mOutput.write("%s=", name);
        }
        if (value != null) {
            mOutput.write(name.toString());
        }
    }
    private void startOpen() {
        if (!mNeedClose) {
            mNeedClose = true;
            mOutput.write(mOpenChar);
        }
    }
    public void visitEnd() {
        if (mNeedClose) {
            mOutput.write(mCloseChar);
        }
        mOutput.write("\n");
    }
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        startOpen();
        mOutput.write("@%s", name);
        return this;
    }
    public AnnotationVisitor visitArray(String name) {
        startOpen();
        return new AnnotationSourcer(mOutput, true );
    }
    public void visitEnum(String name, String desc, String value) {
        mOutput.write("\n", name);
    }
}
