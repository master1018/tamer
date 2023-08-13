public final class ShutdownImpl_Stub
    extends java.rmi.server.RemoteStub
    implements Shutdown
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_shutdown_0;
    static {
        try {
            $method_shutdown_0 = Shutdown.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ShutdownImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void shutdown()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_shutdown_0, null, -7207851917985848402L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
