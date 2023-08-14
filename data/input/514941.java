import proguard.classfile.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.VariableRemapper;
import proguard.classfile.util.*;
public class VariableOptimizer
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final boolean DEBUG = false;
    public VariableOptimizer(boolean reuseThis)
    {
        this(reuseThis, null);
    }
    public VariableOptimizer(boolean       reuseThis,
                             MemberVisitor extraVariableMemberVisitor)
    {
        this.reuseThis                  = reuseThis;
        this.extraVariableMemberVisitor = extraVariableMemberVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        initializeArrays(codeAttribute);
        livenessAnalyzer.visitCodeAttribute(clazz, method, codeAttribute);
        int startIndex =
            (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ||
            reuseThis ? 0 : 1;
        int parameterSize =
            ClassUtil.internalMethodParameterSize(method.getDescriptor(clazz),
                                                  method.getAccessFlags());
        int variableSize = codeAttribute.u2maxLocals;
        int codeLength   = codeAttribute.u4codeLength;
        boolean remapping = false;
        for (int oldIndex = 0; oldIndex < variableSize; oldIndex++)
        {
            variableMap[oldIndex] = oldIndex;
            if (oldIndex >= parameterSize &&
                oldIndex < MAX_VARIABLES_SIZE)
            {
                for (int newIndex = startIndex; newIndex < oldIndex; newIndex++)
                {
                    if (areNonOverlapping(oldIndex, newIndex, codeLength))
                    {
                        variableMap[oldIndex] = newIndex;
                        updateLiveness(oldIndex, newIndex, codeLength);
                        remapping = true;
                        break;
                    }
                }
            }
        }
        if (remapping)
        {
            if (DEBUG)
            {
                System.out.println("Remapping variables:");
                System.out.println("  Class "+ ClassUtil.externalClassName(clazz.getName()));
                System.out.println("  Method "+ClassUtil.externalFullMethodDescription(clazz.getName(),
                                                                                       0,
                                                                                       method.getName(clazz),
                                                                                       method.getDescriptor(clazz)));
                for (int index = 0; index < variableSize; index++)
                {
                    System.out.println("  ["+index+"] -> ["+variableMap[index]+"]");
                }
            }
            variableRemapper.setVariableMap(variableMap);
            variableRemapper.visitCodeAttribute(clazz, method, codeAttribute);
            if (extraVariableMemberVisitor != null)
            {
                method.accept(clazz, extraVariableMemberVisitor);
            }
        }
    }
    private void initializeArrays(CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;
        if (variableMap.length < codeLength)
        {
            variableMap = new int[codeLength];
        }
    }
    private boolean areNonOverlapping(int variableIndex1,
                                      int variableIndex2,
                                      int codeLength)
    {
        for (int offset = 0; offset < codeLength; offset++)
        {
            if ((livenessAnalyzer.isAliveBefore(offset, variableIndex1) &&
                 livenessAnalyzer.isAliveBefore(offset, variableIndex2)) ||
                (livenessAnalyzer.isAliveAfter(offset, variableIndex1) &&
                 livenessAnalyzer.isAliveAfter(offset, variableIndex2)) ||
                livenessAnalyzer.isCategory2(offset, variableIndex1))
            {
                return false;
            }
        }
        return true;
    }
    private void updateLiveness(int oldVariableIndex,
                                int newVariableIndex,
                                int codeLength)
    {
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (livenessAnalyzer.isAliveBefore(offset, oldVariableIndex))
            {
                livenessAnalyzer.setAliveBefore(offset, oldVariableIndex, false);
                livenessAnalyzer.setAliveBefore(offset, newVariableIndex, true);
            }
            if (livenessAnalyzer.isAliveAfter(offset, oldVariableIndex))
            {
                livenessAnalyzer.setAliveAfter(offset, oldVariableIndex, false);
                livenessAnalyzer.setAliveAfter(offset, newVariableIndex, true);
            }
        }
    }
}
