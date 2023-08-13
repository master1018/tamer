import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
import proguard.evaluation.*;
import proguard.evaluation.value.*;
import proguard.optimize.evaluation.*;
import java.util.*;
public class CodePreverifier
extends      SimplifiedVisitor
implements   MemberVisitor,
             AttributeVisitor
{
    private static final boolean DEBUG = false;
    public CodePreverifier(boolean microEdition)
    {
        this.microEdition = microEdition;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        try
        {
            visitCodeAttribute0(clazz, method, codeAttribute);
        }
        catch (RuntimeException ex)
        {
            System.err.println("Unexpected error while preverifying:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            throw ex;
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        ProgramClass  programClass  = (ProgramClass)clazz;
        ProgramMethod programMethod = (ProgramMethod)method;
        livenessAnalyzer.visitCodeAttribute(clazz, method, codeAttribute);
        List stackMapFrameList = new ArrayList();
        for (int offset = 0; offset < codeAttribute.u4codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset) &&
                partialEvaluator.isBranchOrExceptionTarget(offset))
            {
                VerificationType[] variableTypes =
                    correspondingVerificationTypes(programClass,
                                                   programMethod,
                                                   codeAttribute,
                                                   offset,
                                                   partialEvaluator.getVariablesBefore(offset));
                VerificationType[] stackTypes =
                    correspondingVerificationTypes(programClass,
                                                   programMethod,
                                                   codeAttribute,
                                                   offset,
                                                   partialEvaluator.getStackBefore(offset));
                stackMapFrameList.add(new FullFrame(offset,
                                                    variableTypes,
                                                    stackTypes));
            }
        }
        if (!microEdition && !stackMapFrameList.isEmpty())
        {
            VerificationType[] initialVariables =
                correspondingVerificationTypes(programClass,
                                               programMethod,
                                               codeAttribute,
                                               PartialEvaluator.AT_METHOD_ENTRY,
                                               partialEvaluator.getVariablesBefore(0));
            if (method.getName(programClass).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
            {
                initialVariables[0] = VerificationTypeFactory.createUninitializedThisType();
            }
            compressStackMapFrames(initialVariables,
                                   stackMapFrameList);
        }
        String stackMapAttributeName = microEdition ?
             ClassConstants.ATTR_StackMap :
             ClassConstants.ATTR_StackMapTable;
        int frameCount = stackMapFrameList.size();
        if (DEBUG)
        {
            Attribute originalStackMapAttribute = codeAttribute.getAttribute(clazz,
                                                                             stackMapAttributeName);
            if (originalStackMapAttribute != null)
            {
                int originalFrameCount = microEdition ?
                    ((StackMapAttribute)originalStackMapAttribute).u2stackMapFramesCount :
                    ((StackMapTableAttribute)originalStackMapAttribute).u2stackMapFramesCount;
                StackMapFrame[] originalFrames = microEdition ?
                    ((StackMapAttribute)originalStackMapAttribute).stackMapFrames :
                    ((StackMapTableAttribute)originalStackMapAttribute).stackMapFrames;
                if (frameCount != originalFrameCount ||
                    !Arrays.equals(stackMapFrameList.toArray(), originalFrames))
                {
                    System.out.println("Original preverification ["+clazz.getName()+"]:");
                    new ClassPrinter().visitProgramMethod(programClass, programMethod);
                }
            }
            else if (frameCount != 0)
            {
                System.out.println("Original preverification empty ["+clazz.getName()+"."+method.getName(clazz)+"]");
            }
        }
        if (frameCount == 0)
        {
            new AttributesEditor(programClass, programMethod, codeAttribute, true).deleteAttribute(stackMapAttributeName);
        }
        else
        {
            Attribute stackMapAttribute;
            if (microEdition)
            {
                FullFrame[] stackMapFrames = new FullFrame[frameCount];
                stackMapFrameList.toArray(stackMapFrames);
                stackMapAttribute = new StackMapAttribute(stackMapFrames);
            }
            else
            {
                StackMapFrame[] stackMapFrames = new StackMapFrame[frameCount];
                stackMapFrameList.toArray(stackMapFrames);
                stackMapAttribute = new StackMapTableAttribute(stackMapFrames);
            }
            stackMapAttribute.u2attributeNameIndex =
                new ConstantPoolEditor(programClass).addUtf8Constant(stackMapAttributeName);
            new AttributesEditor(programClass, programMethod, codeAttribute, true).addAttribute(stackMapAttribute);
            if (DEBUG)
            {
                System.out.println("Preverifier ["+programClass.getName()+"."+programMethod.getName(programClass)+"]:");
                stackMapAttribute.accept(programClass, programMethod, codeAttribute, new ClassPrinter());
            }
        }
    }
    private VerificationType[] correspondingVerificationTypes(ProgramClass    programClass,
                                                              ProgramMethod   programMethod,
                                                              CodeAttribute   codeAttribute,
                                                              int             offset,
                                                              TracedVariables variables)
    {
        int maximumVariablesSize = variables.size();
        int typeCount = 0;
        int typeIndex = 0;
        for (int index = 0; index < maximumVariablesSize; index++)
        {
            Value value = variables.getValue(index);
            typeIndex++;
            if (value != null &&
                (offset == PartialEvaluator.AT_METHOD_ENTRY ||
                 livenessAnalyzer.isAliveBefore(offset, index)))
            {
                typeCount = typeIndex;
                if (value.isCategory2())
                {
                    index++;
                }
            }
        }
        VerificationType[] types = new VerificationType[typeCount];
        typeIndex = 0;
        for (int index = 0; typeIndex < typeCount; index++)
        {
            Value value         = variables.getValue(index);
            Value producerValue = variables.getProducerValue(index);
            VerificationType type;
            if (value != null &&
                (offset == PartialEvaluator.AT_METHOD_ENTRY ||
                 livenessAnalyzer.isAliveBefore(offset, index)))
            {
                type = correspondingVerificationType(programClass,
                                                     programMethod,
                                                     codeAttribute,
                                                     offset,
                                                     index == 0,
                                                     value,
                                                     producerValue);
                if (value.isCategory2())
                {
                    index++;
                }
            }
            else
            {
                type = VerificationTypeFactory.createTopType();
            }
            types[typeIndex++] = type;
        }
        return types;
    }
    private VerificationType[] correspondingVerificationTypes(ProgramClass  programClass,
                                                              ProgramMethod programMethod,
                                                              CodeAttribute codeAttribute,
                                                              int           offset,
                                                              TracedStack   stack)
    {
        int maximumStackSize = stack.size();
        int typeCount = 0;
        for (int index = 0; index < maximumStackSize; index++)
        {
            Value value = stack.getTop(index);
            typeCount++;
            if (value.isCategory2())
            {
                index++;
            }
        }
        VerificationType[] types = new VerificationType[typeCount];
        int typeIndex = typeCount;
        for (int index = 0; index < maximumStackSize; index++)
        {
            Value value         = stack.getTop(index);
            Value producerValue = stack.getTopProducerValue(index);
            types[--typeIndex] =
                correspondingVerificationType(programClass,
                                              programMethod,
                                              codeAttribute,
                                              offset,
                                              false,
                                              value,
                                              producerValue);
            if (value.isCategory2())
            {
                index++;
            }
        }
        return types;
    }
    private VerificationType correspondingVerificationType(ProgramClass  programClass,
                                                           ProgramMethod programMethod,
                                                           CodeAttribute codeAttribute,
                                                           int           offset,
                                                           boolean       isVariable0,
                                                           Value         value,
                                                           Value         producerValue)
    {
        if (value == null)
        {
            return VerificationTypeFactory.createTopType();
        }
        int type = value.computationalType();
        switch (type)
        {
            case Value.TYPE_INSTRUCTION_OFFSET:
            case Value.TYPE_INTEGER:   return VerificationTypeFactory.createIntegerType();
            case Value.TYPE_LONG:      return VerificationTypeFactory.createLongType();
            case Value.TYPE_FLOAT:     return VerificationTypeFactory.createFloatType();
            case Value.TYPE_DOUBLE:    return VerificationTypeFactory.createDoubleType();
            case Value.TYPE_TOP:       return VerificationTypeFactory.createTopType();
            case Value.TYPE_REFERENCE:
                ReferenceValue referenceValue = value.referenceValue();
                if (referenceValue.isNull() == Value.ALWAYS)
                {
                    return VerificationTypeFactory.createNullType();
                }
                if (offset != PartialEvaluator.AT_METHOD_ENTRY)
                {
                    InstructionOffsetValue producers = producerValue.instructionOffsetValue();
                    if (producers.instructionOffsetCount() == 1)
                    {
                        int producerOffset = producers.instructionOffset(0);
                        while (producerOffset != PartialEvaluator.AT_METHOD_ENTRY &&
                               isDupOrSwap(codeAttribute.code[producerOffset]))
                        {
                            producers      = partialEvaluator.getStackBefore(producerOffset).getTopProducerValue(0).instructionOffsetValue();
                            producerOffset = producers.instructionOffset(0);
                        }
                        if (partialEvaluator.isInitializer()                       &&
                            offset <= partialEvaluator.superInitializationOffset() &&
                            (isVariable0 ||
                             producerOffset > PartialEvaluator.AT_METHOD_ENTRY &&
                             codeAttribute.code[producerOffset] == InstructionConstants.OP_ALOAD_0))
                        {
                            return VerificationTypeFactory.createUninitializedThisType();
                        }
                        if (producerOffset > PartialEvaluator.AT_METHOD_ENTRY &&
                            offset <= partialEvaluator.initializationOffset(producerOffset))
                        {
                            return VerificationTypeFactory.createUninitializedType(producerOffset);
                        }
                    }
                }
                return VerificationTypeFactory.createObjectType(createClassConstant(programClass, referenceValue));
        }
        throw new IllegalArgumentException("Unknown computational type ["+type+"]");
    }
    private int createClassConstant(ProgramClass   programClass,
                                    ReferenceValue referenceValue)
    {
        return new ConstantPoolEditor(programClass).addClassConstant(referenceValue.getType(),
                                                                     referenceValue.getReferencedClass());
    }
    private void compressStackMapFrames(VerificationType[] initialVariableTypes,
                                        List               stackMapFrameList)
    {
        int                previousVariablesCount = initialVariableTypes.length;
        VerificationType[] previousVariableTypes  = initialVariableTypes;
        int previousOffset = -1;
        for (int index = 0; index < stackMapFrameList.size(); index++)
        {
            FullFrame fullFrame = (FullFrame)stackMapFrameList.get(index);
            int                variablesCount = fullFrame.variablesCount;
            VerificationType[] variables      = fullFrame.variables;
            int                stackCount     = fullFrame.stackCount;
            VerificationType[] stack          = fullFrame.stack;
            StackMapFrame compressedFrame = fullFrame;
            if (variablesCount == previousVariablesCount &&
                equalVerificationTypes(variables, previousVariableTypes, variablesCount))
            {
                if (stackCount == 0)
                {
                    compressedFrame = new SameZeroFrame();
                }
                else if (stackCount == 1)
                {
                    compressedFrame = new SameOneFrame(stack[0]);
                }
            }
            else if (stackCount == 0)
            {
                int additionalVariablesCount = variablesCount - previousVariablesCount;
                if (additionalVariablesCount < 0  &&
                    additionalVariablesCount > -4 &&
                    equalVerificationTypes(variables, previousVariableTypes, variablesCount))
                {
                    compressedFrame = new LessZeroFrame((byte)-additionalVariablesCount);
                }
                else if (
                         additionalVariablesCount > 0 &&
                         additionalVariablesCount < 4 &&
                         equalVerificationTypes(variables, previousVariableTypes, previousVariablesCount))
                {
                    VerificationType[] additionalVariables = new VerificationType[additionalVariablesCount];
                    System.arraycopy(variables, variablesCount - additionalVariablesCount,
                                     additionalVariables, 0,
                                     additionalVariablesCount);
                    compressedFrame = new MoreZeroFrame(additionalVariables);
                }
            }
            int offset = fullFrame.u2offsetDelta;
            compressedFrame.u2offsetDelta = offset - previousOffset - 1;
            previousOffset = offset;
            previousVariablesCount = fullFrame.variablesCount;
            previousVariableTypes     = fullFrame.variables;
            stackMapFrameList.set(index, compressedFrame);
        }
    }
    private boolean equalVerificationTypes(VerificationType[] types1,
                                           VerificationType[] types2,
                                           int                length)
    {
        if (length > 0 &&
            (types1.length < length ||
             types2.length < length))
        {
            return false;
        }
        for (int index = 0; index < length; index++)
        {
            if (!types1[index].equals(types2[index]))
            {
                return false;
            }
        }
        return true;
    }
    private boolean isDupOrSwap(int opcode)
    {
        return opcode >= InstructionConstants.OP_DUP &&
               opcode <= InstructionConstants.OP_SWAP;
    }
}
