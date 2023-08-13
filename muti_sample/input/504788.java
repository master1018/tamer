import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
public class ValueFactory
{
    static final IntegerValue INTEGER_VALUE = new UnknownIntegerValue();
    static final LongValue    LONG_VALUE    = new UnknownLongValue();
    static final FloatValue   FLOAT_VALUE   = new UnknownFloatValue();
    static final DoubleValue  DOUBLE_VALUE  = new UnknownDoubleValue();
    static final ReferenceValue REFERENCE_VALUE_NULL                        = new ReferenceValue(null, null, true);
    static final ReferenceValue REFERENCE_VALUE_JAVA_LANG_OBJECT_MAYBE_NULL = new ReferenceValue(ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT, null, true);
    static final ReferenceValue REFERENCE_VALUE_JAVA_LANG_OBJECT_NOT_NULL   = new ReferenceValue(ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT, null, false);
    public Value createValue(String type, Clazz referencedClass, boolean mayBeNull)
    {
        switch (type.charAt(0))
        {
            case ClassConstants.INTERNAL_TYPE_VOID:    return null;
            case ClassConstants.INTERNAL_TYPE_BOOLEAN:
            case ClassConstants.INTERNAL_TYPE_BYTE:
            case ClassConstants.INTERNAL_TYPE_CHAR:
            case ClassConstants.INTERNAL_TYPE_SHORT:
            case ClassConstants.INTERNAL_TYPE_INT:     return createIntegerValue();
            case ClassConstants.INTERNAL_TYPE_LONG:    return createLongValue();
            case ClassConstants.INTERNAL_TYPE_FLOAT:   return createFloatValue();
            case ClassConstants.INTERNAL_TYPE_DOUBLE:  return createDoubleValue();
            default:                                   return createReferenceValue(ClassUtil.isInternalArrayType(type) ?
                                                                                       type :
                                                                                       ClassUtil.internalClassNameFromClassType(type),
                                                                                   referencedClass,
                                                                                   mayBeNull);
        }
    }
    public IntegerValue createIntegerValue()
    {
        return INTEGER_VALUE;
    }
    public IntegerValue createIntegerValue(int value)
    {
        return createIntegerValue();
    }
    public LongValue createLongValue()
    {
        return LONG_VALUE;
    }
    public LongValue createLongValue(long value)
    {
        return createLongValue();
    }
    public FloatValue createFloatValue()
    {
        return FLOAT_VALUE;
    }
    public FloatValue createFloatValue(float value)
    {
        return createFloatValue();
    }
    public DoubleValue createDoubleValue()
    {
        return DOUBLE_VALUE;
    }
    public DoubleValue createDoubleValue(double value)
    {
        return createDoubleValue();
    }
    public ReferenceValue createReferenceValueNull()
    {
        return REFERENCE_VALUE_NULL;
    }
    public ReferenceValue createReferenceValue(String  type,
                                               Clazz   referencedClass,
                                               boolean mayBeNull)
    {
        return type == null                                                ? REFERENCE_VALUE_NULL                                 :
               !type.equals(ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT) ? new ReferenceValue(type, referencedClass, mayBeNull) :
               mayBeNull                                                   ? REFERENCE_VALUE_JAVA_LANG_OBJECT_MAYBE_NULL          :
                                                                             REFERENCE_VALUE_JAVA_LANG_OBJECT_NOT_NULL;
    }
    public ReferenceValue createArrayReferenceValue(String       type,
                                                    Clazz        referencedClass,
                                                    IntegerValue arrayLength)
    {
        return createArrayReferenceValue(type,
                                         referencedClass,
                                         arrayLength,
                                         createValue(type, referencedClass, false));
    }
    public ReferenceValue createArrayReferenceValue(String       type,
                                                    Clazz        referencedClass,
                                                    IntegerValue arrayLength,
                                                    Value        elementValue)
    {
        return createReferenceValue(ClassConstants.INTERNAL_TYPE_ARRAY + type,
                                    referencedClass,
                                    false);
    }
}
