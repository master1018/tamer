package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.util.SimplifiedVisitor;
public class ImplementedClassConstantFilter
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final Clazz           implementedClass;
    private final ConstantVisitor constantVisitor;
    public ImplementedClassConstantFilter(Clazz           implementedClass,
                                          ConstantVisitor constantVisitor)
    {
        this.implementedClass = implementedClass;
        this.constantVisitor  = constantVisitor;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        Clazz referencedClass = classConstant.referencedClass;
        if (referencedClass == null ||
            !referencedClass.extendsOrImplements(implementedClass))
        {
            constantVisitor.visitClassConstant(clazz, classConstant);
        }
    }
}