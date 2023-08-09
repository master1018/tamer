public final class DownloadArrayClass_Stub
    extends java.rmi.server.RemoteStub
    implements Receiver, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void receive(java.lang.Object)")
    };
    private static final long interfaceHash = -953299374608818732L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_receive_0;
    static {
        try {
            java.rmi.server.RemoteRef.class.getMethod("invoke",
                new java.lang.Class[] {
                    java.rmi.Remote.class,
                    java.lang.reflect.Method.class,
                    java.lang.Object[].class,
                    long.class
                });
            useNewInvoke = true;
            $method_receive_0 = Receiver.class.getMethod("receive", new java.lang.Class[] {java.lang.Object.class});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public DownloadArrayClass_Stub() {
        super();
    }
    public DownloadArrayClass_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void receive(java.lang.Object $param_Object_1)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_receive_0, new java.lang.Object[] {$param_Object_1}, -578858472643205929L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
                try {
                    java.io.ObjectOutput out = call.getOutputStream();
                    out.writeObject($param_Object_1);
                } catch (java.io.IOException e) {
                    throw new java.rmi.MarshalException("error marshalling arguments", e);
                }
                ref.invoke(call);
                ref.done(call);
            }
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
