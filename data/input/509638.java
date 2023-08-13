package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class StringReferenceInitializer
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final ClassPool programClassPool;
    private final ClassPool libraryClassPool;
    public StringReferenceInitializer(ClassPool programClassPool,
                                      ClassPool libraryClassPool)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        if (stringConstant.referencedClass == null)
        {
            stringConstant.referencedClass =
                findClass(ClassUtil.internalClassName(stringConstant.getString(clazz)));
        }
    }
    private Clazz findClass(String name)
    {
        Clazz clazz = programClassPool.getClass(name);
        if (clazz == null)
        {
            clazz = libraryClassPool.getClass(name);
        }
        return clazz;
    }
}