public class DynEnumImpl extends DynAnyBasicImpl implements DynEnum
{
    int currentEnumeratorIndex = NO_INDEX;
    private DynEnumImpl() {
        this(null, (Any)null, false);
    }
    protected DynEnumImpl(ORB orb, Any anAny, boolean copyValue) {
        super(orb, anAny, copyValue);
        index = NO_INDEX;
        try {
            currentEnumeratorIndex = any.extract_long();
        } catch (BAD_OPERATION e) {
            currentEnumeratorIndex = 0;
            any.type(any.type());
            any.insert_long(0);
        }
    }
    protected DynEnumImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        index = NO_INDEX;
        currentEnumeratorIndex = 0;
        any.insert_long(0);
    }
    private int memberCount() {
        int memberCount = 0;
        try {
            memberCount = any.type().member_count();
        } catch (BadKind bad) {
        }
        return memberCount;
    }
    private String memberName(int i) {
        String memberName = null;
        try {
            memberName = any.type().member_name(i);
        } catch (BadKind bad) {
        } catch (Bounds bounds) {
        }
        return memberName;
    }
    private int computeCurrentEnumeratorIndex(String value) {
        int memberCount = memberCount();
        for (int i=0; i<memberCount; i++) {
            if (memberName(i).equals(value)) {
                return i;
            }
        }
        return NO_INDEX;
    }
    public int component_count() {
        return 0;
    }
    public org.omg.DynamicAny.DynAny current_component()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        throw new TypeMismatch();
    }
    public String get_as_string () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return memberName(currentEnumeratorIndex);
    }
    public void set_as_string (String value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        int newIndex = computeCurrentEnumeratorIndex(value);
        if (newIndex == NO_INDEX) {
            throw new InvalidValue();
        }
        currentEnumeratorIndex = newIndex;
        any.insert_long(newIndex);
    }
    public int get_as_ulong () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return currentEnumeratorIndex;
    }
    public void set_as_ulong (int value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (value < 0 || value >= memberCount()) {
            throw new InvalidValue();
        }
        currentEnumeratorIndex = value;
        any.insert_long(value);
    }
}
