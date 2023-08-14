import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.MethodInvocationFixer;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.ParameterUsageMarker;
import proguard.optimize.peephole.VariableShrinker;
public class MethodStaticizer
extends      SimplifiedVisitor
implements   MemberVisitor,
             AttributeVisitor
{
    private final MemberVisitor extraStaticMemberVisitor;
    public MethodStaticizer()
    {
        this(null);
    }
    public MethodStaticizer(MemberVisitor extraStaticMemberVisitor)
    {
        this.extraStaticMemberVisitor = extraStaticMemberVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (!ParameterUsageMarker.isParameterUsed(programMethod, 0))
        {
            programMethod.u2accessFlags =
                (programMethod.getAccessFlags() & ~ClassConstants.INTERNAL_ACC_FINAL) |
                ClassConstants.INTERNAL_ACC_STATIC;
            if (extraStaticMemberVisitor != null)
            {
                extraStaticMemberVisitor.visitProgramMethod(programClass, programMethod);
            }
        }
    }
}
