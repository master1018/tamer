abstract public class Servant {
    private transient Delegate _delegate = null;
    final public Delegate _get_delegate() {
        if (_delegate == null) {
            throw
                new
                org.omg.CORBA.BAD_INV_ORDER
                ("The Servant has not been associated with an ORB instance");
        }
        return _delegate;
    }
    final public void _set_delegate(Delegate delegate) {
        _delegate = delegate;
    }
    final public org.omg.CORBA.Object _this_object() {
        return _get_delegate().this_object(this);
    }
    final public org.omg.CORBA.Object _this_object(ORB orb) {
        try {
            ((org.omg.CORBA_2_3.ORB)orb).set_delegate(this);
        }
        catch(ClassCastException e) {
            throw
                new
                org.omg.CORBA.BAD_PARAM
                ("POA Servant requires an instance of org.omg.CORBA_2_3.ORB");
        }
        return _this_object();
    }
    final public ORB _orb() {
        return _get_delegate().orb(this);
    }
    final public POA _poa() {
        return _get_delegate().poa(this);
    }
    final public byte[] _object_id() {
        return _get_delegate().object_id(this);
    }
    public POA _default_POA() {
        return _get_delegate().default_POA(this);
    }
    public boolean _is_a(String repository_id) {
        return _get_delegate().is_a(this, repository_id);
    }
    public boolean _non_existent() {
        return _get_delegate().non_existent(this);
    }
    public org.omg.CORBA.Object _get_interface_def()
    {
        org.omg.PortableServer.portable.Delegate delegate = _get_delegate();
        try {
            return delegate.get_interface_def(this);
        } catch( AbstractMethodError aex ) {
            try {
                Class[] argc = { org.omg.PortableServer.Servant.class };
                java.lang.reflect.Method meth =
                     delegate.getClass().getMethod("get_interface", argc);
                Object[] argx = { this };
                return (org.omg.CORBA.Object)meth.invoke(delegate, argx);
            } catch( java.lang.reflect.InvocationTargetException exs ) {
                Throwable t = exs.getTargetException();
                if (t instanceof Error) {
                    throw (Error) t;
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
            } catch( RuntimeException rex ) {
                throw rex;
            } catch( Exception exr ) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        }
    }
    abstract public String[] _all_interfaces( POA poa, byte[] objectId);
}
