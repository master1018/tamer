public class DynArrayImpl extends DynAnyCollectionImpl implements DynArray
{
    private DynArrayImpl() {
        this(null, (Any)null, false);
    }
    protected DynArrayImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynArrayImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
    protected boolean initializeComponentsFromAny() {
        TypeCode typeCode = any.type();
        int length = getBound();
        TypeCode contentType = getContentType();
        InputStream input;
        try {
            input = any.create_input_stream();
        } catch (BAD_OPERATION e) {
            return false;
        }
        components = new DynAny[length];
        anys = new Any[length];
        for (int i=0; i<length; i++) {
            anys[i] = DynAnyUtil.extractAnyFromStream(contentType, input, orb);
            try {
                components[i] = DynAnyUtil.createMostDerivedDynAny(anys[i], orb, false);
            } catch (InconsistentTypeCode itc) { 
            }
        }
        return true;
    }
    protected boolean initializeComponentsFromTypeCode() {
        TypeCode typeCode = any.type();
        int length = getBound();
        TypeCode contentType = getContentType();
        components = new DynAny[length];
        anys = new Any[length];
        for (int i=0; i<length; i++) {
            createDefaultComponentAt(i, contentType);
        }
        return true;
    }
    protected void checkValue(Object[] value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (value == null || value.length != getBound()) {
            throw new InvalidValue();
        }
    }
}
