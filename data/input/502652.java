import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class CatchExceptionMarker
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (codeAttribute.u2exceptionTableLength > 0)
        {
            markCatchException(method);
        }
    }
    private static void markCatchException(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setCatchesExceptions();
        }
    }
    public static boolean catchesExceptions(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null ||
               info.catchesExceptions();
    }
}
