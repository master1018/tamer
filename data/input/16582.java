public class CheckActivateRef
        extends Activatable
        implements ActivateMe, Runnable
{
    private CheckActivateRef(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        super(id, 0);
    }
    public void ping()
    {}
    public void shutdown() throws Exception
    {
        (new Thread(this,"CheckActivateRef")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args)  {
        Object dummy = new Object();
        RMID rmid = null;
        ActivateMe obj;
        int failures = 0;
        int i = 0;
        System.err.println("\nRegression test for bug 4105080\n");
        System.err.println("java.security.policy = " +
                           System.getProperty("java.security.policy",
                                              "no policy"));
        String propValue =
            System.getProperty("java.rmi.server.useDynamicProxies", "false");
        boolean useDynamicProxies = Boolean.parseBoolean(propValue);
        CheckActivateRef server;
        try {
            TestLibrary.suggestSecurityManager(TestParams.defaultSecurityManager);
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            p.put("java.rmi.server.useDynamicProxies", propValue);
            System.err.println("Create activation group in this VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            ActivationGroup.createGroup(groupID, groupDesc, 0);
            System.err.println("Creating descriptor");
            ActivationDesc desc =
                new ActivationDesc("CheckActivateRef", null, null);
            System.err.println("Registering descriptor");
            obj = (ActivateMe) Activatable.register(desc);
            System.err.println("proxy = " + obj);
            if (useDynamicProxies && !Proxy.isProxyClass(obj.getClass()))
            {
                throw new RuntimeException("proxy is not dynamic proxy");
            }
            try {
                for (; i < 7; i++) {
                    System.err.println("Activate object via method call");
                    try {
                        obj.ping();
                    } catch (RemoteException e) {
                        Exception detail = (Exception) e.detail;
                        if ((detail != null) &&
                            (detail instanceof ActivationException) &&
                            (detail.getMessage().equals("group is inactive")))
                        {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                            }
                            obj.ping();
                        } else {
                            throw e;
                        }
                    }
                    System.err.println("proxy = " + obj);
                    ActivatableRef aref;
                    if (obj instanceof RemoteStub) {
                        aref = (ActivatableRef) ((RemoteObject) obj).getRef();
                    } else if (Proxy.isProxyClass(obj.getClass())) {
                        RemoteObjectInvocationHandler handler =
                            (RemoteObjectInvocationHandler)
                            Proxy.getInvocationHandler(obj);
                        aref = (ActivatableRef) handler.getRef();
                    } else {
                        throw new RuntimeException("unknown proxy type");
                    }
                    final ActivatableRef ref = aref;
                    Field f = (Field)
                        java.security.AccessController.doPrivileged
                        (new java.security.PrivilegedExceptionAction() {
                            public Object run() throws Exception {
                                Field ff = ref.getClass().getDeclaredField("ref");
                                ff.setAccessible(true);
                                return ff;
                            }
                        });
                    Object insideRef = f.get(ref);
                    System.err.println("insideRef = " + insideRef);
                    if (insideRef instanceof ActivatableRef) {
                        TestLibrary.bomb("Embedded ref is an ActivatableRef");
                    } else {
                        System.err.println("ActivatableRef's embedded ref type: " +
                                           insideRef.getClass().getName());
                    }
                    System.err.println("Deactivate object via method call");
                    obj.shutdown();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (java.rmi.UnmarshalException ue) {
                if (ue.detail instanceof java.io.IOException) {
                    if ((failures ++) >= 3) {
                        throw ue;
                    }
                } else {
                    throw ue;
                }
            }
            System.err.println("\nsuccess: CheckActivateRef test passed ");
        } catch (java.rmi.activation.ActivationException e) {
            if (i < 4) {
                TestLibrary.bomb(e);
            }
        } catch (Exception e) {
            if (e instanceof java.security.PrivilegedActionException)
                e = ((java.security.PrivilegedActionException)e).getException();
            TestLibrary.bomb("\nfailure: unexpected exception " +
                             e.getClass().getName(), e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
            obj = null;
        }
    }
}
