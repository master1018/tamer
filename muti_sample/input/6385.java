public final class Callback_Stub
    extends java.rmi.server.RemoteStub
    implements CallbackInterface, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("int getNumDeactivated()"),
        new java.rmi.server.Operation("void inc()")
    };
    private static final long interfaceHash = -1008194523112388035L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_getNumDeactivated_0;
    private static java.lang.reflect.Method $method_inc_1;
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
            $method_getNumDeactivated_0 = CallbackInterface.class.getMethod("getNumDeactivated", new java.lang.Class[] {});
            $method_inc_1 = CallbackInterface.class.getMethod("inc", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public Callback_Stub() {
        super();
    }
    public Callback_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public int getNumDeactivated()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getNumDeactivated_0, null, -761062487639949912L);
                return ((java.lang.Integer) $result).intValue();
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
                ref.invoke(call);
                int $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = in.readInt();
                } catch (java.io.IOException e) {
                    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
                } finally {
                    ref.done(call);
                }
                return $result;
            }
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void inc()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_inc_1, null, 4394985085384332959L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
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
