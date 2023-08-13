package proguard.classfile.visitor;
import proguard.classfile.*;
public class ClassAccessFilter implements ClassVisitor
{
    private final int          requiredSetAccessFlags;
    private final int          requiredUnsetAccessFlags;
    private final ClassVisitor classVisitor;
    public ClassAccessFilter(int          requiredSetAccessFlags,
                             int          requiredUnsetAccessFlags,
                             ClassVisitor classVisitor)
    {
        this.requiredSetAccessFlags   = requiredSetAccessFlags;
        this.requiredUnsetAccessFlags = requiredUnsetAccessFlags;
        this.classVisitor             = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (accepted(programClass.getAccessFlags()))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (accepted(libraryClass.getAccessFlags()))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
    private boolean accepted(int accessFlags)
    {
        return (requiredSetAccessFlags   & ~accessFlags) == 0 &&
               (requiredUnsetAccessFlags &  accessFlags) == 0;
    }
}
