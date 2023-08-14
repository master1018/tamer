package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class DeprecatedAttribute extends Attribute
{
    public DeprecatedAttribute()
    {
    }
    public DeprecatedAttribute(int u2attributeNameIndex)
    {
        super(u2attributeNameIndex);
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitDeprecatedAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitDeprecatedAttribute(clazz, field, this);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitDeprecatedAttribute(clazz, method, this);
    }
}
