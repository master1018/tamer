package proguard.classfile.visitor;
import proguard.classfile.*;
public class ExceptClassFilter implements ClassVisitor
{
    private final Clazz        exceptClass;
    private final ClassVisitor classVisitor;
    public ExceptClassFilter(Clazz        exceptClass,
                             ClassVisitor classVisitor)
    {
        this.exceptClass  = exceptClass;
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (!programClass.equals(exceptClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (!libraryClass.equals(exceptClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}