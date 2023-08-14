package proguard.classfile;
import proguard.classfile.visitor.*;
public class LibraryField extends LibraryMember implements Field
{
    public Clazz referencedClass;
    public LibraryField()
    {
    }
    public LibraryField(int    u2accessFlags,
                        String name,
                        String descriptor)
    {
        super(u2accessFlags, name, descriptor);
    }
    public void accept(LibraryClass libraryClass, MemberVisitor memberVisitor)
    {
        memberVisitor.visitLibraryField(libraryClass, this);
    }
    public void referencedClassesAccept(ClassVisitor classVisitor)
    {
        if (referencedClass != null)
        {
            referencedClass.accept(classVisitor);
        }
    }
}
