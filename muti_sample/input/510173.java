package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.util.SimplifiedVisitor;
public class ImplementingClassConstantFilter
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final Clazz           implementingClass;
    private final ConstantVisitor constantVisitor;
    public ImplementingClassConstantFilter(Clazz           implementingClass,
                                           ConstantVisitor constantVisitor)
    {
        this.implementingClass = implementingClass;
        this.constantVisitor   = constantVisitor;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        Clazz referencedClass = classConstant.referencedClass;
        if (referencedClass == null ||
            !implementingClass.extendsOrImplements(referencedClass))
        {
            constantVisitor.visitClassConstant(clazz, classConstant);
        }
    }
}