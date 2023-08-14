package proguard.classfile.attribute;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class SourceFileAttribute extends Attribute
{
    public int u2sourceFileIndex;
    public SourceFileAttribute()
    {
    }
    public SourceFileAttribute(int u2attributeNameIndex,
                               int u2sourceFileIndex)
    {
        super(u2attributeNameIndex);
        this.u2sourceFileIndex = u2sourceFileIndex;
    }
    public void accept(Clazz clazz, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitSourceFileAttribute(clazz, this);
    }
}
