package proguard.classfile.attribute.annotation;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
public class ConstantElementValue extends ElementValue
{
    public final int u1tag;
    public       int u2constantValueIndex;
    public ConstantElementValue(int u1tag)
    {
        this.u1tag = u1tag;
    }
    public ConstantElementValue(int u1tag,
                                int u2elementNameIndex,
                                int u2constantValueIndex)
    {
        super(u2elementNameIndex);
        this.u1tag                = u1tag;
        this.u2constantValueIndex = u2constantValueIndex;
    }
    public int getTag()
    {
        return u1tag;
    }
    public void accept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitConstantElementValue(clazz, annotation, this);
    }
}
