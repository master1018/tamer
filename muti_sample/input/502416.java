import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class RetargetedInnerClassAttributeRemover
extends      SimplifiedVisitor
implements   ClassVisitor,
             AttributeVisitor,
             InnerClassesInfoVisitor,
             ConstantVisitor
{
    private boolean retargeted;
    public void visitProgramClass(ProgramClass programClass)
    {
        int         attributesCount = programClass.u2attributesCount;
        Attribute[] attributes      = programClass.attributes;
        int newAtributesCount = 0;
        for (int index = 0; index < attributesCount; index++)
        {
            Attribute attribute = attributes[index];
            retargeted = false;
            attribute.accept(programClass, this);
            if (!retargeted)
            {
                attributes[newAtributesCount++] = attribute;
            }
        }
        for (int index = newAtributesCount; index < attributesCount; index++)
        {
            attributes[index] = null;
        }
        programClass.u2attributesCount = newAtributesCount;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        checkTarget(clazz);
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        checkTarget(clazz);
        checkTarget(enclosingMethodAttribute.referencedClass);
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        innerClassesInfo.innerClassConstantAccept(clazz, this);
        innerClassesInfo.outerClassConstantAccept(clazz, this);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        checkTarget(classConstant.referencedClass);
    }
    private void checkTarget(Clazz clazz)
    {
        if (clazz != null &&
            ClassMerger.getTargetClass(clazz) != null)
        {
            retargeted = true;
        }
    }
}