package proguard.classfile.visitor;
import proguard.classfile.*;
public class NamedMethodVisitor implements ClassVisitor
{
    private final String        name;
    private final String        descriptor;
    private final MemberVisitor memberVisitor;
    public NamedMethodVisitor(String        name,
                              String        descriptor,
                              MemberVisitor memberVisitor)
    {
        this.name          = name;
        this.descriptor    = descriptor;
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.methodAccept(name, descriptor, memberVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.methodAccept(name, descriptor, memberVisitor);
    }
}
