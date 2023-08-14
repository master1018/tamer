import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
public class PackageVisibleMemberContainingClassMarker
extends      SimplifiedVisitor
implements   MemberVisitor
{
    public void visitAnyMember(Clazz clazz, Member member)
    {
        if ((member.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_PRIVATE |
              ClassConstants.INTERNAL_ACC_PUBLIC)) == 0)
        {
            setPackageVisibleMembers(clazz);
        }
    }
    private static void setPackageVisibleMembers(Clazz clazz)
    {
        ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
        if (info != null)
        {
            info.setContainsPackageVisibleMembers();
        }
    }
    public static boolean containsPackageVisibleMembers(Clazz clazz)
    {
        ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
        return info == null || info.containsPackageVisibleMembers();
    }
}