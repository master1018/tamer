package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.visitor.AnnotationVisitor;
public abstract class AnnotationsAttribute extends Attribute
{
    public int          u2annotationsCount;
    public Annotation[] annotations;
    protected AnnotationsAttribute()
    {
    }
    protected AnnotationsAttribute(int          u2attributeNameIndex,
                                   int          u2annotationsCount,
                                   Annotation[] annotations)
    {
        super(u2attributeNameIndex);
        this.u2annotationsCount = u2annotationsCount;
        this.annotations        = annotations;
    }
    public void annotationsAccept(Clazz clazz, AnnotationVisitor annotationVisitor)
    {
        for (int index = 0; index < u2annotationsCount; index++)
        {
            annotationVisitor.visitAnnotation(clazz, annotations[index]);
        }
    }
    public void annotationsAccept(Clazz clazz, Field field, AnnotationVisitor annotationVisitor)
    {
        for (int index = 0; index < u2annotationsCount; index++)
        {
            annotationVisitor.visitAnnotation(clazz, field, annotations[index]);
        }
    }
    public void annotationsAccept(Clazz clazz, Method method, AnnotationVisitor annotationVisitor)
    {
        for (int index = 0; index < u2annotationsCount; index++)
        {
            annotationVisitor.visitAnnotation(clazz, method, annotations[index]);
        }
    }
}
