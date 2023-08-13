package com.sun.tools.classfile;
import java.io.IOException;
public class RuntimeInvisibleAnnotations_attribute extends RuntimeAnnotations_attribute {
    RuntimeInvisibleAnnotations_attribute(ClassReader cr, int name_index, int length)
            throws IOException, AttributeException {
        super(cr, name_index, length);
    }
    public RuntimeInvisibleAnnotations_attribute(ConstantPool cp, Annotation[] annotations)
            throws ConstantPoolException {
        this(cp.getUTF8Index(Attribute.RuntimeInvisibleAnnotations), annotations);
    }
    public RuntimeInvisibleAnnotations_attribute(int name_index, Annotation[] annotations) {
        super(name_index, annotations);
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitRuntimeInvisibleAnnotations(this, p);
    }
}
