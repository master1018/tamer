package proguard.classfile.visitor;
import proguard.classfile.*;
public class ClassCounter implements ClassVisitor
{
    private int count;
    public int getCount()
    {
        return count;
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        count++;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        count++;
    }
}
