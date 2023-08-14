public final class FooReceiverImpl_Stub
    extends java.rmi.server.RemoteStub
    implements DownloadParameterClass. FooReceiver
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_receiveFoo_0;
    static {
        try {
            $method_receiveFoo_0 = DownloadParameterClass. FooReceiver.class.getMethod("receiveFoo", new java.lang.Class[] {java.lang.Object.class});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public FooReceiverImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void receiveFoo(java.lang.Object $param_Object_1)
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_receiveFoo_0, new java.lang.Object[] {$param_Object_1}, -1548895758515635945L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
