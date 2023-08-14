package com.sun.tools.classfile;
import java.io.IOException;
public class Deprecated_attribute extends Attribute {
    Deprecated_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
    }
    public Deprecated_attribute(ConstantPool constant_pool)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.Deprecated));
    }
    public Deprecated_attribute(int name_index) {
        super(name_index, 0);
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitDeprecated(this, data);
    }
}
