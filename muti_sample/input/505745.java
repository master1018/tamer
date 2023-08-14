public class RenameClassAdapter extends ClassAdapter {
    private final String mOldName;
    private final String mNewName;
    private String mOldBase;
    private String mNewBase;
    public RenameClassAdapter(ClassWriter cv, String oldName, String newName) {
        super(cv);
        mOldBase = mOldName = oldName;
        mNewBase = mNewName = newName;
        int pos = mOldName.indexOf('$');
        if (pos > 0) {
            mOldBase = mOldName.substring(0, pos);
        }
        pos = mNewName.indexOf('$');
        if (pos > 0) {
            mNewBase = mNewName.substring(0, pos);
        }
        assert (mOldBase == null && mNewBase == null) || (mOldBase != null && mNewBase != null);
    }
    String renameTypeDesc(String desc) {
        if (desc == null) {
            return null;
        }
        return renameType(Type.getType(desc));
    }
    String renameType(Type type) {
        if (type == null) {
            return null;
        }
        if (type.getSort() == Type.OBJECT) {
            String in = type.getInternalName();
            return "L" + renameInternalType(in) + ";";
        } else if (type.getSort() == Type.ARRAY) {
            StringBuilder sb = new StringBuilder();
            for (int n = type.getDimensions(); n > 0; n--) {
                sb.append('[');
            }
            sb.append(renameType(type.getElementType()));
            return sb.toString();
        }
        return type.getDescriptor();
    }
    Type renameTypeAsType(Type type) {
        if (type == null) {
            return null;
        }
        if (type.getSort() == Type.OBJECT) {
            String in = type.getInternalName();
            String newIn = renameInternalType(in);
            if (newIn != in) {
                return Type.getType("L" + newIn + ";");
            }
        } else if (type.getSort() == Type.ARRAY) {
            StringBuilder sb = new StringBuilder();
            for (int n = type.getDimensions(); n > 0; n--) {
                sb.append('[');
            }
            sb.append(renameType(type.getElementType()));
            return Type.getType(sb.toString());
        }
        return type;
    }
    String renameInternalType(String type) {
        if (type == null) {
            return null;
        }
        if (type.equals(mOldName)) {
            return mNewName;
        }
        if (mOldBase != mOldName && type.equals(mOldBase)) {
            return mNewBase;
        }
        int pos = type.indexOf('$');
        if (pos == mOldBase.length() && type.startsWith(mOldBase)) {
            return mNewBase + type.substring(pos);
        }
        if (type.indexOf(';') > 0) {
            type = renameTypeDesc(type);
        }
        return type;
    }
    String renameMethodDesc(String desc) {
        if (desc == null) {
            return null;
        }
        Type[] args = Type.getArgumentTypes(desc);
        StringBuilder sb = new StringBuilder("(");
        for (Type arg : args) {
            String name = renameType(arg);
            sb.append(name);
        }
        sb.append(')');
        Type ret = Type.getReturnType(desc);
        String name = renameType(ret);
        sb.append(name);
        return sb.toString();
    }
    String renameTypeSignature(String sig) {
        if (sig == null) {
            return null;
        }
        SignatureReader reader = new SignatureReader(sig);
        SignatureWriter writer = new SignatureWriter();
        reader.accept(new RenameSignatureAdapter(writer));
        sig = writer.toString();
        return sig;
    }
    String renameFieldSignature(String sig) {
        if (sig == null) {
            return null;
        }
        SignatureReader reader = new SignatureReader(sig);
        SignatureWriter writer = new SignatureWriter();
        reader.acceptType(new RenameSignatureAdapter(writer));
        sig = writer.toString();
        return sig;
    }
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        name = renameInternalType(name);
        superName = renameInternalType(superName);
        signature = renameTypeSignature(signature);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        assert outerName.equals(mOldName);
        outerName = renameInternalType(outerName);
        name = outerName + "$" + innerName;
        super.visitInnerClass(name, outerName, innerName, access);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        desc = renameMethodDesc(desc);
        signature = renameTypeSignature(signature);
        MethodVisitor mw = super.visitMethod(access, name, desc, signature, exceptions);
        return new RenameMethodAdapter(mw);
    }
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        desc = renameTypeDesc(desc);
        return super.visitAnnotation(desc, visible);
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        desc = renameTypeDesc(desc);
        signature = renameFieldSignature(signature);
        return super.visitField(access, name, desc, signature, value);
    }
    public class RenameMethodAdapter extends MethodAdapter {
        public RenameMethodAdapter(MethodVisitor mv) {
            super(mv);
        }
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            desc = renameTypeDesc(desc);
            return super.visitAnnotation(desc, visible);
        }
        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            desc = renameTypeDesc(desc);
            return super.visitParameterAnnotation(parameter, desc, visible);
        }
        @Override
        public void visitTypeInsn(int opcode, String type) {
            type = renameInternalType(type);
            super.visitTypeInsn(opcode, type);
        }
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            owner = renameInternalType(owner);
            desc = renameTypeDesc(desc);
            super.visitFieldInsn(opcode, owner, name, desc);
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            owner = renameInternalType(owner);
            desc = renameMethodDesc(desc);
            super.visitMethodInsn(opcode, owner, name, desc);
        }
        @Override
        public void visitLdcInsn(Object cst) {
            if (cst instanceof Type) {
                cst = renameTypeAsType((Type) cst);
            }
            super.visitLdcInsn(cst);
        }
        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            desc = renameTypeDesc(desc);
            super.visitMultiANewArrayInsn(desc, dims);
        }
        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            type = renameInternalType(type);
            super.visitTryCatchBlock(start, end, handler, type);
        }
        @Override
        public void visitLocalVariable(String name, String desc, String signature,
                Label start, Label end, int index) {
            desc = renameTypeDesc(desc);
            signature = renameFieldSignature(signature);
            super.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }
    public class RenameSignatureAdapter implements SignatureVisitor {
        private final SignatureVisitor mSv;
        public RenameSignatureAdapter(SignatureVisitor sv) {
            mSv = sv;
        }
        public void visitClassType(String name) {
            name = renameInternalType(name);
            mSv.visitClassType(name);
        }
        public void visitInnerClassType(String name) {
            name = renameInternalType(name);
            mSv.visitInnerClassType(name);
        }
        public SignatureVisitor visitArrayType() {
            SignatureVisitor sv = mSv.visitArrayType();
            return new RenameSignatureAdapter(sv);
        }
        public void visitBaseType(char descriptor) {
            mSv.visitBaseType(descriptor);
        }
        public SignatureVisitor visitClassBound() {
            SignatureVisitor sv = mSv.visitClassBound();
            return new RenameSignatureAdapter(sv);
        }
        public void visitEnd() {
            mSv.visitEnd();
        }
        public SignatureVisitor visitExceptionType() {
            SignatureVisitor sv = mSv.visitExceptionType();
            return new RenameSignatureAdapter(sv);
        }
        public void visitFormalTypeParameter(String name) {
            mSv.visitFormalTypeParameter(name);
        }
        public SignatureVisitor visitInterface() {
            SignatureVisitor sv = mSv.visitInterface();
            return new RenameSignatureAdapter(sv);
        }
        public SignatureVisitor visitInterfaceBound() {
            SignatureVisitor sv = mSv.visitInterfaceBound();
            return new RenameSignatureAdapter(sv);
        }
        public SignatureVisitor visitParameterType() {
            SignatureVisitor sv = mSv.visitParameterType();
            return new RenameSignatureAdapter(sv);
        }
        public SignatureVisitor visitReturnType() {
            SignatureVisitor sv = mSv.visitReturnType();
            return new RenameSignatureAdapter(sv);
        }
        public SignatureVisitor visitSuperclass() {
            SignatureVisitor sv = mSv.visitSuperclass();
            return new RenameSignatureAdapter(sv);
        }
        public void visitTypeArgument() {
            mSv.visitTypeArgument();
        }
        public SignatureVisitor visitTypeArgument(char wildcard) {
            SignatureVisitor sv = mSv.visitTypeArgument(wildcard);
            return new RenameSignatureAdapter(sv);
        }
        public void visitTypeVariable(String name) {
            mSv.visitTypeVariable(name);
        }
    }
}
