public class MethodStubber extends MethodAdapter {
    public MethodStubber(MethodVisitor mw,
            int access, String name, String desc, String signature, String[] exceptions) {
        super(mw);
    }
    @Override
    public void visitCode() {
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(36, l0);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("stub");
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,          
                "java/lang/RuntimeException",   
                "<init>",                       
                "(Ljava/lang/String;)V");       
        mv.visitInsn(Opcodes.ATHROW);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable(
                "this",                                         
                "Lcom/android/mkstubs/stubber/MethodStubber;",  
                null,                                           
                l0,                                             
                l1,                                             
                0);                                             
        mv.visitMaxs(3, 1); 
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
    public AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault();
    }
    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }
    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return super.visitParameterAnnotation(parameter, desc, visible);
    }
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    }
    @Override
    public void visitFrame(int type, int local, Object[] local2, int stack, Object[] stack2) {
    }
    @Override
    public void visitIincInsn(int var, int increment) {
    }
    @Override
    public void visitInsn(int opcode) {
    }
    @Override
    public void visitIntInsn(int opcode, int operand) {
    }
    @Override
    public void visitJumpInsn(int opcode, Label label) {
    }
    @Override
    public void visitLabel(Label label) {
    }
    @Override
    public void visitLdcInsn(Object cst) {
    }
    @Override
    public void visitLineNumber(int line, Label start) {
    }
    @Override
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
    }
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
    }
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
    }
    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
    }
    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }
    @Override
    public void visitTypeInsn(int opcode, String type) {
    }
    @Override
    public void visitVarInsn(int opcode, int var) {
    }
}
