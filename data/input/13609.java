public final class UseCustomRef_Stub
    extends java.rmi.server.RemoteStub
    implements Ping, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void ping()"),
        new java.rmi.server.Operation("void receiveAndPing(Ping)")
    };
    private static final long interfaceHash = 8457085322718440165L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_ping_0;
    private static java.lang.reflect.Method $method_receiveAndPing_1;
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
            $method_ping_0 = Ping.class.getMethod("ping", new java.lang.Class[] {});
            $method_receiveAndPing_1 = Ping.class.getMethod("receiveAndPing", new java.lang.Class[] {Ping.class});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public UseCustomRef_Stub() {
        super();
    }
    public UseCustomRef_Stub(java.rmi.server.RemoteRef ref) {
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
    public void receiveAndPing(Ping $param_Ping_1)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_receiveAndPing_1, new java.lang.Object[] {$param_Ping_1}, -5964458762870933304L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
                try {
                    java.io.ObjectOutput out = call.getOutputStream();
                    out.writeObject($param_Ping_1);
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
