class TransformClassAdapter extends ClassAdapter {
    private final boolean mStubAll;
    private boolean mIsInterface;
    private final String mClassName;
    private final Log mLog;
    private final Set<String> mStubMethods;
    private Set<String> mDeleteReturns;
    public TransformClassAdapter(Log logger, Set<String> stubMethods,
            Set<String> deleteReturns, String className, ClassVisitor cv,
            boolean stubNativesOnly, boolean hasNative) {
        super(cv);
        mLog = logger;
        mStubMethods = stubMethods;
        mClassName = className;
        mStubAll = !stubNativesOnly;
        mIsInterface = false;
        mDeleteReturns = deleteReturns;
    }
    @Override
    public void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces) {
        name = mClassName;
        access = access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
        access |= Opcodes.ACC_PUBLIC;
        access = access & ~Opcodes.ACC_FINAL;
        mIsInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        access = access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED);
        access |= Opcodes.ACC_PUBLIC;
        access = access & ~Opcodes.ACC_FINAL;
        super.visitInnerClass(name, outerName, innerName, access);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if (mDeleteReturns != null) {
            Type t = Type.getReturnType(desc);
            if (t.getSort() == Type.OBJECT) {
                String returnType = t.getInternalName();
                if (returnType != null) {
                    if (mDeleteReturns.contains(returnType)) {
                        return null;
                    }
                }
            }
        }
        String methodSignature = mClassName.replace('/', '.') + "#" + name;
        access &= ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        access |= Opcodes.ACC_PUBLIC;
        access = access & ~Opcodes.ACC_FINAL;
        if (!mIsInterface &&
            ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != Opcodes.ACC_ABSTRACT) &&
            (mStubAll ||
             (access & Opcodes.ACC_NATIVE) != 0) ||
             mStubMethods.contains(methodSignature)) {
            boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
            boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
            access = access & ~(Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL | Opcodes.ACC_NATIVE);
            String invokeSignature = methodSignature + desc;
            mLog.debug("  Stub: %s (%s)", invokeSignature, isNative ? "native" : "");
            MethodVisitor mw = super.visitMethod(access, name, desc, signature, exceptions);
            return new StubMethodAdapter(mw, name, returnType(desc), invokeSignature,
                    isStatic, isNative);
        } else {
            mLog.debug("  Keep: %s %s", name, desc);
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature,
            Object value) {
        access &= ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
        access |= Opcodes.ACC_PUBLIC;
        return super.visitField(access, name, desc, signature, value);
    }
    Type returnType(String desc) {
        if (desc != null) {
            try {
                return Type.getReturnType(desc);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        return null;
    }
}
