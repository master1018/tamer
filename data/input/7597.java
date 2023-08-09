public class DynSequenceImpl extends DynAnyCollectionImpl implements DynSequence
{
    private DynSequenceImpl() {
        this(null, (Any)null, false);
    }
    protected DynSequenceImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynSequenceImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
    protected boolean initializeComponentsFromAny() {
        TypeCode typeCode = any.type();
        int length;
        TypeCode contentType = getContentType();
        InputStream input;
        try {
            input = any.create_input_stream();
        } catch (BAD_OPERATION e) {
            return false;
        }
        length = input.read_long();
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
        components = new DynAny[0];
        anys = new Any[0];
        return true;
    }
    protected boolean initializeAnyFromComponents() {
        OutputStream out = any.create_output_stream();
        out.write_long(components.length);
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof DynAnyImpl) {
                ((DynAnyImpl)components[i]).writeAny(out);
            } else {
                components[i].to_any().write_value(out);
            }
        }
        any.read_value(out.create_input_stream(), any.type());
        return true;
    }
    public int get_length() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? components.length : 0);
    }
    public void set_length(int len)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        int bound = getBound();
        if (bound > 0 && len > bound) {
            throw new InvalidValue();
        }
        checkInitComponents();
        int oldLength = components.length;
        if (len > oldLength) {
            DynAny[] newComponents = new DynAny[len];
            Any[] newAnys = new Any[len];
            System.arraycopy(components, 0, newComponents, 0, oldLength);
            System.arraycopy(anys, 0, newAnys, 0, oldLength);
            components = newComponents;
            anys = newAnys;
            TypeCode contentType = getContentType();
            for (int i=oldLength; i<len; i++) {
                createDefaultComponentAt(i, contentType);
            }
            if (index == NO_INDEX)
                index = oldLength;
        } else if (len < oldLength) {
            DynAny[] newComponents = new DynAny[len];
            Any[] newAnys = new Any[len];
            System.arraycopy(components, 0, newComponents, 0, len);
            System.arraycopy(anys, 0, newAnys, 0, len);
            components = newComponents;
            anys = newAnys;
            if (len == 0 || index >= len) {
                index = NO_INDEX;
            }
        } else {
            if (index == NO_INDEX && len > 0) {
                index = 0;
            }
        }
    }
    protected void checkValue(Object[] value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (value == null || value.length == 0) {
            clearData();
            index = NO_INDEX;
            return;
        } else {
            index = 0;
        }
        int bound = getBound();
        if (bound > 0 && value.length > bound) {
            throw new InvalidValue();
        }
    }
}
