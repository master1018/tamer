import proguard.classfile.*;
import proguard.classfile.visitor.MemberVisitor;
public class MemberNameCleaner implements MemberVisitor
{
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        MemberObfuscator.setNewMemberName(programField, null);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        MemberObfuscator.setNewMemberName(programMethod, null);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        MemberObfuscator.setNewMemberName(libraryField, null);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        MemberObfuscator.setNewMemberName(libraryMethod, null);
    }
}
