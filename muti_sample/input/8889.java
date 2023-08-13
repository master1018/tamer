public final class CheckUnmarshalOnStopThread_Stub
    extends java.rmi.server.RemoteStub
    implements CheckUnmarshal, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("PoisonPill getPoisonPill()"),
        new java.rmi.server.Operation("void passRuntimeExceptionParameter(RuntimeExceptionParameter)"),
        new java.rmi.server.Operation("java.lang.Object ping()")
    };
    private static final long interfaceHash = -5923540687975666490L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_getPoisonPill_0;
    private static java.lang.reflect.Method $method_passRuntimeExceptionParameter_1;
    private static java.lang.reflect.Method $method_ping_2;
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
            $method_getPoisonPill_0 = CheckUnmarshal.class.getMethod("getPoisonPill", new java.lang.Class[] {});
            $method_passRuntimeExceptionParameter_1 = CheckUnmarshal.class.getMethod("passRuntimeExceptionParameter", new java.lang.Class[] {RuntimeExceptionParameter.class});
            $method_ping_2 = CheckUnmarshal.class.getMethod("ping", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public CheckUnmarshalOnStopThread_Stub() {
        super();
    }
    public CheckUnmarshalOnStopThread_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public PoisonPill getPoisonPill()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getPoisonPill_0, null, 5776441251039617360L);
                return ((PoisonPill) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
                ref.invoke(call);
                PoisonPill $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (PoisonPill) in.readObject();
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
    public void passRuntimeExceptionParameter(RuntimeExceptionParameter $param_RuntimeExceptionParameter_1)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_passRuntimeExceptionParameter_1, new java.lang.Object[] {$param_RuntimeExceptionParameter_1}, -4427599990679364365L);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
                try {
                    java.io.ObjectOutput out = call.getOutputStream();
                    out.writeObject($param_RuntimeExceptionParameter_1);
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
    public java.lang.Object ping()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_ping_2, null, 7635508643486276040L);
                return ((java.lang.Object) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
                ref.invoke(call);
                java.lang.Object $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (java.lang.Object) in.readObject();
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
