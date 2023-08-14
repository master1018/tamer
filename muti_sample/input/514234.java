import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class CaughtClassFilter
implements   ClassVisitor
{
    private final ClassVisitor classVisitor;
    public CaughtClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (CaughtClassMarker.isCaught(programClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (CaughtClassMarker.isCaught(libraryClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}