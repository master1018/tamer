package com.sun.tools.classfile;
import java.io.IOException;
public class CompilationID_attribute extends Attribute {
    CompilationID_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        compilationID_index = cr.readUnsignedShort();
    }
    public CompilationID_attribute(ConstantPool constant_pool, int compilationID_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.CompilationID), compilationID_index);
    }
    public CompilationID_attribute(int name_index, int compilationID_index) {
        super(name_index, 2);
        this.compilationID_index = compilationID_index;
    }
    String getCompilationID(ConstantPool constant_pool)
            throws ConstantPoolException {
        return constant_pool.getUTF8Value(compilationID_index);
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitCompilationID(this, data);
    }
    public final int compilationID_index;
}
