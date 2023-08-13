package proguard.classfile.visitor;
import proguard.classfile.*;
public class ExceptClassesFilter implements ClassVisitor
{
    private final Clazz[]      exceptClasses;
    private final ClassVisitor classVisitor;
    public ExceptClassesFilter(Clazz[]      exceptClasses,
                               ClassVisitor classVisitor)
    {
        this.exceptClasses = exceptClasses;
        this.classVisitor  = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (!present(programClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (!present(libraryClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
    private boolean present(Clazz clazz)
    {
        if (exceptClasses == null)
        {
            return false;
        }
        for (int index = 0; index < exceptClasses.length; index++)
        {
            if (exceptClasses[index].equals(clazz))
            {
                return true;
            }
        }
        return false;
    }
}