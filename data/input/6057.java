public abstract class ServerRequest {
    @Deprecated
    public String op_name()
    {
        return operation();
    }
    public String operation()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    @Deprecated
    public void params(NVList params)
    {
        arguments(params);
    }
    public void arguments(org.omg.CORBA.NVList args) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    @Deprecated
    public void result(Any any)
    {
        set_result(any);
    }
    public void set_result(org.omg.CORBA.Any any)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    @Deprecated
    public void except(Any any)
    {
        set_exception(any);
    }
    public void set_exception(Any any)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public abstract Context ctx();
}
