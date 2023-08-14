public final class UnregisterGroup_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void justGoAway()"),
        new java.rmi.server.Operation("void ping()"),
        new java.rmi.server.Operation("void shutdown()"),
        new java.rmi.server.Operation("void unregister()")
    };
    private static final long interfaceHash = -4733924075192691630L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_justGoAway_0;
    private static java.lang.reflect.Method $method_ping_1;
    private static java.lang.reflect.Method $method_shutdown_2;
    private static java.lang.reflect.Method $method_unregister_3;
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
            $method_justGoAway_0 = ActivateMe.class.getMethod("justGoAway", new java.lang.Class[] {});
            $method_ping_1 = ActivateMe.class.getMethod("ping", new java.lang.Class[] {});
            $method_shutdown_2 = ActivateMe.class.getMethod("shutdown", new java.lang.Class[] {});
            $method_unregister_3 = ActivateMe.class.getMethod("unregister", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public UnregisterGroup_Stub() {
        super();
    }
    public UnregisterGroup_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void justGoAway()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_justGoAway_0, null, -5382478058620783904L);
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
    public void ping()
        throws java.rmi.RemoteException
    {
        try {
            if (useNewInvoke) {
                ref.invoke(this, $method_ping_1, null, 5866401369815527589L);
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
    public void shutdown()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            ref.invoke(this, $method_shutdown_2, null, -7207851917985848402L);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        }
    }
    public void unregister()
        throws java.lang.Exception
    {
        if (useNewInvoke) {
            ref.invoke(this, $method_unregister_3, null, -5366864281862648102L);
        } else {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 3, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        }
    }
}
