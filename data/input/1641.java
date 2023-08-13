public class DynValueBoxImpl extends DynValueCommonImpl implements DynValueBox
{
    private DynValueBoxImpl() {
        this(null, (Any)null, false);
    }
    protected DynValueBoxImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynValueBoxImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
    public Any get_boxed_value()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitAny();
        return any;
    }
    public void set_boxed_value(org.omg.CORBA.Any boxed)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ! isNull && ! boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        any = boxed;
        representations = REPRESENTATION_ANY;
        index = 0;
        isNull = false;
    }
    public DynAny get_boxed_value_as_dyn_any()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return components[0];
    }
    public void set_boxed_value_as_dyn_any(DynAny boxed)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ! isNull && ! boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        components = new DynAny[] {boxed};
        representations = REPRESENTATION_COMPONENTS;
        index = 0;
        isNull = false;
    }
    protected boolean initializeComponentsFromAny() {
        try {
            components = new DynAny[] {DynAnyUtil.createMostDerivedDynAny(any, orb, false)};
        } catch (InconsistentTypeCode ictc) {
            return false; 
        }
        return true;
    }
    protected boolean initializeComponentsFromTypeCode() {
        try {
            any = DynAnyUtil.createDefaultAnyOfType(any.type(), orb);
            components = new DynAny[] {DynAnyUtil.createMostDerivedDynAny(any, orb, false)};
        } catch (InconsistentTypeCode ictc) {
            return false; 
        }
        return true;
    }
    protected boolean initializeAnyFromComponents() {
        any = getAny(components[0]);
        return true;
    }
}
