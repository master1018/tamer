public class CorbaUtils {
    public static org.omg.CORBA.Object remoteToCorba(Remote remoteObj, ORB orb)
        throws ClassNotFoundException, ConfigurationException {
            synchronized (CorbaUtils.class) {
                if (toStubMethod == null) {
                    initMethodHandles();
                }
            }
            java.lang.Object stub;
            try {
                stub = toStubMethod.invoke(null, new java.lang.Object[]{remoteObj});
            } catch (InvocationTargetException e) {
                Throwable realException = e.getTargetException();
                ConfigurationException ce = new ConfigurationException(
    "Problem with PortableRemoteObject.toStub(); object not exported or stub not found");
                ce.setRootCause(realException);
                throw ce;
            } catch (IllegalAccessException e) {
                ConfigurationException ce = new ConfigurationException(
    "Cannot invoke javax.rmi.PortableRemoteObject.toStub(java.rmi.Remote)");
                ce.setRootCause(e);
                throw ce;
            }
            if (!corbaStubClass.isInstance(stub)) {
                return null;  
            }
            try {
                connectMethod.invoke(stub, new java.lang.Object[]{orb});
            } catch (InvocationTargetException e) {
                Throwable realException = e.getTargetException();
                if (!(realException instanceof java.rmi.RemoteException)) {
                    ConfigurationException ce = new ConfigurationException(
                        "Problem invoking javax.rmi.CORBA.Stub.connect()");
                    ce.setRootCause(realException);
                    throw ce;
                }
            } catch (IllegalAccessException e) {
                ConfigurationException ce = new ConfigurationException(
                    "Cannot invoke javax.rmi.CORBA.Stub.connect()");
                ce.setRootCause(e);
                throw ce;
            }
            return (org.omg.CORBA.Object)stub;
    }
    public static ORB getOrb(String server, int port, Hashtable env) {
        Properties orbProp;
        if (env != null) {
            if (env instanceof Properties) {
                orbProp = (Properties) env.clone();
            } else {
                Enumeration envProp;
                orbProp = new Properties();
                for (envProp = env.keys(); envProp.hasMoreElements();) {
                    String key = (String)envProp.nextElement();
                    Object val = env.get(key);
                    if (val instanceof String) {
                        orbProp.put(key, val);
                    }
                }
            }
        } else {
            orbProp = new Properties();
        }
        if (server != null) {
            orbProp.put("org.omg.CORBA.ORBInitialHost", server);
        }
        if (port >= 0) {
            orbProp.put("org.omg.CORBA.ORBInitialPort", ""+port);
        }
        if (env != null) {
            Object applet = env.get(Context.APPLET);
            if (applet != null) {
                return initAppletORB(applet, orbProp);
            }
        }
        return ORB.init(new String[0], orbProp);
    }
    private static ORB initAppletORB(Object applet, Properties orbProp) {
        try {
            Class<?> appletClass  = Class.forName("java.applet.Applet", true, null);
            if (!appletClass.isInstance(applet)) {
                throw new ClassCastException(applet.getClass().getName());
            }
            Method method = ORB.class.getMethod("init", appletClass, Properties.class);
            return (ORB) method.invoke(null, applet, orbProp);
        } catch (ClassNotFoundException e) {
            throw new ClassCastException(applet.getClass().getName());
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new AssertionError(e);
        } catch (IllegalAccessException iae) {
            throw new AssertionError(iae);
        }
    }
    private static Method toStubMethod = null;
    private static Method connectMethod = null;
    private static Class corbaStubClass = null;
    private static void initMethodHandles() throws ClassNotFoundException {
        corbaStubClass = Class.forName("javax.rmi.CORBA.Stub");
        try {
            connectMethod = corbaStubClass.getMethod("connect",
                new Class[] {org.omg.CORBA.ORB.class});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
        "No method definition for javax.rmi.CORBA.Stub.connect(org.omg.CORBA.ORB)");
        }
        Class proClass = Class.forName("javax.rmi.PortableRemoteObject");
        try {
            toStubMethod = proClass.getMethod("toStub",
                new Class[] {java.rmi.Remote.class});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
"No method definition for javax.rmi.PortableRemoteObject.toStub(java.rmi.Remote)");
        }
    }
}
