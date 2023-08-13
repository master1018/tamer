package proguard.classfile.visitor;
import proguard.classfile.*;
public class ClassPresenceFilter implements ClassVisitor
{
    private final ClassPool    classPool;
    private final ClassVisitor presentClassVisitor;
    private final ClassVisitor missingClassVisitor;
    public ClassPresenceFilter(ClassPool    classPool,
                               ClassVisitor presentClassVisitor,
                               ClassVisitor missingClassVisitor)
    {
        this.classPool           = classPool;
        this.presentClassVisitor = presentClassVisitor;
        this.missingClassVisitor = missingClassVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        ClassVisitor classFileVisitor = classFileVisitor(programClass);
        if (classFileVisitor != null)
        {
            classFileVisitor.visitProgramClass(programClass);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        ClassVisitor classFileVisitor = classFileVisitor(libraryClass);
        if (classFileVisitor != null)
        {
            classFileVisitor.visitLibraryClass(libraryClass);
        }
    }
    private ClassVisitor classFileVisitor(Clazz clazz)
    {
        return classPool.getClass(clazz.getName()) != null ?
            presentClassVisitor :
            missingClassVisitor;
    }
}
