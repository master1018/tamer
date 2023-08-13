import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class SideEffectMethodMarker
extends      SimplifiedVisitor
implements   ClassPoolVisitor,
             ClassVisitor,
             MemberVisitor,
             AttributeVisitor
{
    private final SideEffectInstructionChecker sideEffectInstructionChecker = new SideEffectInstructionChecker(false);
    private int     newSideEffectCount;
    private boolean hasSideEffects;
    public void visitClassPool(ClassPool classPool)
    {
        do
        {
            newSideEffectCount = 0;
            classPool.classesAccept(this);
        }
        while (newSideEffectCount > 0);
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.methodsAccept(this);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (!hasSideEffects(programMethod) &&
            !NoSideEffectMethodMarker.hasNoSideEffects(programMethod))
        {
            hasSideEffects =
                (programMethod.getAccessFlags() &
                 (ClassConstants.INTERNAL_ACC_NATIVE |
                  ClassConstants.INTERNAL_ACC_SYNCHRONIZED)) != 0;
            if (!hasSideEffects)
            {
                programMethod.attributesAccept(programClass, this);
            }
            if (hasSideEffects)
            {
                markSideEffects(programMethod);
                newSideEffectCount++;
            }
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        hasSideEffects = hasSideEffects(clazz, method, codeAttribute);
    }
    private boolean hasSideEffects(Clazz         clazz,
                                   Method        method,
                                   CodeAttribute codeAttribute)
    {
        byte[] code   = codeAttribute.code;
        int    length = codeAttribute.u4codeLength;
        int offset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(code, offset);
            if (sideEffectInstructionChecker.hasSideEffects(clazz,
                                                            method,
                                                            codeAttribute,
                                                            offset,
                                                            instruction))
            {
                return true;
            }
            offset += instruction.length(offset);
        }
        while (offset < length);
        return false;
    }
    private static void markSideEffects(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        if (info != null)
        {
            info.setSideEffects();
        }
    }
    public static boolean hasSideEffects(Method method)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(method);
        return info == null ||
               info.hasSideEffects();
    }
}
