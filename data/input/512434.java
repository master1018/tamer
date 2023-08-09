import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
public class NameFactoryResetter implements ClassVisitor
{
    private final NameFactory nameFactory;
    public NameFactoryResetter(NameFactory nameFactory)
    {
        this.nameFactory = nameFactory;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        nameFactory.reset();
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        nameFactory.reset();
    }
}
