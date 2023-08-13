package com.sun.tools.classfile;
public class DefaultAttribute extends Attribute {
    DefaultAttribute(ClassReader cr, int name_index, byte[] data) {
        super(name_index, data.length);
        info = data;
    }
    public DefaultAttribute(ConstantPool constant_pool, int name_index, byte[] info) {
        super(name_index, info.length);
        this.info = info;
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitDefault(this, p);
    }
    public final byte[] info;
}
