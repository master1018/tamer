package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public abstract class Attribute implements VisitorAccepter
{
    public int u2attributeNameIndex;
    public Object visitorInfo;
    protected Attribute()
    {
    }
    protected Attribute(int u2attributeNameIndex)
    {
        this.u2attributeNameIndex = u2attributeNameIndex;
    }
    public String getAttributeName(Clazz clazz)
    {
        return clazz.getString(u2attributeNameIndex);
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void accept(Clazz clazz, Field field, AttributeVisitor attributeVisitor)
    {
        if (field == null)
        {
            accept(clazz, attributeVisitor);
        }
        else
        {
            throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
        }
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        if (method == null)
        {
            accept(clazz, (Field)null, attributeVisitor);
        }
        else
        {
            throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
        }
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, AttributeVisitor attributeVisitor)
    {
        if (codeAttribute == null)
        {
            accept(clazz, method, attributeVisitor);
        }
        else
        {
            throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
        }
    }
    public Object getVisitorInfo()
    {
        return visitorInfo;
    }
    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
