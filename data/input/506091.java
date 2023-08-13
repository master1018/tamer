import proguard.classfile.*;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.info.*;
public class OptimizationInfoMemberFilter
implements   MemberVisitor
{
    private final MemberVisitor memberVisitor;
    public OptimizationInfoMemberFilter(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (FieldOptimizationInfo.getFieldOptimizationInfo(programField) != null)
        {
            memberVisitor.visitProgramField(programClass, programField);
        }
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        if (FieldOptimizationInfo.getFieldOptimizationInfo(libraryField) != null)
        {
            memberVisitor.visitLibraryField(libraryClass, libraryField);
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (MethodOptimizationInfo.getMethodOptimizationInfo(programMethod) != null)
        {
            memberVisitor.visitProgramMethod(programClass, programMethod);
        }
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (MethodOptimizationInfo.getMethodOptimizationInfo(libraryMethod) != null)
        {
            memberVisitor.visitLibraryMethod(libraryClass, libraryMethod);
        }
    }
}
