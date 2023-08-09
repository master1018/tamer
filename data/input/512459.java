package proguard.classfile.attribute;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.visitor.*;
public class InnerClassesAttribute extends Attribute
{
    public int                u2classesCount;
    public InnerClassesInfo[] classes;
    public InnerClassesAttribute()
    {
    }
    public InnerClassesAttribute(int                u2attributeNameIndex,
                                 int                u2classesCount,
                                 InnerClassesInfo[] classes)
    {
        super(u2attributeNameIndex);
        this.u2classesCount = u2classesCount;
        this.classes        = classes;
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitInnerClassesAttribute(clazz, this);
    }
    public void innerClassEntriesAccept(Clazz clazz, InnerClassesInfoVisitor innerClassesInfoVisitor)
    {
        for (int index = 0; index < u2classesCount; index++)
        {
            innerClassesInfoVisitor.visitInnerClassesInfo(clazz, classes[index]);
        }
    }
}
