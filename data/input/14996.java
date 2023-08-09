public final class RetryDirtyCalls_Stub
    extends java.rmi.server.RemoteStub
    implements Self
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_getSelf_0;
    static {
        try {
            $method_getSelf_0 = Self.class.getMethod("getSelf", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public RetryDirtyCalls_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public Self getSelf()
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_getSelf_0, null, 2868857108246021904L);
            return ((Self) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
