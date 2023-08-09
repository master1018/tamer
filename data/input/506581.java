package proguard.classfile;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.visitor.*;
public class ProgramField extends ProgramMember implements Field
{
    public Clazz referencedClass;
    public ProgramField()
    {
    }
    public ProgramField(int         u2accessFlags,
                        int         u2nameIndex,
                        int         u2descriptorIndex,
                        int         u2attributesCount,
                        Attribute[] attributes,
                        Clazz       referencedClass)
    {
        super(u2accessFlags, u2nameIndex, u2descriptorIndex, u2attributesCount, attributes);
        this.referencedClass = referencedClass;
    }
    public void accept(ProgramClass programClass, MemberVisitor memberVisitor)
    {
        memberVisitor.visitProgramField(programClass, this);
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
        if (referencedClass != null)
        {
            referencedClass.accept(classVisitor);
        }
    }
}
