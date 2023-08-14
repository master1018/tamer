public final class ContextImpl extends Context {
    private org.omg.CORBA.ORB _orb;
    private ORBUtilSystemException wrapper ;
    public ContextImpl(org.omg.CORBA.ORB orb)
    {
        _orb = orb;
        wrapper = ORBUtilSystemException.get(
            (com.sun.corba.se.spi.orb.ORB)orb,
            CORBALogDomains.RPC_PRESENTATION ) ;
    }
    public ContextImpl(Context parent)
    {
        throw wrapper.contextNotImplemented() ;
    }
    public String context_name()
    {
        throw wrapper.contextNotImplemented() ;
    }
    public Context parent()
    {
        throw wrapper.contextNotImplemented() ;
    }
    public Context create_child(String name)
    {
        throw wrapper.contextNotImplemented() ;
    }
    public void set_one_value(String propName, Any propValue)
    {
        throw wrapper.contextNotImplemented() ;
    }
    public void set_values(NVList values)
    {
        throw wrapper.contextNotImplemented() ;
    }
    public void delete_values(String propName)
    {
        throw wrapper.contextNotImplemented() ;
    }
    public NVList get_values(String startScope,
                             int opFlags,
                             String propName)
    {
        throw wrapper.contextNotImplemented() ;
    }
};
