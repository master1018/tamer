public class LocalObject implements org.omg.CORBA.Object
{
    private static String reason = "This is a locally constrained object.";
    public LocalObject() {}
    public boolean _is_equivalent(org.omg.CORBA.Object that) {
        return equals(that) ;
    }
    public boolean _non_existent() {
        return false;
    }
    public int _hash(int maximum) {
        return hashCode() ;
    }
    public boolean _is_a(String repository_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.Object _duplicate() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public void _release() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public Request _request(String operation) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result,
                                   ExceptionList exceptions,
                                   ContextList contexts) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.Object _get_interface()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.Object _get_interface_def()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.ORB _orb() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.Policy _get_policy(int policy_type) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.DomainManager[] _get_domain_managers() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public org.omg.CORBA.Object
        _set_policy_override(org.omg.CORBA.Policy[] policies,
                             org.omg.CORBA.SetOverrideType set_add) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public boolean _is_local() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public ServantObject _servant_preinvoke(String operation,
                                            Class expectedType) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public void _servant_postinvoke(ServantObject servant) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public OutputStream _request(String operation,
                                 boolean responseExpected) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public InputStream _invoke(OutputStream output)
        throws ApplicationException, RemarshalException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public void _releaseReply(InputStream input) {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
    public boolean validate_connection() {
        throw new org.omg.CORBA.NO_IMPLEMENT(reason);
    }
}
