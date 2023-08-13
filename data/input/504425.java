import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.evaluation.value.Value;
import proguard.optimize.evaluation.StoringInvocationUnit;
import proguard.optimize.info.ParameterUsageMarker;
public class ConstantParameterFilter
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final MemberVisitor constantParameterVisitor;
    public ConstantParameterFilter(MemberVisitor constantParameterVisitor)
    {
        this.constantParameterVisitor = constantParameterVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        int firstParameterIndex =
            (programMethod.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;
        int parameterCount =
            ClassUtil.internalMethodParameterCount(programMethod.getDescriptor(programClass));
        for (int index = firstParameterIndex; index < parameterCount; index++)
        {
            Value value = StoringInvocationUnit.getMethodParameterValue(programMethod, index);
            if (value != null &&
                value.isParticular())
            {
                constantParameterVisitor.visitProgramMethod(programClass, programMethod);
            }
        }
    }
}