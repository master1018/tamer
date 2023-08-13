package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.visitor.*;
public class EnclosingMethodAttribute extends Attribute
{
    public int u2classIndex;
    public int u2nameAndTypeIndex;
    public Clazz referencedClass;
    public Method referencedMethod;
    public EnclosingMethodAttribute()
    {
    }
    public EnclosingMethodAttribute(int u2attributeNameIndex,
                                    int u2classIndex,
                                    int u2nameAndTypeIndex)
    {
        super(u2attributeNameIndex);
        this.u2classIndex       = u2classIndex;
        this.u2nameAndTypeIndex = u2nameAndTypeIndex;
    }
    public String getClassName(Clazz clazz)
    {
        return clazz.getClassName(u2classIndex);
    }
    public String getName(Clazz clazz)
    {
        return clazz.getName(u2nameAndTypeIndex);
    }
    public String getType(Clazz clazz)
    {
        return clazz.getType(u2nameAndTypeIndex);
    }
    public void referencedClassAccept(ClassVisitor classVisitor)
    {
        if (referencedClass != null)
        {
            referencedClass.accept(classVisitor);
        }
    }
    public void referencedMethodAccept(MemberVisitor memberVisitor)
    {
        if (referencedMethod != null)
        {
            referencedMethod.accept(referencedClass,
                                    memberVisitor);
        }
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitEnclosingMethodAttribute(clazz, this);
    }
}
