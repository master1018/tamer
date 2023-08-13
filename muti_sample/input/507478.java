package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class InterfaceMethodrefConstant extends RefConstant
{
    public InterfaceMethodrefConstant()
    {
    }
    public InterfaceMethodrefConstant(int    u2classIndex,
                                      int    u2nameAndTypeIndex,
                                      Clazz  referencedClass,
                                      Member referencedMember)
    {
        this.u2classIndex       = u2classIndex;
        this.u2nameAndTypeIndex = u2nameAndTypeIndex;
        this.referencedClass    = referencedClass;
        this.referencedMember   = referencedMember;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_InterfaceMethodref;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitInterfaceMethodrefConstant(clazz, this);
    }
}
