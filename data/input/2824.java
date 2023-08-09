public final class DGCAckFailure_Stub
    extends java.rmi.server.RemoteStub
    implements ReturnRemote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_returnRemote_0;
    static {
        try {
            $method_returnRemote_0 = ReturnRemote.class.getMethod("returnRemote", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public DGCAckFailure_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.lang.Object returnRemote()
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_returnRemote_0, null, -8981544221566403070L);
            return ((java.lang.Object) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
