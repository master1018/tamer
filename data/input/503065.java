package proguard.classfile.attribute;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class SourceDirAttribute extends Attribute
{
    public int u2sourceDirIndex;
    public SourceDirAttribute()
    {
    }
    public SourceDirAttribute(int u2attributeNameIndex,
                              int u2sourceDirIndex)
    {
        super(u2attributeNameIndex);
        this.u2sourceDirIndex = u2sourceDirIndex;
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSourceDirAttribute(clazz, this);
    }
}
