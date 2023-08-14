public final class KeepAliveDuringCall_Stub
    extends java.rmi.server.RemoteStub
    implements ShutdownMonitor
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_declareStillAlive_0;
    private static java.lang.reflect.Method $method_submitShutdown_1;
    static {
        try {
            $method_declareStillAlive_0 = ShutdownMonitor.class.getMethod("declareStillAlive", new java.lang.Class[] {});
            $method_submitShutdown_1 = ShutdownMonitor.class.getMethod("submitShutdown", new java.lang.Class[] {Shutdown.class});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public KeepAliveDuringCall_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void declareStillAlive()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_declareStillAlive_0, null, -1562228924246272634L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void submitShutdown(Shutdown $param_Shutdown_1)
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_submitShutdown_1, new java.lang.Object[] {$param_Shutdown_1}, 7574258166120515108L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
