import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.VariableEditor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.*;
import proguard.optimize.info.*;
public class VariableShrinker
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final boolean DEBUG = false;
    private final MemberVisitor extraVariableMemberVisitor;
    private final VariableUsageMarker variableUsageMarker = new VariableUsageMarker();
    private final VariableEditor      variableEditor      = new VariableEditor();
    public VariableShrinker()
    {
        this(null);
    }
    public VariableShrinker(MemberVisitor extraVariableMemberVisitor)
    {
        this.extraVariableMemberVisitor = extraVariableMemberVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if ((method.getAccessFlags() & ClassConstants.INTERNAL_ACC_ABSTRACT) == 0)
        {
            int parameterSize =
                ClassUtil.internalMethodParameterSize(method.getDescriptor(clazz),
                                                      method.getAccessFlags());
            int maxLocals = codeAttribute.u2maxLocals;
            if (DEBUG)
            {
                System.out.println("VariableShrinker: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
                System.out.println("  Parameter size = " + parameterSize);
                System.out.println("  Max locals     = " + maxLocals);
            }
            variableUsageMarker.visitCodeAttribute(clazz, method, codeAttribute);
            variableEditor.reset(maxLocals);
            for (int variableIndex = parameterSize; variableIndex < maxLocals; variableIndex++)
            {
                if (!variableUsageMarker.isVariableUsed(variableIndex))
                {
                    if (DEBUG)
                    {
                        System.out.println("  Deleting local variable #"+variableIndex);
                    }
                    variableEditor.deleteVariable(variableIndex);
                    if (extraVariableMemberVisitor != null)
                    {
                        method.accept(clazz, extraVariableMemberVisitor);
                    }
                }
            }
            variableEditor.visitCodeAttribute(clazz, method, codeAttribute);
        }
    }
}
