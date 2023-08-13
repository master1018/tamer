public class ClassStubber extends ClassAdapter {
    public ClassStubber(ClassVisitor cv) {
        super(cv);
    }
    @Override
    public void visit(int version, int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }
    @Override
    public void visitEnd() {
        super.visitEnd();
    }
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }
    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions) {
        MethodVisitor mw = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodStubber(mw, access, name, desc, signature, exceptions);
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature,
            Object value) {
        return super.visitField(access, name, desc, signature, value);
    }
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }
    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }
}
