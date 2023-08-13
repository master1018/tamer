package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
public class MethodImplementationFilter
extends      SimplifiedVisitor
implements   MemberVisitor
{
    private final MemberVisitor memberVisitor;
    public MethodImplementationFilter(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (programClass.mayHaveImplementations(programMethod))
        {
            memberVisitor.visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (libraryClass.mayHaveImplementations(libraryMethod))
        {
            memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
}
