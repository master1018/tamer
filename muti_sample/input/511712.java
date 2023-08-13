import proguard.classfile.*;
import proguard.classfile.editor.MethodInvocationFixer;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.NonPrivateMemberMarker;
public class MemberPrivatizer
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final MemberVisitor extraMemberVisitor;
    public MemberPrivatizer()
    {
        this(null);
    }
    public MemberPrivatizer(MemberVisitor extraMemberVisitor)
    {
        this.extraMemberVisitor = extraMemberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (NonPrivateMemberMarker.canBeMadePrivate(programField))
        {
            programField.u2accessFlags =
                AccessUtil.replaceAccessFlags(programField.u2accessFlags,
                                              ClassConstants.INTERNAL_ACC_PRIVATE);
            if (extraMemberVisitor != null)
            {
                extraMemberVisitor.visitProgramField(programClass, programField);
            }
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (NonPrivateMemberMarker.canBeMadePrivate(programMethod))
        {
            programMethod.u2accessFlags =
                AccessUtil.replaceAccessFlags(programMethod.u2accessFlags,
                                              ClassConstants.INTERNAL_ACC_PRIVATE);
            if (extraMemberVisitor != null)
            {
                extraMemberVisitor.visitProgramMethod(programClass, programMethod);
            }
        }
    }
}
