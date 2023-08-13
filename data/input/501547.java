package proguard.classfile.visitor;
import proguard.classfile.*;
public class VariableClassVisitor implements ClassVisitor
{
    private ClassVisitor classVisitor;
    public VariableClassVisitor()
    {
        this(null);
    }
    public VariableClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void setClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public ClassVisitor getClassVisitor()
    {
        return classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (classVisitor != null)
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (classVisitor != null)
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}
