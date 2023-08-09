class FilterClassAdapter extends ClassAdapter {
    private final Filter mFilter;
    private String mClassName;
    public FilterClassAdapter(ClassVisitor writer, Filter filter) {
        super(writer);
        mFilter = filter;
    }
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    @Override
    public void visitEnd() {
        super.visitEnd();
    }
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
            return null;
        }
        String filterName = String.format("%s#%s", mClassName, name);
        if (!mFilter.accept(filterName)) {
            System.out.println("- Remove field " + filterName);
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
            return null;
        }
        String filterName = String.format("%s#%s%s", mClassName, name, desc);
        if (!mFilter.accept(filterName)) {
            System.out.println("- Remove method " + filterName);
            return null;
        }
        if (signature != null) {
            filterName = String.format("%s#%s%s", mClassName, name, signature);
            if (!mFilter.accept(filterName)) {
                System.out.println("- Remove method " + filterName);
                return null;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }
    @Override
    public void visitAttribute(Attribute attr) {
    }
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if ((access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0) {
            return;
        }
        if (!mFilter.accept(name)) {
            return;
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }
    @Override
    public void visitSource(String source, String debug) {
    }
}
