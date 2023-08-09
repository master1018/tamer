package proguard.classfile.visitor;
import proguard.classfile.*;
public class ProgramMemberFilter implements MemberVisitor
{
    private final MemberVisitor memberVisitor;
    public ProgramMemberFilter(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        memberVisitor.visitProgramField(programClass, programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        memberVisitor.visitProgramMethod(programClass, programMethod);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
    }
}
