public final class TestImpl_Stub
    extends java.rmi.server.RemoteStub
    implements Test, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("java.lang.String echo(java.lang.String)")
    };
    private static final long interfaceHash = 8975711176019764637L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_echo_0;
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
            $method_echo_0 = Test.class.getMethod("echo", new java.lang.Class[] {java.lang.String.class});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public TestImpl_Stub() {
        super();
    }
    public TestImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.lang.String echo(java.lang.String $param_String_1)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_echo_0, new java.lang.Object[] {$param_String_1}, 5525131960618330777L);
                return ((java.lang.String) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
                try {
                    java.io.ObjectOutput out = call.getOutputStream();
                    out.writeObject($param_String_1);
                } catch (java.io.IOException e) {
                    throw new java.rmi.MarshalException("error marshalling arguments", e);
                }
                ref.invoke(call);
                java.lang.String $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (java.lang.String) in.readObject();
                } catch (java.io.IOException e) {
                    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
                } catch (java.lang.ClassNotFoundException e) {
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
}
