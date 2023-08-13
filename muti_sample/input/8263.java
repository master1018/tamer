public final class ForceLogSnapshot_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void crash()"),
        new java.rmi.server.Operation("void ping(int, java.lang.String)")
    };
    private static final long interfaceHash = -5865767584502007357L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_crash_0;
    private static java.lang.reflect.Method $method_ping_1;
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
            $method_crash_0 = ActivateMe.class.getMethod("crash", new java.lang.Class[] {});
            $method_ping_1 = ActivateMe.class.getMethod("ping", new java.lang.Class[] {int.class, java.lang.String.class});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public ForceLogSnapshot_Stub() {
        super();
    }
    public ForceLogSnapshot_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void crash()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            ref.invoke(this, $method_crash_0, null, 8484760490859430950L);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        }
    }
    public void ping(int $param_int_1, java.lang.String $param_String_2)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_ping_1, new java.lang.Object[] {new java.lang.Integer($param_int_1), $param_String_2}, -1519179153769139224L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
                try {
                    java.io.ObjectOutput out = call.getOutputStream();
                    out.writeInt($param_int_1);
                    out.writeObject($param_String_2);
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
