public final class RegistryRunner_Stub
    extends java.rmi.server.RemoteStub
    implements RemoteExiter, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_exit_0;
    static {
        try {
            $method_exit_0 = RemoteExiter.class.getMethod("exit", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public RegistryRunner_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void exit()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_exit_0, null, -6307240473358936408L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
