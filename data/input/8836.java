abstract class DynAnyCollectionImpl extends DynAnyConstructedImpl
{
    Any[] anys = null;
    private DynAnyCollectionImpl() {
        this(null, (Any)null, false);
    }
    protected DynAnyCollectionImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynAnyCollectionImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
    protected void createDefaultComponentAt(int i, TypeCode contentType) {
        try {
            components[i] = DynAnyUtil.createMostDerivedDynAny(contentType, orb);
        } catch (InconsistentTypeCode itc) { 
        }
        anys[i] = getAny(components[i]);
    }
    protected TypeCode getContentType() {
        try {
            return any.type().content_type();
        } catch (BadKind badKind) { 
            return null;
        }
    }
    protected int getBound() {
        try {
            return any.type().length();
        } catch (BadKind badKind) { 
            return 0;
        }
    }
    public org.omg.CORBA.Any[] get_elements () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? anys : null);
    }
    protected abstract void checkValue(Object[] value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;
    public void set_elements (org.omg.CORBA.Any[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkValue(value);
        components = new DynAny[value.length];
        anys = value;
        TypeCode expectedTypeCode = getContentType();
        for (int i=0; i<value.length; i++) {
            if (value[i] != null) {
                if (! value[i].type().equal(expectedTypeCode)) {
                    clearData();
                    throw new TypeMismatch();
                }
                try {
                    components[i] = DynAnyUtil.createMostDerivedDynAny(value[i], orb, false);
                } catch (InconsistentTypeCode itc) {
                    throw new InvalidValue();
                }
            } else {
                clearData();
                throw new InvalidValue();
            }
        }
        index = (value.length == 0 ? NO_INDEX : 0);
        representations = REPRESENTATION_COMPONENTS;
    }
    public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? components : null);
    }
    public void set_elements_as_dyn_any (org.omg.DynamicAny.DynAny[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkValue(value);
        components = (value == null ? emptyComponents : value);
        anys = new Any[value.length];
        TypeCode expectedTypeCode = getContentType();
        for (int i=0; i<value.length; i++) {
            if (value[i] != null) {
                if (! value[i].type().equal(expectedTypeCode)) {
                    clearData();
                    throw new TypeMismatch();
                }
                anys[i] = getAny(value[i]);
            } else {
                clearData();
                throw new InvalidValue();
            }
        }
        index = (value.length == 0 ? NO_INDEX : 0);
        representations = REPRESENTATION_COMPONENTS;
    }
}
