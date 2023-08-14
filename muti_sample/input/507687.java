package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class SubclassToAdder
implements   ClassVisitor
{
    private final Clazz targetClass;
    public SubclassToAdder(Clazz targetClass)
    {
        this.targetClass = targetClass;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        targetClass.addSubClass(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        targetClass.addSubClass(libraryClass);
    }
}