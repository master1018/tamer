import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.ParameterUsageMarker;
public class ParameterShrinker
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final boolean DEBUG = false;
    private final MemberVisitor extraVariableMemberVisitor;
    private final VariableRemapper variableRemapper = new VariableRemapper();
    public ParameterShrinker()
    {
        this(null);
    }
    public ParameterShrinker(MemberVisitor extraVariableMemberVisitor)
    {
        this.extraVariableMemberVisitor = extraVariableMemberVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        int oldParameterSize = ParameterUsageMarker.getParameterSize(method);
        int newParameterSize =
            ClassUtil.internalMethodParameterSize(method.getDescriptor(clazz),
                                                  method.getAccessFlags());
        if (oldParameterSize > newParameterSize)
        {
            int maxLocals = codeAttribute.u2maxLocals;
            if (DEBUG)
            {
                System.out.println("ParameterShrinker: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
                System.out.println("  Old parameter size = " + oldParameterSize);
                System.out.println("  New parameter size = " + newParameterSize);
                System.out.println("  Max locals         = " + maxLocals);
            }
            int[] variableMap = new int[maxLocals];
            int usedParameterIndex   = 0;
            int unusedParameterIndex = newParameterSize;
            for (int parameterIndex = 0; parameterIndex < oldParameterSize; parameterIndex++)
            {
                if (ParameterUsageMarker.isParameterUsed(method, parameterIndex))
                {
                    variableMap[parameterIndex] = usedParameterIndex++;
                }
                else
                {
                    if (DEBUG)
                    {
                        System.out.println("  Deleting parameter #"+parameterIndex);
                    }
                    variableMap[parameterIndex] = unusedParameterIndex++;
                    if (extraVariableMemberVisitor != null)
                    {
                        method.accept(clazz, extraVariableMemberVisitor);
                    }
                }
            }
            for (int variableIndex = oldParameterSize; variableIndex < maxLocals; variableIndex++)
            {
                variableMap[variableIndex] = variableIndex;
            }
            variableRemapper.setVariableMap(variableMap);
            variableRemapper.visitCodeAttribute(clazz, method, codeAttribute);
        }
    }
}
