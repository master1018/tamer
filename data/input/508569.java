package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class CodeAttribute extends Attribute
{
    public int             u2maxStack;
    public int             u2maxLocals;
    public int             u4codeLength;
    public byte[]          code;
    public int             u2exceptionTableLength;
    public ExceptionInfo[] exceptionTable;
    public int             u2attributesCount;
    public Attribute[]     attributes;
    public CodeAttribute()
    {
    }
    public CodeAttribute(int             u2attributeNameIndex,
                         int             u2maxStack,
                         int             u2maxLocals,
                         int             u4codeLength,
                         byte[]          code,
                         int             u2exceptionTableLength,
                         ExceptionInfo[] exceptionTable,
                         int             u2attributesCount,
                         Attribute[]     attributes)
    {
        super(u2attributeNameIndex);
        this.u2maxStack             = u2maxStack;
        this.u2maxLocals            = u2maxLocals;
        this.u4codeLength           = u4codeLength;
        this.code                   = code;
        this.u2exceptionTableLength = u2exceptionTableLength;
        this.exceptionTable         = exceptionTable;
        this.u2attributesCount      = u2attributesCount;
        this.attributes             = attributes;
    }
    public Attribute getAttribute(Clazz clazz, String name)
    {
        for (int index = 0; index < u2attributesCount; index++)
        {
            Attribute attribute = attributes[index];
            if (attribute.getAttributeName(clazz).equals(name))
            {
                return attribute;
            }
        }
        return null;
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitCodeAttribute(clazz, method, this);
    }
    public void instructionsAccept(Clazz clazz, Method method, InstructionVisitor instructionVisitor)
    {
        instructionsAccept(clazz, method, 0, u4codeLength, instructionVisitor);
    }
    public void instructionAccept(Clazz clazz, Method method, int offset, InstructionVisitor instructionVisitor)
    {
        Instruction instruction = InstructionFactory.create(code, offset);
        instruction.accept(clazz, method, this, offset, instructionVisitor);
    }
    public void instructionsAccept(Clazz clazz, Method method, int startOffset, int endOffset, InstructionVisitor instructionVisitor)
    {
        int offset = startOffset;
        while (offset < endOffset)
        {
            Instruction instruction = InstructionFactory.create(code, offset);
            int instructionLength = instruction.length(offset);
            instruction.accept(clazz, method, this, offset, instructionVisitor);
            offset += instructionLength;
        }
    }
    public void exceptionsAccept(Clazz clazz, Method method, ExceptionInfoVisitor exceptionInfoVisitor)
    {
        for (int index = 0; index < u2exceptionTableLength; index++)
        {
            exceptionInfoVisitor.visitExceptionInfo(clazz, method, this, exceptionTable[index]);
        }
    }
    public void exceptionsAccept(Clazz clazz, Method method, int offset, ExceptionInfoVisitor exceptionInfoVisitor)
    {
        for (int index = 0; index < u2exceptionTableLength; index++)
        {
            ExceptionInfo exceptionInfo = exceptionTable[index];
            if (exceptionInfo.isApplicable(offset))
            {
                exceptionInfoVisitor.visitExceptionInfo(clazz, method, this, exceptionInfo);
            }
        }
    }
    public void exceptionsAccept(Clazz clazz, Method method, int startOffset, int endOffset, ExceptionInfoVisitor exceptionInfoVisitor)
    {
        for (int index = 0; index < u2exceptionTableLength; index++)
        {
            ExceptionInfo exceptionInfo = exceptionTable[index];
            if (exceptionInfo.isApplicable(startOffset, endOffset))
            {
                exceptionInfoVisitor.visitExceptionInfo(clazz, method, this, exceptionInfo);
            }
        }
    }
    public void attributesAccept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        for (int index = 0; index < u2attributesCount; index++)
        {
            attributes[index].accept(clazz, method, this, attributeVisitor);
        }
    }
}
