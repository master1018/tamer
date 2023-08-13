package proguard.classfile.attribute.preverification;
public class VerificationTypeFactory
{
    static final IntegerType           INTEGER_TYPE            = new IntegerType();
    static final LongType              LONG_TYPE               = new LongType();
    static final FloatType             FLOAT_TYPE              = new FloatType();
    static final DoubleType            DOUBLE_TYPE             = new DoubleType();
    static final TopType               TOP_TYPE                = new TopType();
    static final NullType              NULL_TYPE               = new NullType();
    static final UninitializedThisType UNINITIALIZED_THIS_TYPE = new UninitializedThisType();
    public static IntegerType createIntegerType()
    {
        return INTEGER_TYPE;
    }
    public static LongType createLongType()
    {
        return LONG_TYPE;
    }
    public static FloatType createFloatType()
    {
        return FLOAT_TYPE;
    }
    public static DoubleType createDoubleType()
    {
        return DOUBLE_TYPE;
    }
    public static TopType createTopType()
    {
        return TOP_TYPE;
    }
    public static NullType createNullType()
    {
        return NULL_TYPE;
    }
    public static UninitializedThisType createUninitializedThisType()
    {
        return UNINITIALIZED_THIS_TYPE;
    }
    public static UninitializedType createUninitializedType(int newInstructionOffset)
    {
        return new UninitializedType(newInstructionOffset);
    }
    public static ObjectType createObjectType(int classIndex)
    {
        return new ObjectType(classIndex);
    }
}
