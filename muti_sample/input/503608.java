import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class InnerUsageMarker
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InnerClassesInfoVisitor,
             ConstantVisitor,
             ClassVisitor
{
    private final UsageMarker usageMarker;
    private boolean attributeUsed;
    private boolean classUsed;
    public InnerUsageMarker(UsageMarker usageMarker)
    {
        this.usageMarker = usageMarker;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        attributeUsed = false;
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
        if (attributeUsed)
        {
            usageMarker.markAsUsed(innerClassesAttribute);
            markConstant(clazz, innerClassesAttribute.u2attributeNameIndex);
        }
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        boolean innerClassesInfoUsed = usageMarker.isUsed(innerClassesInfo);
        if (!innerClassesInfoUsed)
        {
            classUsed = true;
            innerClassesInfo.innerClassConstantAccept(clazz, this);
            innerClassesInfoUsed = classUsed;
            classUsed = true;
            innerClassesInfo.outerClassConstantAccept(clazz, this);
            innerClassesInfoUsed &= classUsed;
            if (innerClassesInfoUsed)
            {
                usageMarker.markAsUsed(innerClassesInfo);
                innerClassesInfo.innerNameConstantAccept(clazz, this);
            }
        }
        attributeUsed |= innerClassesInfoUsed;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classUsed = usageMarker.isUsed(classConstant);
        if (!classUsed)
        {
            classUsed = true;
            classConstant.referencedClassAccept(this);
            if (classUsed)
            {
                usageMarker.markAsUsed(classConstant);
                markConstant(clazz, classConstant.u2nameIndex);
            }
        }
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        usageMarker.markAsUsed(utf8Constant);
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        classUsed = usageMarker.isUsed(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        classUsed = true;
    }
    private void markConstant(Clazz clazz, int index)
    {
         clazz.constantPoolEntryAccept(index, this);
    }
}
