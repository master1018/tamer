public final class ServerStackTrace_Stub
    extends java.rmi.server.RemoteStub
    implements Ping
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_ping_0;
    static {
        try {
            $method_ping_0 = Ping.class.getMethod("ping", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ServerStackTrace_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void ping()
        throws PingException, java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_ping_0, null, 5866401369815527589L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (PingException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
