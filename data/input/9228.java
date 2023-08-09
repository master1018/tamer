public final class MyRemoteObject_Stub
    extends java.rmi.server.RemoteStub
    implements MyRemoteInterface, java.rmi.Remote
{
    private static java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void ping()")
    };
    private static final long interfaceHash = -8409781791984809394L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_ping_0;
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
            $method_ping_0 = MyRemoteInterface.class.getMethod("ping", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public MyRemoteObject_Stub() {
        super();
    }
    public MyRemoteObject_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void ping()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_ping_0, null, 5866401369815527589L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
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
