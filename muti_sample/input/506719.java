package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.visitor.*;
public abstract class RefConstant extends Constant
{
    public int u2classIndex;
    public int u2nameAndTypeIndex;
    public Clazz referencedClass;
    public Member referencedMember;
    protected RefConstant()
    {
    }
    public int getClassIndex()
    {
        return u2classIndex;
    }
    public int getNameAndTypeIndex()
    {
        return u2nameAndTypeIndex;
    }
    public void setNameAndTypeIndex(int index)
    {
        u2nameAndTypeIndex = index;
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
    public void referencedMemberAccept(MemberVisitor memberVisitor)
    {
        if (referencedMember != null)
        {
            referencedMember.accept(referencedClass,
                                    memberVisitor);
        }
    }
}
