public final class MarshalAfterUnexport2_Stub
    extends java.rmi.server.RemoteStub
    implements Receiver, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_receive_0;
    static {
        try {
            $method_receive_0 = Receiver.class.getMethod("receive", new java.lang.Class[] {java.rmi.Remote.class});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public MarshalAfterUnexport2_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void receive(java.rmi.Remote $param_Remote_1)
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_receive_0, new java.lang.Object[] {$param_Remote_1}, 5876293363550629411L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
