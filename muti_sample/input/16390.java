public class DynStructImpl extends DynAnyComplexImpl implements DynStruct
{
    private DynStructImpl() {
        this(null, (Any)null, false);
    }
    protected DynStructImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynStructImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        index = 0;
    }
    public org.omg.DynamicAny.NameValuePair[] get_members () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkInitComponents();
        return nameValuePairs;
    }
    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkInitComponents();
        return nameDynAnyPairs;
    }
}
