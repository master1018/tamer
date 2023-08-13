import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class InterfaceUsageMarker
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor
{
    private final UsageMarker usageMarker;
    private boolean used;
    private boolean anyUsed;
    public InterfaceUsageMarker(UsageMarker usageMarker)
    {
        this.usageMarker = usageMarker;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        boolean classUsed         = usageMarker.isUsed(programClass);
        boolean classPossiblyUsed = usageMarker.isPossiblyUsed(programClass);
        if (classUsed || classPossiblyUsed)
        {
            boolean oldAnyUsed = anyUsed;
            anyUsed = false;
            programClass.interfaceConstantsAccept(this);
            classUsed |= anyUsed;
            anyUsed = oldAnyUsed;
            if (classPossiblyUsed)
            {
                if (classUsed)
                {
                    usageMarker.markAsUsed(programClass);
                    programClass.thisClassConstantAccept(this);
                    programClass.superClassConstantAccept(this);
                }
                else
                {
                    usageMarker.markAsUnused(programClass);
                }
            }
        }
        used = classUsed;
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        used    = true;
        anyUsed = true;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        boolean classUsed = usageMarker.isUsed(classConstant);
        if (!classUsed)
        {
            classConstant.referencedClassAccept(this);
            classUsed = used;
            if (classUsed)
            {
                usageMarker.markAsUsed(classConstant);
                clazz.constantPoolEntryAccept(classConstant.u2nameIndex, this);
            }
        }
        used    =  classUsed;
        anyUsed |= classUsed;
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        if (!usageMarker.isUsed(utf8Constant))
        {
            usageMarker.markAsUsed(utf8Constant);
        }
    }
}
