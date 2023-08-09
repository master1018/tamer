public final class ActivateFails_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void ping()"),
        new java.rmi.server.Operation("ShutdownThread shutdown()")
    };
    private static final long interfaceHash = -6632667923281093978L;
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
    public ActivateFails_Stub() {
        super();
    }
    public ActivateFails_Stub(java.rmi.server.RemoteRef ref) {
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
    public ShutdownThread shutdown()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            Object $result = ref.invoke(this, $method_shutdown_1, null, -3616843253114182719L);
            return ((ShutdownThread) $result);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
            ref.invoke(call);
            ShutdownThread $result;
            try {
                java.io.ObjectInput in = call.getInputStream();
                $result = (ShutdownThread) in.readObject();
            } catch (java.io.IOException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling return", e);
            } catch (java.lang.ClassNotFoundException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling return", e);
            } finally {
                ref.done(call);
            }
            return $result;
        }
    }
}
