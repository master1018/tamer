package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class RuntimeVisibleAnnotationsAttribute extends AnnotationsAttribute
{
    public RuntimeVisibleAnnotationsAttribute()
    {
    }
    public RuntimeVisibleAnnotationsAttribute(int          u2attributeNameIndex,
                                              int          u2annotationsCount,
                                              Annotation[] annotations)
    {
        super(u2attributeNameIndex, u2annotationsCount, annotations);
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitRuntimeVisibleAnnotationsAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitRuntimeVisibleAnnotationsAttribute(clazz, field, this);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitRuntimeVisibleAnnotationsAttribute(clazz, method, this);
    }
}
