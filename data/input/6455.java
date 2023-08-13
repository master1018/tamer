public final class EchoImpl_Stub
    extends java.rmi.server.RemoteStub
    implements Echo, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_echoNot_0;
    private static java.lang.reflect.Method $method_shutdown_1;
    static {
        try {
            $method_echoNot_0 = Echo.class.getMethod("echoNot", new java.lang.Class[] {byte[].class});
            $method_shutdown_1 = Echo.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public EchoImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public byte[] echoNot(byte[] $param_arrayOf_byte_1)
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_echoNot_0, new java.lang.Object[] {$param_arrayOf_byte_1}, -4295721514897591756L);
            return ((byte[]) $result);
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
