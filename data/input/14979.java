package com.sun.tools.classfile;
import java.io.IOException;
public class ConstantValue_attribute extends Attribute {
    ConstantValue_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        constantvalue_index = cr.readUnsignedShort();
    }
    public ConstantValue_attribute(ConstantPool constant_pool, int constantvalue_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.ConstantValue), constantvalue_index);
    }
    public ConstantValue_attribute(int name_index, int constantvalue_index) {
        super(name_index, 2);
        this.constantvalue_index = constantvalue_index;
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitConstantValue(this, data);
    }
    public final int constantvalue_index;
}
