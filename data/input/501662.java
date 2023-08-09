import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class InstanceofClassFilter
implements   ClassVisitor
{
    private final ClassVisitor classVisitor;
    public InstanceofClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (InstanceofClassMarker.isInstanceofed(programClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (InstanceofClassMarker.isInstanceofed(libraryClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}