package com.sun.tools.classfile;
import java.io.IOException;
public class RuntimeInvisibleParameterAnnotations_attribute extends RuntimeParameterAnnotations_attribute {
    RuntimeInvisibleParameterAnnotations_attribute(ClassReader cr, int name_index, int length)
            throws IOException, Annotation.InvalidAnnotation {
        super(cr, name_index, length);
    }
    public RuntimeInvisibleParameterAnnotations_attribute(ConstantPool cp, Annotation[][] parameter_annotations)
            throws ConstantPoolException {
        this(cp.getUTF8Index(Attribute.RuntimeInvisibleParameterAnnotations), parameter_annotations);
    }
    public RuntimeInvisibleParameterAnnotations_attribute(int name_index, Annotation[][] parameter_annotations) {
        super(name_index, parameter_annotations);
    }
    public <R, P> R accept(Visitor<R, P> visitor, P p) {
        return visitor.visitRuntimeInvisibleParameterAnnotations(this, p);
    }
}
