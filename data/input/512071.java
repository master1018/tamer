package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.*;
public class StringConstant extends Constant
{
    public int u2stringIndex;
    public Clazz referencedClass;
    public Member referencedMember;
    public Clazz javaLangStringClass;
    public StringConstant()
    {
    }
    public StringConstant(int    u2stringIndex,
                          Clazz  referencedClass,
                          Member referenceMember)
    {
        this.u2stringIndex    = u2stringIndex;
        this.referencedClass  = referencedClass;
        this.referencedMember = referenceMember;
    }
    public String getString(Clazz clazz)
    {
        return clazz.getString(u2stringIndex);
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_String;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitStringConstant(clazz, this);
    }
    public void referencedClassAccept(ClassVisitor classVisitor)
    {
        if (referencedClass  != null &&
            referencedMember == null)
        {
            referencedClass.accept(classVisitor);
        }
    }
    public void referencedMemberAccept(MemberVisitor memberVisitor)
    {
        if (referencedMember != null)
        {
            referencedMember.accept(referencedClass, memberVisitor);
        }
    }
}
