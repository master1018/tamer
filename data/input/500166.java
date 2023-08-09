package proguard.classfile.visitor;
import proguard.classfile.*;
public class ProgramClassFilter implements ClassVisitor
{
    private final ClassVisitor classVisitor;
    public ProgramClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        classVisitor.visitProgramClass(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
}
