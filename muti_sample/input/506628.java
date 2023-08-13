package proguard.classfile.visitor;
import proguard.classfile.*;
public class ClassVersionFilter implements ClassVisitor
{
    private final int          minimumClassVersion;
    private final int          maximumClassVersion;
    private final ClassVisitor classVisitor;
    public ClassVersionFilter(int          minimumClassVersion,
                              int          maximumClassVersion,
                              ClassVisitor classVisitor)
    {
        this.minimumClassVersion = minimumClassVersion;
        this.maximumClassVersion = maximumClassVersion;
        this.classVisitor        = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (programClass.u4version >= minimumClassVersion &&
            programClass.u4version <= maximumClassVersion)
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
}
