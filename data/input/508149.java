package proguard.classfile.visitor;
import proguard.classfile.*;
public class NamedFieldVisitor implements ClassVisitor
{
    private final String        name;
    private final String        descriptor;
    private final MemberVisitor memberVisitor;
    public NamedFieldVisitor(String        name,
                             String        descriptor,
                             MemberVisitor memberVisitor)
    {
        this.name          = name;
        this.descriptor    = descriptor;
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.fieldAccept(name, descriptor, memberVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldAccept(name, descriptor, memberVisitor);
    }
}
