package proguard.classfile.visitor;
import proguard.classfile.*;
public class SubclassTraveler implements ClassVisitor
{
    private final ClassVisitor classVisitor;
    public SubclassTraveler(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.subclassesAccept(classVisitor);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.subclassesAccept(classVisitor);
    }
}