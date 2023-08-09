class StubMethodAdapter implements MethodVisitor {
    private static String CONSTRUCTOR = "<init>";
    private static String CLASS_INIT = "<clinit>";
    private MethodVisitor mParentVisitor;
    private Type mReturnType;
    private String mInvokeSignature;
    private boolean mOutputFirstLineNumber = true;
    private boolean mIsInitMethod = false;
    private boolean mMessageGenerated;
    private final boolean mIsStatic;
    private final boolean mIsNative;
    public StubMethodAdapter(MethodVisitor mv, String methodName, Type returnType,
            String invokeSignature, boolean isStatic, boolean isNative) {
        mParentVisitor = mv;
        mReturnType = returnType;
        mInvokeSignature = invokeSignature;
        mIsStatic = isStatic;
        mIsNative = isNative;
        if (CONSTRUCTOR.equals(methodName) || CLASS_INIT.equals(methodName)) {
            mIsInitMethod = true;
        }
    }
    private void generateInvoke() {
        mParentVisitor.visitLdcInsn(mInvokeSignature);
        mParentVisitor.visitInsn(mIsNative ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        if (mIsStatic) {
            mParentVisitor.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mParentVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        }
        int sort = mReturnType != null ? mReturnType.getSort() : Type.VOID;
        switch(sort) {
        case Type.VOID:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeV",
                    "(Ljava/lang/String;ZLjava/lang/Object;)V");
            mParentVisitor.visitInsn(Opcodes.RETURN);
            break;
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeI",
                    "(Ljava/lang/String;ZLjava/lang/Object;)I");
            switch(sort) {
            case Type.BOOLEAN:
                Label l1 = new Label();
                mParentVisitor.visitJumpInsn(Opcodes.IFEQ, l1);
                mParentVisitor.visitInsn(Opcodes.ICONST_1);
                mParentVisitor.visitInsn(Opcodes.IRETURN);
                mParentVisitor.visitLabel(l1);
                mParentVisitor.visitInsn(Opcodes.ICONST_0);
                break;
            case Type.CHAR:
                mParentVisitor.visitInsn(Opcodes.I2C);
                break;
            case Type.BYTE:
                mParentVisitor.visitInsn(Opcodes.I2B);
                break;
            case Type.SHORT:
                mParentVisitor.visitInsn(Opcodes.I2S);
                break;
            }
            mParentVisitor.visitInsn(Opcodes.IRETURN);
            break;
        case Type.LONG:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeL",
                    "(Ljava/lang/String;ZLjava/lang/Object;)J");
            mParentVisitor.visitInsn(Opcodes.LRETURN);
            break;
        case Type.FLOAT:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeF",
                    "(Ljava/lang/String;ZLjava/lang/Object;)F");
            mParentVisitor.visitInsn(Opcodes.FRETURN);
            break;
        case Type.DOUBLE:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeD",
                    "(Ljava/lang/String;ZLjava/lang/Object;)D");
            mParentVisitor.visitInsn(Opcodes.DRETURN);
            break;
        case Type.ARRAY:
        case Type.OBJECT:
            mParentVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/android/tools/layoutlib/create/OverrideMethod",
                    "invokeA",
                    "(Ljava/lang/String;ZLjava/lang/Object;)Ljava/lang/Object;");
            mParentVisitor.visitTypeInsn(Opcodes.CHECKCAST, mReturnType.getInternalName());
            mParentVisitor.visitInsn(Opcodes.ARETURN);
            break;
        }
    }
    private void generatePop() {
        switch(mReturnType != null ? mReturnType.getSort() : Type.VOID) {
        case Type.VOID:
            break;
        case Type.BOOLEAN:
        case Type.CHAR:
        case Type.BYTE:
        case Type.SHORT:
        case Type.INT:
        case Type.FLOAT:
        case Type.ARRAY:
        case Type.OBJECT:
            mParentVisitor.visitInsn(Opcodes.POP);
            break;
        case Type.LONG:
        case Type.DOUBLE:
            mParentVisitor.visitInsn(Opcodes.POP2);
            break;
        }
    }
    public void visitCode() {
        mParentVisitor.visitCode();
    }
    public void visitMaxs(int maxStack, int maxLocals) {
        if (!mIsInitMethod && !mMessageGenerated) {
            generateInvoke();
            mMessageGenerated = true;
        }
        mParentVisitor.visitMaxs(maxStack, maxLocals);
    }
    public void visitEnd() {
        if (!mIsInitMethod && !mMessageGenerated) {
            generateInvoke();
            mMessageGenerated = true;
            mParentVisitor.visitMaxs(1, 1);
        }
        mParentVisitor.visitEnd();
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return mParentVisitor.visitAnnotation(desc, visible);
    }
    public AnnotationVisitor visitAnnotationDefault() {
        return mParentVisitor.visitAnnotationDefault();
    }
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
            boolean visible) {
        return mParentVisitor.visitParameterAnnotation(parameter, desc, visible);
    }
    public void visitAttribute(Attribute attr) {
        mParentVisitor.visitAttribute(attr);
    }
    public void visitLineNumber(int line, Label start) {
        if (mIsInitMethod || mOutputFirstLineNumber) {
            mParentVisitor.visitLineNumber(line, start);
            mOutputFirstLineNumber = false;
        }
    }
    public void visitInsn(int opcode) {
        if (mIsInitMethod) {
            switch (opcode) {
            case Opcodes.RETURN:
            case Opcodes.ARETURN:
            case Opcodes.DRETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
                generatePop();
                generateInvoke();
                mMessageGenerated = true;
            default:
                mParentVisitor.visitInsn(opcode);
            }
        }
    }
    public void visitLabel(Label label) {
        if (mIsInitMethod) {
            mParentVisitor.visitLabel(label);
        }
    }
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (mIsInitMethod) {
            mParentVisitor.visitTryCatchBlock(start, end, handler, type);
        }
    }
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (mIsInitMethod) {
            mParentVisitor.visitMethodInsn(opcode, owner, name, desc);
        }
    }
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (mIsInitMethod) {
            mParentVisitor.visitFieldInsn(opcode, owner, name, desc);
        }
    }
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        if (mIsInitMethod) {
            mParentVisitor.visitFrame(type, nLocal, local, nStack, stack);
        }
    }
    public void visitIincInsn(int var, int increment) {
        if (mIsInitMethod) {
            mParentVisitor.visitIincInsn(var, increment);
        }
    }
    public void visitIntInsn(int opcode, int operand) {
        if (mIsInitMethod) {
            mParentVisitor.visitIntInsn(opcode, operand);
        }
    }
    public void visitJumpInsn(int opcode, Label label) {
        if (mIsInitMethod) {
            mParentVisitor.visitJumpInsn(opcode, label);
        }
    }
    public void visitLdcInsn(Object cst) {
        if (mIsInitMethod) {
            mParentVisitor.visitLdcInsn(cst);
        }
    }
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        if (mIsInitMethod) {
            mParentVisitor.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (mIsInitMethod) {
            mParentVisitor.visitLookupSwitchInsn(dflt, keys, labels);
        }
    }
    public void visitMultiANewArrayInsn(String desc, int dims) {
        if (mIsInitMethod) {
            mParentVisitor.visitMultiANewArrayInsn(desc, dims);
        }
    }
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        if (mIsInitMethod) {
            mParentVisitor.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }
    public void visitTypeInsn(int opcode, String type) {
        if (mIsInitMethod) {
            mParentVisitor.visitTypeInsn(opcode, type);
        }
    }
    public void visitVarInsn(int opcode, int var) {
        if (mIsInitMethod) {
            mParentVisitor.visitVarInsn(opcode, var);
        }
    }
}
