package proguard.classfile.visitor;
import proguard.classfile.*;
public class ImplementedClassFilter implements ClassVisitor
{
    private final Clazz        implementedClass;
    private final ClassVisitor classVisitor;
    public ImplementedClassFilter(Clazz        implementedClass,
                                  ClassVisitor classVisitor)
    {
        this.implementedClass = implementedClass;
        this.classVisitor     = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (!programClass.extendsOrImplements(implementedClass))
        {
            classVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (!libraryClass.extendsOrImplements(implementedClass))
        {
            classVisitor.visitLibraryClass(libraryClass);
        }
    }
}