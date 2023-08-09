import proguard.classfile.*;
import proguard.classfile.util.MethodLinker;
import proguard.evaluation.value.*;
public class FieldOptimizationInfo
{
    private static final SpecificValueFactory VALUE_FACTORY = new SpecificValueFactory();
    private boolean        isWritten;
    private boolean        isRead;
    private boolean        canBeMadePrivate = true;
    private ReferenceValue referencedClass;
    private Value          value;
    public FieldOptimizationInfo(Clazz clazz, Field field)
    {
        isWritten =
        isRead    = (field.getAccessFlags() & ClassConstants.INTERNAL_ACC_VOLATILE) != 0;
        value     = initialValue(field.getDescriptor(clazz));
    }
    public void setWritten()
    {
        isWritten = true;
    }
    public boolean isWritten()
    {
        return isWritten;
    }
    public void setRead()
    {
        isRead = true;
    }
    public boolean isRead()
    {
        return isRead;
    }
    public void setCanNotBeMadePrivate()
    {
        canBeMadePrivate = false;
    }
    public boolean canBeMadePrivate()
    {
        return canBeMadePrivate;
    }
    public void generalizeReferencedClass(ReferenceValue referencedClass)
    {
        this.referencedClass = this.referencedClass != null ?
            this.referencedClass.generalize(referencedClass) :
            referencedClass;
    }
    public ReferenceValue getReferencedClass()
    {
        return referencedClass;
    }
    public void generalizeValue(Value value)
    {
        this.value = this.value != null ?
            this.value.generalize(value) :
            value;
    }
    public Value getValue()
    {
        return value;
    }
    private Value initialValue(String type)
    {
        switch (type.charAt(0))
        {
            case ClassConstants.INTERNAL_TYPE_BOOLEAN:
            case ClassConstants.INTERNAL_TYPE_BYTE:
            case ClassConstants.INTERNAL_TYPE_CHAR:
            case ClassConstants.INTERNAL_TYPE_SHORT:
            case ClassConstants.INTERNAL_TYPE_INT:
                return VALUE_FACTORY.createIntegerValue(0);
            case ClassConstants.INTERNAL_TYPE_LONG:
                return VALUE_FACTORY.createLongValue(0L);
            case ClassConstants.INTERNAL_TYPE_FLOAT:
                return VALUE_FACTORY.createFloatValue(0.0f);
            case ClassConstants.INTERNAL_TYPE_DOUBLE:
                return VALUE_FACTORY.createDoubleValue(0.0);
            case ClassConstants.INTERNAL_TYPE_CLASS_START:
            case ClassConstants.INTERNAL_TYPE_ARRAY:
                return VALUE_FACTORY.createReferenceValueNull();
            default:
                throw new IllegalArgumentException("Invalid type ["+type+"]");
        }
    }
    public static void setFieldOptimizationInfo(Clazz clazz, Field field)
    {
        MethodLinker.lastMember(field).setVisitorInfo(new FieldOptimizationInfo(clazz, field));
    }
    public static FieldOptimizationInfo getFieldOptimizationInfo(Field field)
    {
        Object visitorInfo = MethodLinker.lastMember(field).getVisitorInfo();
        return visitorInfo instanceof FieldOptimizationInfo ?
            (FieldOptimizationInfo)visitorInfo :
            null;
    }
}
