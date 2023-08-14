package com.sun.tools.classfile;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
public class SourceDebugExtension_attribute extends Attribute {
    SourceDebugExtension_attribute(ClassReader cr, int name_index, int length) throws IOException {
        super(name_index, length);
        debug_extension = new byte[attribute_length];
        cr.readFully(debug_extension);
    }
    public SourceDebugExtension_attribute(ConstantPool constant_pool, byte[] debug_extension)
            throws ConstantPoolException {
        this(constant_pool.getUTF8Index(Attribute.SourceDebugExtension), debug_extension);
    }
    public SourceDebugExtension_attribute(int name_index, byte[] debug_extension) {
        super(name_index, debug_extension.length);
        this.debug_extension = debug_extension;
    }
    public String getValue() {
        DataInputStream d  = new DataInputStream(new ByteArrayInputStream(debug_extension));
        try {
            return d.readUTF();
        } catch (IOException e) {
            return null;
        }
    }
    public <R, D> R accept(Visitor<R, D> visitor, D data) {
        return visitor.visitSourceDebugExtension(this, data);
    }
    public final byte[] debug_extension;
}
