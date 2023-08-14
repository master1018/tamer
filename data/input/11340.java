public final class StubClassesPermitted_Stub
    extends java.rmi.server.RemoteStub
    implements CanCreateStubs, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("java.lang.Object getForbiddenClass()"),
        new java.rmi.server.Operation("java.rmi.registry.Registry getRegistry()"),
        new java.rmi.server.Operation("java.rmi.activation.ActivationGroupID returnGroupID()"),
        new java.rmi.server.Operation("void shutdown()")
    };
    private static final long interfaceHash = 1677779850431817575L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_getForbiddenClass_0;
    private static java.lang.reflect.Method $method_getRegistry_1;
    private static java.lang.reflect.Method $method_returnGroupID_2;
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
            $method_getForbiddenClass_0 = CanCreateStubs.class.getMethod("getForbiddenClass", new java.lang.Class[] {});
            $method_getRegistry_1 = CanCreateStubs.class.getMethod("getRegistry", new java.lang.Class[] {});
            $method_returnGroupID_2 = CanCreateStubs.class.getMethod("returnGroupID", new java.lang.Class[] {});
            $method_shutdown_3 = CanCreateStubs.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public StubClassesPermitted_Stub() {
        super();
    }
    public StubClassesPermitted_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.lang.Object getForbiddenClass()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            Object $result = ref.invoke(this, $method_getForbiddenClass_0, null, -658265783646674294L);
            return ((java.lang.Object) $result);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
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
    }
    public java.rmi.registry.Registry getRegistry()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_getRegistry_1, null, 255311215504696981L);
                return ((java.rmi.registry.Registry) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
                ref.invoke(call);
                java.rmi.registry.Registry $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (java.rmi.registry.Registry) in.readObject();
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
    public java.rmi.activation.ActivationGroupID returnGroupID()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                Object $result = ref.invoke(this, $method_returnGroupID_2, null, 6267304638191237098L);
                return ((java.rmi.activation.ActivationGroupID) $result);
            } else {
                java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
                ref.invoke(call);
                java.rmi.activation.ActivationGroupID $result;
                try {
                    java.io.ObjectInput in = call.getInputStream();
                    $result = (java.rmi.activation.ActivationGroupID) in.readObject();
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
