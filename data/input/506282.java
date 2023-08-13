package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.util.SimplifiedVisitor;
public class AllExceptionInfoVisitor
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final ExceptionInfoVisitor exceptionInfoVisitor;
    public AllExceptionInfoVisitor(ExceptionInfoVisitor exceptionInfoVisitor)
    {
        this.exceptionInfoVisitor = exceptionInfoVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.exceptionsAccept(clazz, method, exceptionInfoVisitor);
    }
}
