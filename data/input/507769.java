package proguard.classfile.visitor;
import proguard.classfile.*;
public interface ClassVisitor
{
    public void visitProgramClass(ProgramClass programClass);
    public void visitLibraryClass(LibraryClass libraryClass);
}
