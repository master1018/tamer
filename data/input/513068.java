class MethodSourcer implements MethodVisitor {
    private final Output mOutput;
    private final int mAccess;
    private final String mClassName;
    private final String mName;
    private final String mDesc;
    private final String mSignature;
    private final String[] mExceptions;
    private boolean mNeedDeclaration;
    private boolean mIsConstructor;
    public MethodSourcer(Output output, String className, int access, String name,
            String desc, String signature, String[] exceptions) {
        mOutput = output;
        mClassName = className;
        mAccess = access;
        mName = name;
        mDesc = desc;
        mSignature = signature;
        mExceptions = exceptions;
        mNeedDeclaration = true;
        mIsConstructor = "<init>".equals(name);
    }
    private void writeHeader() {        
        if (!mNeedDeclaration) {
            return;
        }
        AccessSourcer as = new AccessSourcer(mOutput);
        as.write(mAccess, AccessSourcer.IS_METHOD);
        SignatureSourcer sigSourcer = null;
        if (mSignature != null) {
            SignatureReader sigReader = new SignatureReader(mSignature);
            sigSourcer = new SignatureSourcer();
            sigReader.accept(sigSourcer);
            if (sigSourcer.hasFormalsContent()) {
                mOutput.write(" %s", sigSourcer.formalsToString());
            }
        }
        if (!mIsConstructor) {
            if (sigSourcer == null || sigSourcer.getReturnType() == null) {
                mOutput.write(" %s", Type.getReturnType(mDesc).getClassName());
            } else {
                mOutput.write(" %s", sigSourcer.getReturnType().toString());
            }
        }
        mOutput.write(" %s(", mIsConstructor ? mClassName : mName);
        if (mSignature == null) {
            Type[] types = Type.getArgumentTypes(mDesc);
            for(int i = 0; i < types.length; i++) {
                if (i > 0) {
                    mOutput.write(", ");
                }
                mOutput.write("%s arg%d", types[i].getClassName(), i);
            }
        } else {
            ArrayList<SignatureSourcer> params = sigSourcer.getParameters();
            for(int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    mOutput.write(", ");
                }
                mOutput.write("%s arg%d", params.get(i).toString(), i);
            }
        }
        mOutput.write(")");
        if (mExceptions != null && mExceptions.length > 0) {
            mOutput.write(" throws ");
            for (int i = 0; i < mExceptions.length; i++) {
                if (i > 0) {
                    mOutput.write(", ");
                }
                mOutput.write(mExceptions[i].replace('/', '.'));
            }
        }
        mOutput.write(" {\n");
        mNeedDeclaration = false;
    }
    public void visitCode() {
        writeHeader();
        mOutput.write("throw new RuntimeException(\"Stub\");");
    }
    public void visitEnd() {
        writeHeader();
        mOutput.write("\n}\n");
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        mOutput.write("@%s", desc);
        return new AnnotationSourcer(mOutput);
    }
    public AnnotationVisitor visitAnnotationDefault() {
        return null;
    }
    public void visitAttribute(Attribute attr) {
        mOutput.write("%s  ", attr.type);
    }
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    }
    public void visitFrame(int type, int local, Object[] local2, int stack, Object[] stack2) {
    }
    public void visitIincInsn(int var, int increment) {
    }
    public void visitInsn(int opcode) {
    }
    public void visitIntInsn(int opcode, int operand) {
    }
    public void visitJumpInsn(int opcode, Label label) {
    }
    public void visitLabel(Label label) {
    }
    public void visitLdcInsn(Object cst) {
    }
    public void visitLineNumber(int line, Label start) {
    }
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
    }
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }
    public void visitMaxs(int maxStack, int maxLocals) {
    }
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    }
    public void visitMultiANewArrayInsn(String desc, int dims) {
    }
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return null;
    }
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
    }
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }
    public void visitTypeInsn(int opcode, String type) {
    }
    public void visitVarInsn(int opcode, int var) {
    }
}
