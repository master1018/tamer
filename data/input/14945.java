package com.sun.tools.classfile;
import java.io.IOException;
public abstract class RuntimeAnnotations_attribute extends Attribute {
    protected RuntimeAnnotations_attribute(ClassReader cr, int name_index, int length)
            throws IOException, Annotation.InvalidAnnotation {
        super(name_index, length);
        int num_annotations = cr.readUnsignedShort();
        annotations = new Annotation[num_annotations];
        for (int i = 0; i < annotations.length; i++)
            annotations[i] = new Annotation(cr);
    }
    protected RuntimeAnnotations_attribute(int name_index, Annotation[] annotations) {
        super(name_index, length(annotations));
        this.annotations = annotations;
    }
    private static int length(Annotation[] annos) {
        int n = 2;
        for (Annotation anno: annos)
            n += anno.length();
        return n;
    }
    public final Annotation[] annotations;
}
