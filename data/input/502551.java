import proguard.classfile.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.evaluation.PartialEvaluator;
import proguard.evaluation.value.*;
public class ParameterUsageMarker
extends      SimplifiedVisitor
implements   MemberVisitor,
             AttributeVisitor,
             InstructionVisitor
{
    private static final boolean DEBUG = false;
    private final boolean          markThisParameter;
    private final boolean          markAllParameters;
    private final PartialEvaluator partialEvaluator = new PartialEvaluator();
    public ParameterUsageMarker()
    {
        this(false, false);
    }
    public ParameterUsageMarker(boolean markThisParameter,
                                boolean markAllParameters)
    {
        this.markThisParameter = markThisParameter;
        this.markAllParameters = markAllParameters;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        int parameterSize =
            ClassUtil.internalMethodParameterSize(programMethod.getDescriptor(programClass),
                                                  programMethod.getAccessFlags());
        if (parameterSize > 0)
        {
            int accessFlags = programMethod.getAccessFlags();
            if (markThisParameter &&
                (accessFlags & ClassConstants.INTERNAL_ACC_STATIC) == 0)
            {
                markParameterUsed(programMethod, 0);
            }
            if (markAllParameters)
            {
                markUsedParameters(programMethod,
                                   (accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                                       -1L : -2L);
            }
            if ((accessFlags & ClassConstants.INTERNAL_ACC_NATIVE) != 0)
            {
                markUsedParameters(programMethod, -1L);
            }
            else if ((accessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
            {
                markParameterUsed(programMethod, 0);
            }
            else
            {
                if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) == 0 &&
                    ((accessFlags & ClassConstants.INTERNAL_ACC_SYNCHRONIZED) != 0 ||
                     programClass.mayHaveImplementations(programMethod)            ||
                     programMethod.getName(programClass).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT)))
                {
                    markParameterUsed(programMethod, 0);
                }
                programMethod.attributesAccept(programClass, this);
            }
            if (DEBUG)
            {
                System.out.print("ParameterUsageMarker: ["+programClass.getName() +"."+programMethod.getName(programClass)+programMethod.getDescriptor(programClass)+"]: ");
                for (int index = 0; index < parameterSize; index++)
                {
                    System.out.print(isParameterUsed(programMethod, index) ? '+' : '-');
                }
                System.out.println();
            }
        }
        setParameterSize(programMethod, parameterSize);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (libraryClass.mayHaveImplementations(libraryMethod))
        {
            markUsedParameters(libraryMethod, -1L);
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.instructionsAccept(clazz, method, this);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        if (partialEvaluator.isTraced(offset) &&
            variableInstruction.isLoad())
        {
            int parameterIndex = variableInstruction.variableIndex;
            if (parameterIndex < codeAttribute.u2maxLocals)
            {
                Value producer =
                    partialEvaluator.getVariablesBefore(offset).getProducerValue(parameterIndex);
                if (producer != null &&
                    producer.instructionOffsetValue().contains(PartialEvaluator.AT_METHOD_ENTRY))
                {
                    markParameterUsed(method, parameterIndex);
                    if (variableInstruction.isCategory2())
                    {
                        markParameterUsed(method, parameterIndex + 1);
                    }
                }
            }
        }
    }
    private static void setParameterSize(Method method, int parameterSize)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setParameterSize(parameterSize);
        }
    }
    public static int getParameterSize(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info != null ? info.getParameterSize() : 0;
    }
    public static void markParameterUsed(Method method, int variableIndex)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setParameterUsed(variableIndex);
        }
    }
    public static void markUsedParameters(Method method, long usedParameters)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setUsedParameters(info.getUsedParameters() | usedParameters);
        }
    }
    public static boolean isParameterUsed(Method method, int variableIndex)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null ||
               info.isParameterUsed(variableIndex);
    }
    public static long getUsedParameters(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info != null ? info.getUsedParameters() : -1L;
    }
}
