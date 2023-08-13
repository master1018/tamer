package proguard.classfile;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.visitor.*;
public class ProgramMethod extends ProgramMember implements Method
{
    public Clazz[] referencedClasses;
    public ProgramMethod()
    {
    }
    public ProgramMethod(int         u2accessFlags,
                         int         u2nameIndex,
                         int         u2descriptorIndex,
                         int         u2attributesCount,
                         Attribute[] attributes,
                         Clazz[]     referencedClasses)
    {
        super(u2accessFlags, u2nameIndex, u2descriptorIndex, u2attributesCount, attributes);
        this.referencedClasses = referencedClasses;
    }
    public void accept(ProgramClass programClass, MemberVisitor memberVisitor)
    {
        memberVisitor.visitProgramMethod(programClass, this);
    }
    public void attributesAccept(ProgramClass programClass, AttributeVisitor attributeVisitor)
    {
        for (int index = 0; index < u2attributesCount; index++)
        {
            attributes[index].accept(programClass, this, attributeVisitor);
        }
    }
    public void referencedClassesAccept(ClassVisitor classVisitor)
    {
        if (referencedClasses != null)
        {
            for (int index = 0; index < referencedClasses.length; index++)
            {
                if (referencedClasses[index] != null)
                {
                    referencedClasses[index].accept(classVisitor);
                }
            }
        }
    }
}
