import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
public class MethodInvocationMarker
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor,
             MemberVisitor
{
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.referencedMemberAccept(this);
    }
    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant refConstant)
    {
        refConstant.referencedMemberAccept(this);
    }
    public void visitAnyMember(Clazz Clazz, Member member) {}
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        incrementInvocationCount(programMethod);
    }
    private static void incrementInvocationCount(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.incrementInvocationCount();
        }
    }
    public static int getInvocationCount(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info != null ? info.getInvocationCount() :
                              Integer.MAX_VALUE;
    }
}
