package proguard.classfile.visitor;
import proguard.classfile.*;
public class MemberToClassVisitor implements MemberVisitor
{
    private final ClassVisitor classVisitor;
    private Clazz lastVisitedClass;
    public MemberToClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (!programClass.equals(lastVisitedClass))
        {
            classVisitor.visitProgramClass(programClass);
            lastVisitedClass = programClass;
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (!programClass.equals(lastVisitedClass))
        {
            classVisitor.visitProgramClass(programClass);
            lastVisitedClass = programClass;
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        if (!libraryClass.equals(lastVisitedClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
            lastVisitedClass = libraryClass;
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (!libraryClass.equals(lastVisitedClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
            lastVisitedClass = libraryClass;
        }
    }
}
