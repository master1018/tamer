package proguard.classfile.visitor;
import proguard.classfile.*;
public class AllFieldVisitor implements ClassVisitor
{
    private final MemberVisitor memberVisitor;
    public AllFieldVisitor(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.fieldsAccept(memberVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldsAccept(memberVisitor);
    }
}
