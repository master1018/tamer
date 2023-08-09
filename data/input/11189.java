package com.sun.tools.classfile;
import java.io.IOException;
public class SourceFile_attribute extends Attribute {
    SourceFile_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        sourcefile_index = cr.readUnsignedShort();
    }
    public SourceFile_attribute(ConstantPool constant_pool, int sourcefile_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.SourceFile), sourcefile_index);
    }
    public SourceFile_attribute(int name_index, int sourcefile_index) {
        super(name_index, 2);
        this.sourcefile_index = sourcefile_index;
    }
    public String getSourceFile(ConstantPool constant_pool) throws ConstantPoolException {
        return constant_pool.getUTF8Value(sourcefile_index);
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitSourceFile(this, p);
    }
    public final int sourcefile_index;
}
