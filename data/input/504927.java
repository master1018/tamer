package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
public class ExceptionHandlerConstantVisitor
implements   ExceptionInfoVisitor
{
    private final ConstantVisitor constantVisitor;
    public ExceptionHandlerConstantVisitor(ConstantVisitor constantVisitor)
    {
        this.constantVisitor = constantVisitor;
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        int catchType = exceptionInfo.u2catchType;
        if (catchType != 0)
        {
            clazz.constantPoolEntryAccept(catchType, constantVisitor);
        }
    }
}