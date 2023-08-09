package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class ClassSubHierarchyInitializer
implements   ClassVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        addSubclass(programClass, programClass.getSuperClass());
        for (int index = 0; index < programClass.u2interfacesCount; index++)
        {
            addSubclass(programClass, programClass.getInterface(index));
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        addSubclass(libraryClass, libraryClass.superClass);
        Clazz[] interfaceClasses = libraryClass.interfaceClasses;
        if (interfaceClasses != null)
        {
            for (int index = 0; index < interfaceClasses.length; index++)
            {
                addSubclass(libraryClass, interfaceClasses[index]);
            }
        }
    }
    private void addSubclass(Clazz subclass, Clazz clazz)
    {
        if (clazz != null)
        {
            clazz.addSubClass(subclass);
        }
    }
}
