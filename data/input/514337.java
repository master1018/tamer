import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
final class SubclassedClassFilter
implements ClassVisitor
{
    private final ClassVisitor classVisitor;
    public SubclassedClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (programClass.subClasses != null)
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (libraryClass.subClasses != null)
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}
