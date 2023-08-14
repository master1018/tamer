import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class BackwardBranchMarker
extends      SimplifiedVisitor
implements   InstructionVisitor
{
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        markBackwardBranch(method, branchInstruction.branchOffset);
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        markBackwardBranch(method, switchInstruction.defaultOffset);
        for (int index = 0; index < switchInstruction.jumpOffsets.length; index++)
        {
            markBackwardBranch(method, switchInstruction.jumpOffsets[index]);
        }
    }
    private void markBackwardBranch(Method method, int branchOffset)
    {
        if (branchOffset < 0)
        {
            setBranchesBackward(method);
        }
    }
    private static void setBranchesBackward(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setBranchesBackward();
        }
    }
    public static boolean branchesBackward(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null || info.branchesBackward();
    }
}
