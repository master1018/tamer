package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
public class ArrayElementValue extends ElementValue
{
    public int            u2elementValuesCount;
    public ElementValue[] elementValues;
    public ArrayElementValue()
    {
    }
    public ArrayElementValue(int            u2elementNameIndex,
                             int            u2elementValuesCount,
                             ElementValue[] elementValues)
    {
        super(u2elementNameIndex);
        this.u2elementValuesCount = u2elementValuesCount;
        this.elementValues        = elementValues;
    }
    public int getTag()
    {
        return ClassConstants.ELEMENT_VALUE_ARRAY;
    }
    public void accept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitArrayElementValue(clazz, annotation, this);
    }
    public void elementValuesAccept(Clazz clazz, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        for (int index = 0; index < u2elementValuesCount; index++)
        {
            elementValues[index].accept(clazz, annotation, elementValueVisitor);
        }
    }
}
