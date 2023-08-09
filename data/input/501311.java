public class ClassHasNativeVisitor implements ClassVisitor {
    private boolean mHasNativeMethods = false;
    public boolean hasNativeMethods() {
        return mHasNativeMethods;
    }
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }
    public void visitAttribute(Attribute attr) {
    }
    public void visitEnd() {
    }
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        return null;
    }
    public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
    }
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        mHasNativeMethods |= ((access & Opcodes.ACC_NATIVE) != 0);
        return null;
    }
    public void visitOuterClass(String owner, String name, String desc) {
    }
    public void visitSource(String source, String debug) {
    }
}
