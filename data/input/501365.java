import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class AccessMethodMarker
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor,
             ClassVisitor,
             MemberVisitor
{
    private Method invokingMethod;
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        invokingMethod = method;
        clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
       stringConstant.referencedClassAccept(this);
       stringConstant.referencedMemberAccept(this);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        clazz.constantPoolEntryAccept(refConstant.u2classIndex, this);
        refConstant.referencedClassAccept(this);
        refConstant.referencedMemberAccept(this);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
       classConstant.referencedClassAccept(this);
    }
    public void visitAnyClass(Clazz clazz)
    {
        int accessFlags = clazz.getAccessFlags();
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) == 0)
        {
            setAccessesPackageCode(invokingMethod);
        }
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        int accessFlags = member.getAccessFlags();
        if      ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE)   != 0)
        {
            setAccessesPrivateCode(invokingMethod);
        }
        else if ((accessFlags & ClassConstants.INTERNAL_ACC_PROTECTED) != 0)
        {
            setAccessesProtectedCode(invokingMethod);
        }
        else if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC)    == 0)
        {
            setAccessesPackageCode(invokingMethod);
        }
    }
    private static void setAccessesPrivateCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setAccessesPrivateCode();
        }
    }
    public static boolean accessesPrivateCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null || info.accessesPrivateCode();
    }
    private static void setAccessesPackageCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setAccessesPackageCode();
        }
    }
    public static boolean accessesPackageCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null || info.accessesPackageCode();
    }
    private static void setAccessesProtectedCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setAccessesProtectedCode();
        }
    }
    public static boolean accessesProtectedCode(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null || info.accessesProtectedCode();
    }
}
