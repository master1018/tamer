public final class ModifyDescriptor_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("java.rmi.activation.ActivationID getID()"),
        new java.rmi.server.Operation("java.lang.String getMessage()"),
        new java.rmi.server.Operation("java.lang.String getProperty(java.lang.String)"),
        new java.rmi.server.Operation("void shutdown()")
    };
    private static final long interfaceHash = 7998207954486691383L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_getID_0;
    private static java.lang.reflect.Method $method_getMessage_1;
    private static java.lang.reflect.Method $method_getProperty_2;
    private static java.lang.reflect.Method $method_shutdown_3;
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
            $method_getID_0 = ActivateMe.class.getMethod("getID", new java.lang.Class[] {});
            $method_getMessage_1 = ActivateMe.class.getMethod("getMessage", new java.lang.Class[] {});
            $method_getProperty_2 = ActivateMe.class.getMethod("getProperty", new java.lang.Class[] {java.lang.String.class});
            $method_shutdown_3 = ActivateMe.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public ModifyDescriptor_Stub() {
        super();
    }
    public ModifyDescriptor_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.rmi.activation.ActivationID getID()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getID_0, null, -7795865521150345044L);
                return ((java.rmi.activation.ActivationID) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
                ref.invoke(call);
                java.rmi.activation.ActivationID $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (java.rmi.activation.ActivationID) in.readObject();
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
    public java.lang.String getMessage()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getMessage_1, null, 5353407034680111516L);
                return ((java.lang.String) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
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
    public java.lang.String getProperty(java.lang.String $param_String_1)
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getProperty_2, new java.lang.Object[] {$param_String_1}, 77249282285080913L);
                return ((java.lang.String) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
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
    public void shutdown()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            ref.invoke(this, $method_shutdown_3, null, -7207851917985848402L);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 3, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        }
    }
}
