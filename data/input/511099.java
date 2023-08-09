package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class UnknownAttribute extends Attribute
{
    public final int    u4attributeLength;
    public byte[] info;
    public UnknownAttribute(int attributeLength)
    {
        u4attributeLength = attributeLength;
    }
    public UnknownAttribute(int    u2attributeNameIndex,
                            int    u4attributeLength,
                            byte[] info)
    {
        super(u2attributeNameIndex);
        this.u4attributeLength = u4attributeLength;
        this.info              = info;
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitUnknownAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitUnknownAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitUnknownAttribute(clazz, this);
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitUnknownAttribute(clazz, this);
    }
}
