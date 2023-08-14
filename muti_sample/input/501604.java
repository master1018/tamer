import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.optimize.info.ExceptionInstructionChecker;
public class UnreachableExceptionRemover
extends      SimplifiedVisitor
implements   AttributeVisitor,
             ExceptionInfoVisitor
{
    private final ExceptionInfoVisitor extraExceptionInfoVisitor;
    private final ExceptionInstructionChecker exceptionInstructionChecker = new ExceptionInstructionChecker();
    public UnreachableExceptionRemover()
    {
        this(null);
    }
    public UnreachableExceptionRemover(ExceptionInfoVisitor extraExceptionInfoVisitor)
    {
        this.extraExceptionInfoVisitor = extraExceptionInfoVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.exceptionsAccept(clazz, method, this);
        codeAttribute.u2exceptionTableLength =
            removeEmptyExceptions(codeAttribute.exceptionTable,
                                  codeAttribute.u2exceptionTableLength);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        if (!mayThrowExceptions(clazz,
                                method,
                                codeAttribute,
                                exceptionInfo.u2startPC,
                                exceptionInfo.u2endPC))
        {
            exceptionInfo.u2endPC = exceptionInfo.u2startPC;
            if (extraExceptionInfoVisitor != null)
            {
                extraExceptionInfoVisitor.visitExceptionInfo(clazz, method, codeAttribute, exceptionInfo);
            }
        }
    }
    private boolean mayThrowExceptions(Clazz         clazz,
                                       Method        method,
                                       CodeAttribute codeAttribute,
                                       int           startOffset,
                                       int           endOffset)
    {
        byte[] code = codeAttribute.code;
        int offset = startOffset;
        while (offset < endOffset)
        {
            Instruction instruction = InstructionFactory.create(code, offset);
            if (exceptionInstructionChecker.mayThrowExceptions(clazz,
                                                               method,
                                                               codeAttribute,
                                                               offset,
                                                               instruction))
            {
                return true;
            }
            offset += instruction.length(offset);
        }
        return false;
    }
    private int removeEmptyExceptions(ExceptionInfo[] exceptionInfos,
                                      int             exceptionInfoCount)
    {
        int newIndex = 0;
        for (int index = 0; index < exceptionInfoCount; index++)
        {
            ExceptionInfo exceptionInfo = exceptionInfos[index];
            if (exceptionInfo.u2startPC < exceptionInfo.u2endPC)
            {
                exceptionInfos[newIndex++] = exceptionInfo;
            }
        }
        return newIndex;
    }
}
