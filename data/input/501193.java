import proguard.classfile.*;
import proguard.classfile.visitor.MemberVisitor;
public class MemberSpecialNameFilter implements MemberVisitor
{
    private final MemberVisitor memberVisitor;
    public MemberSpecialNameFilter(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (isSpecialName(programField))
        {
            memberVisitor.visitProgramField(programClass, programField);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (isSpecialName(programMethod))
        {
            memberVisitor.visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        if (isSpecialName(libraryField))
        {
            memberVisitor.visitLibraryField(libraryClass, libraryField);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (isSpecialName(libraryMethod))
        {
            memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
    private static boolean isSpecialName(Member member)
    {
        return SpecialNameFactory.isSpecialName(MemberObfuscator.newMemberName(member));
    }
}
