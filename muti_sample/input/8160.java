public final class CheckRegisterInLog_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void ping()"),
        new java.rmi.server.Operation("void shutdown()")
    };
    private static final long interfaceHash = 10333549859256328L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_ping_0;
    private static java.lang.reflect.Method $method_shutdown_1;
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
            $method_ping_0 = ActivateMe.class.getMethod("ping", new java.lang.Class[] {});
            $method_shutdown_1 = ActivateMe.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public CheckRegisterInLog_Stub() {
        super();
    }
    public CheckRegisterInLog_Stub(java.rmi.server.RemoteRef ref) {
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
    public void shutdown()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            ref.invoke(this, $method_shutdown_1, null, -7207851917985848402L);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        }
    }
}
