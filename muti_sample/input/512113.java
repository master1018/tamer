import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class SuperInvocationMarker
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor
{
    private boolean invokesSuperMethods;
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        if (constantInstruction.opcode == InstructionConstants.OP_INVOKESPECIAL)
        {
            invokesSuperMethods = false;
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
            if (invokesSuperMethods)
            {
                setInvokesSuperMethods(method);
            }
        }
    }
    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant refConstant)
    {
        invokesSuperMethods =
            !clazz.equals(refConstant.referencedClass) &&
            !refConstant.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }
    private static void setInvokesSuperMethods(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setInvokesSuperMethods();
        }
    }
    public static boolean invokesSuperMethods(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null || info.invokesSuperMethods();
    }
}
