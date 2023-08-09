package proguard.classfile.visitor;
import proguard.classfile.*;
public class LibraryClassFilter implements ClassVisitor
{
    private final ClassVisitor classVisitor;
    public LibraryClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        classVisitor.visitLibraryClass(libraryClass);
    }
}
