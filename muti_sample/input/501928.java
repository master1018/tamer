import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.util.SimplifiedVisitor;
public class PackageVisibleMemberInvokingClassMarker
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        Clazz referencedClass = refConstant.referencedClass;
        if (referencedClass != null &&
            (referencedClass.getAccessFlags() &
             ClassConstants.INTERNAL_ACC_PUBLIC) == 0)
        {
            setInvokesPackageVisibleMembers(clazz);
        }
        Member referencedMember = refConstant.referencedMember;
        if (referencedMember != null &&
            (referencedMember.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_PUBLIC |
              ClassConstants.INTERNAL_ACC_PRIVATE)) == 0)
        {
            setInvokesPackageVisibleMembers(clazz);
        }
    }
    private static void setInvokesPackageVisibleMembers(Clazz clazz)
    {
        ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
        if (info != null)
        {
            info.setInvokesPackageVisibleMembers();
        }
    }
    public static boolean invokesPackageVisibleMembers(Clazz clazz)
    {
        ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
        return info == null || info.invokesPackageVisibleMembers();
    }
}