import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.KeepMarker;
public class ClassFinalizer
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final ClassVisitor extraClassVisitor;
    public ClassFinalizer()
    {
        this(null);
    }
    public ClassFinalizer(ClassVisitor  extraClassVisitor)
    {
        this.extraClassVisitor = extraClassVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if ((programClass.u2accessFlags & (ClassConstants.INTERNAL_ACC_FINAL     |
                                           ClassConstants.INTERNAL_ACC_INTERFACE |
                                           ClassConstants.INTERNAL_ACC_ABSTRACT)) == 0 &&
            !KeepMarker.isKept(programClass)                                           &&
            programClass.subClasses == null)
        {
            programClass.u2accessFlags |= ClassConstants.INTERNAL_ACC_FINAL;
            if (extraClassVisitor != null)
            {
                extraClassVisitor.visitProgramClass(programClass);
            }
        }
    }
}
