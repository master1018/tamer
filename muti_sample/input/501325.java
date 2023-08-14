import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class InstantiationClassFilter
implements   ClassVisitor
{
    private final ClassVisitor classVisitor;
    public InstantiationClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (InstantiationClassMarker.isInstantiated(programClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (InstantiationClassMarker.isInstantiated(libraryClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}