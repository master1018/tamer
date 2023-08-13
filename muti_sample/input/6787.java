package com.sun.tools.classfile;
import java.io.IOException;
public class RuntimeVisibleAnnotations_attribute extends RuntimeAnnotations_attribute {
    RuntimeVisibleAnnotations_attribute(ClassReader cr, int name_index, int length)
            throws IOException, Annotation.InvalidAnnotation {
        super(cr, name_index, length);
    }
    public RuntimeVisibleAnnotations_attribute(ConstantPool cp, Annotation[] annotations)
            throws ConstantPoolException {
        this(cp.getUTF8Index(Attribute.RuntimeVisibleAnnotations), annotations);
    }
    public RuntimeVisibleAnnotations_attribute(int name_index, Annotation[] annotations) {
        super(name_index, annotations);
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitRuntimeVisibleAnnotations(this, p);
    }
}
