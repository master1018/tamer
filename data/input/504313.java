import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.*;
import java.util.Stack;
public class MethodInliner
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor,
             MemberVisitor
{
    private static final int MAXIMUM_INLINED_CODE_LENGTH       = Integer.parseInt(System.getProperty("maximum.inlined.code.length",      "8"));
    private static final int MAXIMUM_RESULTING_CODE_LENGTH_JSE = Integer.parseInt(System.getProperty("maximum.resulting.code.length", "8000"));
    private static final int MAXIMUM_RESULTING_CODE_LENGTH_JME = Integer.parseInt(System.getProperty("maximum.resulting.code.length", "2000"));
    private static final int MAXIMUM_CODE_EXPANSION            = 2;
    private static final int MAXIMUM_EXTRA_CODE_LENGTH         = 128;
    private static final boolean DEBUG = false;
    public MethodInliner(boolean microEdition,
                         boolean allowAccessModification,
                         boolean inlineSingleInvocations)
    {
        this(microEdition,
             allowAccessModification,
             inlineSingleInvocations,
             null);
    }
    public MethodInliner(boolean            microEdition,
                         boolean            allowAccessModification,
                         boolean            inlineSingleInvocations,
                         InstructionVisitor extraInlinedInvocationVisitor)
    {
        this.microEdition                  = microEdition;
        this.allowAccessModification       = allowAccessModification;
        this.inlineSingleInvocations       = inlineSingleInvocations;
        this.extraInlinedInvocationVisitor = extraInlinedInvocationVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (!inlining)
        {
            targetClass                  = (ProgramClass)clazz;
            targetMethod                 = (ProgramMethod)method;
            constantAdder                = new ConstantAdder(targetClass);
            exceptionInfoAdder           = new ExceptionInfoAdder(targetClass, codeAttributeComposer);
            estimatedResultingCodeLength = codeAttribute.u4codeLength;
            inliningMethods.clear();
            uninitializedObjectCount     = method.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ? 1 : 0;
            inlinedAny                   = false;
            codeAttributeComposer.reset();
            stackSizeComputer.visitCodeAttribute(clazz, method, codeAttribute);
            copyCode(clazz, method, codeAttribute);
            targetClass   = null;
            targetMethod  = null;
            constantAdder = null;
            if (inlinedAny)
            {
                codeAttributeComposer.visitCodeAttribute(clazz, method, codeAttribute);
                codeAttribute.instructionsAccept(clazz, method, accessMethodMarker);
                catchExceptionMarker.visitCodeAttribute(clazz, method, codeAttribute);
            }
        }
        else if ((inlineSingleInvocations ?
                      MethodInvocationMarker.getInvocationCount(method) == 1 :
                      codeAttribute.u4codeLength <= MAXIMUM_INLINED_CODE_LENGTH) &&
                 estimatedResultingCodeLength + codeAttribute.u4codeLength <
                 (microEdition ?
                     MAXIMUM_RESULTING_CODE_LENGTH_JME :
                     MAXIMUM_RESULTING_CODE_LENGTH_JSE))
        {
            if (DEBUG)
            {
                System.out.println("MethodInliner: inlining ["+
                                   clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"] in ["+
                                   targetClass.getName()+"."+targetMethod.getName(targetClass)+targetMethod.getDescriptor(targetClass)+"]");
            }
            estimatedResultingCodeLength += codeAttribute.u4codeLength;
            storeParameters(clazz, method);
            copyCode(clazz, method, codeAttribute);
            inlined    = true;
            inlinedAny = true;
        }
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
        codeAttributeComposer.beginCodeFragment(parameterSize+1);
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
                                                        new VariableInstruction(opcode, variableOffset + parameterOffset + parameterIndex).shrink());
            }
        }
        if (!isStatic)
        {
            codeAttributeComposer.appendInstruction(parameterSize,
                                                    new VariableInstruction(InstructionConstants.OP_ASTORE, variableOffset).shrink());
        }
        codeAttributeComposer.endCodeFragment();
    }
    private void copyCode(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttributeComposer.beginCodeFragment(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.exceptionsAccept(clazz, method, exceptionInfoAdder);
        codeAttributeComposer.appendLabel(codeAttribute.u4codeLength);
        codeAttributeComposer.endCodeFragment();
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        codeAttributeComposer.appendInstruction(offset, instruction.shrink());
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        if (inlining)
        {
            switch (simpleInstruction.opcode)
            {
                case InstructionConstants.OP_IRETURN:
                case InstructionConstants.OP_LRETURN:
                case InstructionConstants.OP_FRETURN:
                case InstructionConstants.OP_DRETURN:
                case InstructionConstants.OP_ARETURN:
                case InstructionConstants.OP_RETURN:
                    if (offset < codeAttribute.u4codeLength-1)
                    {
                        Instruction branchInstruction =
                            new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                                  codeAttribute.u4codeLength - offset);
                        codeAttributeComposer.appendInstruction(offset,
                                                                branchInstruction.shrink());
                    }
                    else
                    {
                        codeAttributeComposer.appendLabel(offset);
                    }
                    return;
            }
        }
        codeAttributeComposer.appendInstruction(offset, simpleInstruction.shrink());
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        if (inlining)
        {
            variableInstruction.variableIndex += variableOffset;
        }
        codeAttributeComposer.appendInstruction(offset, variableInstruction.shrink());
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        switch (constantInstruction.opcode)
        {
            case InstructionConstants.OP_NEW:
                uninitializedObjectCount++;
                break;
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                inlined = false;
                codeAttributeComposer.appendLabel(offset);
                emptyInvokingStack =
                    !inlining &&
                    stackSizeComputer.isReachable(offset) &&
                    stackSizeComputer.getStackSize(offset) == 0;
                variableOffset += codeAttribute.u2maxLocals;
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                variableOffset -= codeAttribute.u2maxLocals;
                if (inlined)
                {
                    if (extraInlinedInvocationVisitor != null)
                    {
                        extraInlinedInvocationVisitor.visitConstantInstruction(clazz, method, codeAttribute, offset, constantInstruction);
                    }
                    return;
                }
                break;
        }
        if (inlining)
        {
            constantInstruction.constantIndex =
                constantAdder.addConstant(clazz, constantInstruction.constantIndex);
        }
        codeAttributeComposer.appendInstruction(offset, constantInstruction.shrink());
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant) {}
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        methodrefConstant.referencedMemberAccept(this);
    }
    public void visitAnyMember(Clazz Clazz, Member member) {}
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        int accessFlags = programMethod.getAccessFlags();
        if (
            (accessFlags & (ClassConstants.INTERNAL_ACC_PRIVATE |
                            ClassConstants.INTERNAL_ACC_STATIC  |
                            ClassConstants.INTERNAL_ACC_FINAL)) != 0                               &&
            (accessFlags & (ClassConstants.INTERNAL_ACC_SYNCHRONIZED |
                            ClassConstants.INTERNAL_ACC_NATIVE       |
                            ClassConstants.INTERNAL_ACC_INTERFACE    |
                            ClassConstants.INTERNAL_ACC_ABSTRACT)) == 0                            &&
            !programMethod.getName(programClass).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT)  &&
            (!programMethod.equals(targetMethod) ||
             !programClass.equals(targetClass))                                                    &&
            !inliningMethods.contains(programMethod)                                               &&
            targetClass.u4version >= programClass.u4version                                        &&
            (!SuperInvocationMarker.invokesSuperMethods(programMethod) ||
             programClass.equals(targetClass))                                                     &&
            (!BackwardBranchMarker.branchesBackward(programMethod) ||
             uninitializedObjectCount == 0)                                                        &&
            (allowAccessModification ||
             ((!AccessMethodMarker.accessesPrivateCode(programMethod) ||
               programClass.equals(targetClass)) &&
              (!AccessMethodMarker.accessesPackageCode(programMethod) ||
               ClassUtil.internalPackageName(programClass.getName()).equals(
               ClassUtil.internalPackageName(targetClass.getName())))))                            &&
            (!AccessMethodMarker.accessesProtectedCode(programMethod) ||
             programClass.equals(targetClass))                                                     &&
            (!CatchExceptionMarker.catchesExceptions(programMethod) ||
             emptyInvokingStack)                                                                   &&
            (programClass.equals(targetClass) ||
             programClass.findMethod(ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                     ClassConstants.INTERNAL_METHOD_TYPE_CLINIT) == null))
        {
            boolean oldInlining = inlining;
            inlining = true;
            inliningMethods.push(programMethod);
            programMethod.attributesAccept(programClass, this);
            MethodOptimizationInfo info =
                MethodOptimizationInfo.getMethodOptimizationInfo(targetMethod);
            if (info != null)
            {
                info.merge(MethodOptimizationInfo.getMethodOptimizationInfo(programMethod));
            }
            inlining = oldInlining;
            inliningMethods.pop();
        }
        else if (programMethod.getName(programClass).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            uninitializedObjectCount--;
        }
    }
}
