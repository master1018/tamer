package proguard.classfile.constant.visitor;
import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class AllConstantVisitor implements ClassVisitor
{
    private final ConstantVisitor constantVisitor;
    public AllConstantVisitor(ConstantVisitor constantVisitor)
    {
        this.constantVisitor = constantVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(constantVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass) {}
}
