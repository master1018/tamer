package com.sun.tools.classfile;
import java.io.IOException;
public class RuntimeVisibleParameterAnnotations_attribute extends RuntimeParameterAnnotations_attribute {
    RuntimeVisibleParameterAnnotations_attribute(ClassReader cr, int name_index, int length)
            throws IOException, Annotation.InvalidAnnotation {
        super(cr, name_index, length);
    }
    public RuntimeVisibleParameterAnnotations_attribute(ConstantPool cp, Annotation[][] parameter_annotations)
            throws ConstantPoolException {
        this(cp.getUTF8Index(Attribute.RuntimeVisibleParameterAnnotations), parameter_annotations);
    }
    public RuntimeVisibleParameterAnnotations_attribute(int name_index, Annotation[][] parameter_annotations) {
        super(name_index, parameter_annotations);
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitRuntimeVisibleParameterAnnotations(this, p);
    }
}
