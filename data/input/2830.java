public final class RestartLatecomer_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_callback_0;
    private static java.lang.reflect.Method $method_ping_1;
    private static java.lang.reflect.Method $method_shutdown_2;
    static {
        try {
            $method_callback_0 = ActivateMe.class.getMethod("callback", new java.lang.Class[] {java.lang.String.class});
            $method_ping_1 = ActivateMe.class.getMethod("ping", new java.lang.Class[] {});
            $method_shutdown_2 = ActivateMe.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public RestartLatecomer_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void callback(java.lang.String $param_String_1)
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_callback_0, new java.lang.Object[] {$param_String_1}, -1016900954059279373L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void ping()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_ping_1, null, 5866401369815527589L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void shutdown()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_shutdown_2, null, -7207851917985848402L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
