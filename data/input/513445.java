package proguard.classfile.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class ClassForNameClassVisitor
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final ClassVisitor classVisitor;
    public ClassForNameClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.referencedClassAccept(classVisitor);
    }
}
