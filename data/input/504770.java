package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.visitor.MemberVisitor;
public abstract class ElementValue implements VisitorAccepter
{
    public int u2elementNameIndex;
    public Clazz referencedClass;
    public Method referencedMethod;
    public Object visitorInfo;
    protected ElementValue()
    {
    }
    protected ElementValue(int u2elementNameIndex)
    {
        this.u2elementNameIndex = u2elementNameIndex;
    }
    public String getMethodName(Clazz clazz)
    {
        return clazz.getString(u2elementNameIndex);
    }
    public abstract int getTag();
    public abstract void accept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor);
    public void referencedMethodAccept(MemberVisitor memberVisitor)
    {
        if (referencedMethod != null)
        {
            referencedMethod.accept(referencedClass, memberVisitor);
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
