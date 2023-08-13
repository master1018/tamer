import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
import proguard.optimize.KeepMarker;
public class MemberOptimizationInfoSetter
extends      SimplifiedVisitor
implements   MemberVisitor
{
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (!KeepMarker.isKept(programMethod))
        {
            MethodOptimizationInfo.setMethodOptimizationInfo(programClass,
                                                             programMethod);
        }
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (!KeepMarker.isKept(programField))
        {
            FieldOptimizationInfo.setFieldOptimizationInfo(programClass,
                                                           programField);
        }
    }
}
