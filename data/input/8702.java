package com.sun.tools.classfile;
import java.io.IOException;
public class Synthetic_attribute extends Attribute {
    Synthetic_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
    }
    public Synthetic_attribute(ConstantPool constant_pool)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.Synthetic));
    }
    public Synthetic_attribute(int name_index) {
        super(name_index, 0);
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitSynthetic(this, data);
    }
}
