package proguard.classfile.visitor;
import proguard.classfile.*;
public class ConcreteClassDownTraveler
implements   ClassVisitor
{
    private final ClassVisitor classVisitor;
    public ConcreteClassDownTraveler(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if ((programClass.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_INTERFACE |
              ClassConstants.INTERNAL_ACC_ABSTRACT)) != 0)
        {
            Clazz[] subClasses = programClass.subClasses;
            if (subClasses != null)
            {
                for (int index = 0; index < subClasses.length; index++)
                {
                    subClasses[index].accept(this);
                }
            }
        }
        else
        {
            programClass.accept(classVisitor);
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if ((libraryClass.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_INTERFACE |
              ClassConstants.INTERNAL_ACC_ABSTRACT)) != 0)
        {
            Clazz[] subClasses = libraryClass.subClasses;
            if (subClasses != null)
            {
                for (int index = 0; index < subClasses.length; index++)
                {
                    subClasses[index].accept(this);
                }
            }
        }
        else
        {
            libraryClass.accept(classVisitor);
        }
    }
}
