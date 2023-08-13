package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.ExceptionInfoVisitor;
public class ExceptionRangeFilter
implements   ExceptionInfoVisitor
{
    private final int                  startOffset;
    private final int                  endOffset;
    private final ExceptionInfoVisitor exceptionInfoVisitor;
    public ExceptionRangeFilter(int                  startOffset,
                                int                  endOffset,
                                ExceptionInfoVisitor exceptionInfoVisitor)
    {
        this.startOffset          = startOffset;
        this.endOffset            = endOffset;
        this.exceptionInfoVisitor = exceptionInfoVisitor;
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        if (exceptionInfo.isApplicable(startOffset, endOffset))
        {
            exceptionInfoVisitor.visitExceptionInfo(clazz, method, codeAttribute, exceptionInfo);
        }
    }
}
