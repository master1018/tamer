package proguard.classfile.visitor;
import proguard.classfile.*;
public class MemberCounter implements MemberVisitor
{
    private int count;
    public int getCount()
    {
        return count;
    }
    public void visitLibraryField(LibraryClass libraryClass,
                                  LibraryField libraryField)
    {
        count++;
    }
    public void visitLibraryMethod(LibraryClass libraryClass,
                                   LibraryMethod libraryMethod)
    {
        count++;
    }
    public void visitProgramField(ProgramClass programClass,
                                  ProgramField programField)
    {
        count++;
    }
    public void visitProgramMethod(ProgramClass programClass,
                                   ProgramMethod programMethod)
    {
        count++;
    }
}
