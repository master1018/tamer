package proguard.classfile.visitor;
import proguard.classfile.*;
public class SubclassFilter implements ClassVisitor
{
    private final Clazz        subclass;
    private final ClassVisitor classVisitor;
    public SubclassFilter(Clazz        subclass,
                          ClassVisitor classVisitor)
    {
        this.subclass     = subclass;
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (!present(programClass.subClasses))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (!present(libraryClass.subClasses))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
    private boolean present(Clazz[] subclasses)
    {
        if (subclasses == null)
        {
            return false;
        }
        for (int index = 0; index < subclasses.length; index++)
        {
            if (subclasses[index].equals(subclass))
            {
                return true;
            }
        }
        return false;
    }
}