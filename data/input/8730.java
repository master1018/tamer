public final class Impl2_Stub
    extends java.rmi.server.RemoteStub
    implements Pong
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_pong_0;
    static {
        try {
            $method_pong_0 = Pong.class.getMethod("pong", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public Impl2_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void pong()
        throws PongException, java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_pong_0, null, -5941237182396893426L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (PongException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
