public class DynValueImpl extends DynValueCommonImpl implements DynValue
{
    private DynValueImpl() {
        this(null, (Any)null, false);
    }
    protected DynValueImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }
    protected DynValueImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
}
