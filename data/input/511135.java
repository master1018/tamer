import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class UsedClassFilter
implements   ClassVisitor
{
    private final UsageMarker  usageMarker;
    private final ClassVisitor classVisitor;
    public UsedClassFilter(UsageMarker  usageMarker,
                           ClassVisitor classVisitor)
    {
        this.usageMarker  = usageMarker;
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (usageMarker.isUsed(programClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (usageMarker.isUsed(libraryClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}
