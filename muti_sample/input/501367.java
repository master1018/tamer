package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
public class ClassPoolFiller
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final ClassPool classPool;
    public ClassPoolFiller(ClassPool classPool)
    {
        this.classPool = classPool;
    }
    public void visitAnyClass(Clazz clazz)
    {
        classPool.addClass(clazz);
    }
}
