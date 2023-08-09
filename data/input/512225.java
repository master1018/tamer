package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class SyntheticAttribute extends Attribute
{
    public SyntheticAttribute()
    {
    }
    public SyntheticAttribute(int u2attributeNameIndex)
    {
        super(u2attributeNameIndex);
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSyntheticAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSyntheticAttribute(clazz, field, this);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSyntheticAttribute(clazz, method, this);
    }
}
