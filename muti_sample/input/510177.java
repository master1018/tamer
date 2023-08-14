import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
public class IdentifiedValueFactory
extends      SpecificValueFactory
{
    private int integerID;
    private int longID;
    private int floatID;
    private int doubleID;
    private int referenceID;
    public IntegerValue createIntegerValue()
    {
        return new IdentifiedIntegerValue(this, integerID++);
    }
    public LongValue createLongValue()
    {
        return new IdentifiedLongValue(this, longID++);
    }
    public FloatValue createFloatValue()
    {
        return new IdentifiedFloatValue(this, floatID++);
    }
    public DoubleValue createDoubleValue()
    {
        return new IdentifiedDoubleValue(this, doubleID++);
    }
    public ReferenceValue createReferenceValue(String  type,
                                               Clazz   referencedClass,
                                               boolean mayBeNull)
    {
        return type == null ?
            REFERENCE_VALUE_NULL :
            new IdentifiedReferenceValue(type, referencedClass, mayBeNull, this, referenceID++);
    }
}