package proguard.classfile.visitor;
import proguard.classfile.*;
public class AllMemberVisitor implements ClassVisitor
{
    private final MemberVisitor memberVisitor;
    public AllMemberVisitor(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.fieldsAccept(memberVisitor);
        programClass.methodsAccept(memberVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldsAccept(memberVisitor);
        libraryClass.methodsAccept(memberVisitor);
    }
}
