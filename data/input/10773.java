public class DynAnyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.DynamicAny.DynAnyFactory
{
    private ORB orb;
    private DynAnyFactoryImpl() {
        this.orb = null;
    }
    public DynAnyFactoryImpl(ORB orb) {
        this.orb = orb;
    }
    public org.omg.DynamicAny.DynAny create_dyn_any (org.omg.CORBA.Any any)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        return DynAnyUtil.createMostDerivedDynAny(any, orb, true);
    }
    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code (org.omg.CORBA.TypeCode type)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        return DynAnyUtil.createMostDerivedDynAny(type, orb);
    }
    private String[] __ids = { "IDL:omg.org/DynamicAny/DynAnyFactory:1.0" };
    public String[] _ids() {
        return __ids;
    }
}
