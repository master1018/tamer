package proguard.classfile.visitor;
import proguard.classfile.*;
public class BottomClassFilter implements ClassVisitor
{
    private final ClassVisitor classVisitor;
    public BottomClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (programClass.subClasses == null)
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (libraryClass.subClasses == null)
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}
