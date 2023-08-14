package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class ClassConstant extends Constant
{
    public int u2nameIndex;
    public Clazz referencedClass;
    public Clazz javaLangClassClass;
    public ClassConstant()
    {
    }
    public ClassConstant(int   u2nameIndex,
                         Clazz referencedClass)
    {
        this.u2nameIndex     = u2nameIndex;
        this.referencedClass = referencedClass;
    }
    public String getName(Clazz clazz)
    {
        return clazz.getString(u2nameIndex);
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Class;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitClassConstant(clazz, this);
    }
    public void referencedClassAccept(ClassVisitor classVisitor)
    {
        if (referencedClass != null)
        {
            referencedClass.accept(classVisitor);
        }
    }
}
