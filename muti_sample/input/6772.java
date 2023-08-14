abstract class DynAnyImpl extends org.omg.CORBA.LocalObject implements DynAny
{
    protected static final int NO_INDEX = -1;
    protected static final byte STATUS_DESTROYABLE = 0;
    protected static final byte STATUS_UNDESTROYABLE = 1;
    protected static final byte STATUS_DESTROYED = 2;
    protected ORB orb = null;
    protected ORBUtilSystemException wrapper ;
    protected Any any = null;
    protected byte status = STATUS_DESTROYABLE;
    protected int index = NO_INDEX;
    protected DynAnyImpl() {
        wrapper = ORBUtilSystemException.get(
            CORBALogDomains.RPC_PRESENTATION ) ;
    }
    protected DynAnyImpl(ORB orb, Any any, boolean copyValue) {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PRESENTATION ) ;
        if (copyValue)
            this.any = DynAnyUtil.copy(any, orb);
        else
            this.any = any;
        index = NO_INDEX;
    }
    protected DynAnyImpl(ORB orb, TypeCode typeCode) {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PRESENTATION ) ;
        this.any = DynAnyUtil.createDefaultAnyOfType(typeCode, orb);
    }
    protected DynAnyFactory factory() {
        try {
            return (DynAnyFactory)orb.resolve_initial_references(
                ORBConstants.DYN_ANY_FACTORY_NAME );
        } catch (InvalidName in) {
            throw new RuntimeException("Unable to find DynAnyFactory");
        }
    }
    protected Any getAny() {
        return any;
    }
    protected Any getAny(DynAny dynAny) {
        if (dynAny instanceof DynAnyImpl)
            return ((DynAnyImpl)dynAny).getAny();
        else
            return dynAny.to_any();
    }
    protected void writeAny(OutputStream out) {
        any.write_value(out);
    }
    protected void setStatus(byte newStatus) {
        status = newStatus;
    }
    protected void clearData() {
        any.type(any.type());
    }
    public org.omg.CORBA.TypeCode type() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return any.type();
    }
    public void assign (org.omg.DynamicAny.DynAny dyn_any)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(dyn_any.type()))) {
            throw new TypeMismatch();
        }
        any = dyn_any.to_any();
    }
    public void from_any (org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(value.type()))) {
            throw new TypeMismatch();
        }
        Any tempAny = null;
        try {
            tempAny = DynAnyUtil.copy(value, orb);
        } catch (Exception e) {
            throw new InvalidValue();
        }
        if ( ! DynAnyUtil.isInitialized(tempAny)) {
            throw new InvalidValue();
        }
        any = tempAny;
   }
    public abstract org.omg.CORBA.Any to_any();
    public abstract boolean equal (org.omg.DynamicAny.DynAny dyn_any);
    public abstract void destroy();
    public abstract org.omg.DynamicAny.DynAny copy();
    private String[] __ids = { "IDL:omg.org/DynamicAny/DynAny:1.0" };
    public String[] _ids() {
        return __ids;
    }
}
