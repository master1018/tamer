package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class AnnotationDefaultAttribute extends Attribute
{
    public ElementValue defaultValue;
    public AnnotationDefaultAttribute()
    {
    }
    public AnnotationDefaultAttribute(int          u2attributeNameIndex,
                                      ElementValue defaultValue)
    {
        super(u2attributeNameIndex);
        this.defaultValue = defaultValue;
    }
    public void defaultValueAccept(Clazz clazz, ElementValueVisitor elementValueVisitor)
    {
        defaultValue.accept(clazz, null, elementValueVisitor);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitAnnotationDefaultAttribute(clazz, method, this);
    }
}
