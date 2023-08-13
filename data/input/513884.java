package proguard.classfile.visitor;
import proguard.classfile.*;
public interface MemberVisitor
{
    public void visitProgramField( ProgramClass programClass, ProgramField  programField);
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod);
    public void visitLibraryField( LibraryClass libraryClass, LibraryField  libraryField);
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod);
}
