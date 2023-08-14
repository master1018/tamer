import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.MethodrefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.CodeAttributeComposer;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
public class TailRecursionSimplifier
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor,
             ExceptionInfoVisitor
{
    private static final boolean DEBUG = false;
    public TailRecursionSimplifier()
    {
        this(null);
    }
    public TailRecursionSimplifier(InstructionVisitor extraTailRecursionVisitor)
    {
        this.extraTailRecursionVisitor = extraTailRecursionVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        int accessFlags = method.getAccessFlags();
        if (
            (accessFlags & (ClassConstants.INTERNAL_ACC_PRIVATE |
                            ClassConstants.INTERNAL_ACC_STATIC  |
                            ClassConstants.INTERNAL_ACC_FINAL)) != 0 &&
            (accessFlags & (ClassConstants.INTERNAL_ACC_SYNCHRONIZED |
                            ClassConstants.INTERNAL_ACC_NATIVE       |
                            ClassConstants.INTERNAL_ACC_INTERFACE    |
                            ClassConstants.INTERNAL_ACC_ABSTRACT)) == 0)
        {
            targetMethod    = method;
            inlinedAny      = false;
            codeAttributeComposer.reset();
            copyCode(clazz, method, codeAttribute);
            if (inlinedAny)
            {
                codeAttributeComposer.visitCodeAttribute(clazz, method, codeAttribute);
            }
        }
    }
    private void copyCode(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttributeComposer.beginCodeFragment(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttributeComposer.appendLabel(codeAttribute.u4codeLength);
        codeAttributeComposer.endCodeFragment();
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        codeAttributeComposer.appendInstruction(offset, instruction.shrink());
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        switch (constantInstruction.opcode)
        {
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            {
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                if (recursive)
                {
                    int nextOffset =
                        offset + constantInstruction.length(offset);
                    Instruction nextInstruction =
                        InstructionFactory.create(codeAttribute.code, nextOffset);
                    switch (nextInstruction.opcode)
                    {
                        case InstructionConstants.OP_IRETURN:
                        case InstructionConstants.OP_LRETURN:
                        case InstructionConstants.OP_FRETURN:
                        case InstructionConstants.OP_DRETURN:
                        case InstructionConstants.OP_ARETURN:
                        case InstructionConstants.OP_RETURN:
                        {
                            codeAttribute.exceptionsAccept(clazz, method, offset, this);
                            if (recursive)
                            {
                                if (DEBUG)
                                {
                                    System.out.println("TailRecursionSimplifier.visitConstantInstruction: ["+
                                                       clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"], inlining "+constantInstruction.toString(offset));
                                }
                                codeAttributeComposer.appendLabel(offset);
                                storeParameters(clazz, method);
                                int gotoOffset = offset + 1;
                                codeAttributeComposer.appendInstruction(gotoOffset,
                                                                        new BranchInstruction(InstructionConstants.OP_GOTO, -gotoOffset));
                                inlinedAny = true;
                                if (extraTailRecursionVisitor != null)
                                {
                                    extraTailRecursionVisitor.visitConstantInstruction(clazz, method, codeAttribute, offset, constantInstruction);
                                }
                                return;
                            }
                        }
                    }
                }
                break;
            }
        }
        codeAttributeComposer.appendInstruction(offset, constantInstruction.shrink());
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        recursive = targetMethod.equals(methodrefConstant.referencedMember);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        recursive = false;
    }
    private void storeParameters(Clazz clazz, Method method)
    {
        String descriptor = method.getDescriptor(clazz);
        boolean isStatic =
            (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0;
        int parameterCount  = ClassUtil.internalMethodParameterCount(descriptor);
        int parameterSize   = ClassUtil.internalMethodParameterSize(descriptor);
        int parameterOffset = isStatic ? 0 : 1;
        String[] parameterTypes = new String[parameterSize];
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(descriptor);
        for (int parameterIndex = 0; parameterIndex < parameterSize; parameterIndex++)
        {
            String parameterType = internalTypeEnumeration.nextType();
            parameterTypes[parameterIndex] = parameterType;
            if (ClassUtil.internalTypeSize(parameterType) == 2)
            {
                parameterIndex++;
            }
        }
        codeAttributeComposer.beginCodeFragment(parameterSize + 1);
        for (int parameterIndex = parameterSize-1; parameterIndex >= 0; parameterIndex--)
        {
            String parameterType = parameterTypes[parameterIndex];
            if (parameterType != null)
            {
                byte opcode;
                switch (parameterType.charAt(0))
                {
                    case ClassConstants.INTERNAL_TYPE_BOOLEAN:
                    case ClassConstants.INTERNAL_TYPE_BYTE:
                    case ClassConstants.INTERNAL_TYPE_CHAR:
                    case ClassConstants.INTERNAL_TYPE_SHORT:
                    case ClassConstants.INTERNAL_TYPE_INT:
                        opcode = InstructionConstants.OP_ISTORE;
                        break;
                    case ClassConstants.INTERNAL_TYPE_LONG:
                        opcode = InstructionConstants.OP_LSTORE;
                        break;
                    case ClassConstants.INTERNAL_TYPE_FLOAT:
                        opcode = InstructionConstants.OP_FSTORE;
                        break;
                    case ClassConstants.INTERNAL_TYPE_DOUBLE:
                        opcode = InstructionConstants.OP_DSTORE;
                        break;
                    default:
                        opcode = InstructionConstants.OP_ASTORE;
                        break;
                }
                codeAttributeComposer.appendInstruction(parameterSize-parameterIndex-1,
                                                        new VariableInstruction(opcode, parameterOffset + parameterIndex).shrink());
            }
        }
        if (!isStatic)
        {
            codeAttributeComposer.appendInstruction(parameterSize,
                                                    new VariableInstruction(InstructionConstants.OP_ASTORE, 0).shrink());
        }
        codeAttributeComposer.endCodeFragment();
    }
}