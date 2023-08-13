package proguard.classfile.visitor;
import proguard.classfile.*;
public class AllMethodVisitor implements ClassVisitor
{
    private final MemberVisitor memberVisitor;
    public AllMethodVisitor(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.methodsAccept(memberVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.methodsAccept(memberVisitor);
    }
}
