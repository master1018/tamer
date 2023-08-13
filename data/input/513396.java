package proguard.classfile;
import proguard.classfile.visitor.*;
public class LibraryMethod extends LibraryMember implements Method
{
    public Clazz[] referencedClasses;
    public LibraryMethod()
    {
    }
    public LibraryMethod(int    u2accessFlags,
                         String name,
                         String descriptor)
    {
        super(u2accessFlags, name, descriptor);
    }
    public void accept(LibraryClass libraryClass, MemberVisitor memberVisitor)
    {
        memberVisitor.visitLibraryMethod(libraryClass, this);
    }
    public void referencedClassesAccept(ClassVisitor classVisitor)
    {
        if (referencedClasses != null)
        {
            for (int index = 0; index < referencedClasses.length; index++)
            {
                if (referencedClasses[index] != null)
                {
                    referencedClasses[index].accept(classVisitor);
                }
            }
        }
    }
}
