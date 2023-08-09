package proguard.classfile.visitor;
import proguard.classfile.*;
public class LibraryMemberFilter implements MemberVisitor
{
    private final MemberVisitor memberVisitor;
    public LibraryMemberFilter(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        memberVisitor.visitLibraryField(libraryClass, libraryField);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
    }
}
