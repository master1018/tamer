package com.sun.tools.classfile;
import java.io.IOException;
public class EnclosingMethod_attribute extends Attribute {
    EnclosingMethod_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        class_index = cr.readUnsignedShort();
        method_index = cr.readUnsignedShort();
    }
    public EnclosingMethod_attribute(ConstantPool constant_pool, int class_index, int method_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.EnclosingMethod), class_index, method_index);
    }
    public EnclosingMethod_attribute(int name_index, int class_index, int method_index) {
        super(name_index, 4);
        this.class_index = class_index;
        this.method_index = method_index;
    }
    public String getClassName(ConstantPool constant_pool) throws ConstantPoolException {
        return constant_pool.getClassInfo(class_index).getName();
    }
    public String getMethodName(ConstantPool constant_pool) throws ConstantPoolException {
        if (method_index == 0)
            return "";
        return constant_pool.getNameAndTypeInfo(method_index).getName();
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitEnclosingMethod(this, data);
    }
    public final int class_index;
    public final int method_index;
}
