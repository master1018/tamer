public final class ActivatableImpl_Stub
    extends java.rmi.server.RemoteStub
    implements MyRMI, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_classLoaderOk_0;
    private static java.lang.reflect.Method $method_shutdown_1;
    static {
        try {
            $method_classLoaderOk_0 = MyRMI.class.getMethod("classLoaderOk", new java.lang.Class[] {});
            $method_shutdown_1 = MyRMI.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ActivatableImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public boolean classLoaderOk()
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_classLoaderOk_0, null, 5226188865994330896L);
            return ((java.lang.Boolean) $result).booleanValue();
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void shutdown()
        throws java.lang.Exception
    {
        ref.invoke(this, $method_shutdown_1, null, -7207851917985848402L);
    }
}
