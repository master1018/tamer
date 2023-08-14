public class ClassSourcer implements ClassVisitor {
    private final Output mOutput;
    private final AccessSourcer mAccessSourcer;
    private String mClassName;
    public ClassSourcer(Output output) {
        mOutput = output;
        mAccessSourcer = new AccessSourcer(mOutput);
    }
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        String pkg = name.substring(0, name.lastIndexOf('/')).replace('/', '.');
        mClassName = name.substring(name.lastIndexOf('/') + 1);
        mOutput.write("package %s;\n", pkg);
        mAccessSourcer.write(access & ~Opcodes.ACC_SUPER, AccessSourcer.IS_CLASS);
        mOutput.write(" class %s", mClassName);
        if (signature != null) {
            SignatureReader sigReader = new SignatureReader(signature);
            SignatureSourcer sigSourcer = new SignatureSourcer();
            sigReader.accept(sigSourcer);
            if (sigSourcer.hasFormalsContent()) {
                mOutput.write(sigSourcer.formalsToString());
            }
            mOutput.write(" extends %s", sigSourcer.getSuperClass().toString());
        } else {
            mOutput.write(" extends %s", superName.replace('/', '.'));
        }
        if (interfaces != null && interfaces.length > 0) {
            mOutput.write(" implements ");
            boolean need_sep = false;
            for (String i : interfaces) {
                if (need_sep) {
                    mOutput.write(", ");
                }
                mOutput.write(i.replace('/', '.'));
                need_sep = true;
            }
        }
        mOutput.write(" {\n");
    }
    public void visitEnd() {
        mOutput.write("}\n");
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        mOutput.write("@%s", desc);
        return new AnnotationSourcer(mOutput);
    }
    public void visitAttribute(Attribute attr) {
        mOutput.write("%s  ", attr.type);
    }
    public FieldVisitor visitField(int access, String name, String desc, String signature,
            Object value) {
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
            return null;
        }
        return new FieldSourcer(mOutput, access, name, desc, signature);
    }
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions) {
        return new MethodSourcer(mOutput, mClassName, access, name, desc, signature, exceptions);
    }
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }
    public void visitOuterClass(String owner, String name, String desc) {
    }
    public void visitSource(String source, String debug) {
    }
}
