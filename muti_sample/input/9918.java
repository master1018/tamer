package com.sun.tools.classfile;
import java.io.IOException;
public class SourceID_attribute extends Attribute {
    SourceID_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        sourceID_index = cr.readUnsignedShort();
    }
    public SourceID_attribute(ConstantPool constant_pool, int sourceID_index)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.SourceID), sourceID_index);
    }
    public SourceID_attribute(int name_index, int sourceID_index) {
        super(name_index, 2);
        this.sourceID_index = sourceID_index;
    }
    String getSourceID(ConstantPool constant_pool) throws ConstantPoolException {
        return constant_pool.getUTF8Value(sourceID_index);
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitSourceID(this, data);
    }
    public final int sourceID_index;
}
