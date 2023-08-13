public class ActivatableRef implements RemoteRef {
    private static final long serialVersionUID = 7579060052569229166L;
    protected ActivationID id;
    protected RemoteRef ref;
    transient boolean force = false;
    private static final int MAX_RETRIES = 3;
    private static final String versionComplaint =
        "activation requires 1.2 stubs";
    public ActivatableRef()
    {}
    public ActivatableRef(ActivationID id, RemoteRef ref)
    {
        this.id = id;
        this.ref = ref;
    }
    public static Remote getStub(ActivationDesc desc, ActivationID id)
        throws StubNotFoundException
    {
        String className = desc.getClassName();
        try {
            Class cl =
                RMIClassLoader.loadClass(desc.getLocation(), className);
            RemoteRef clientRef = new ActivatableRef(id, null);
            return Util.createProxy(cl, clientRef, false);
        } catch (IllegalArgumentException e) {
            throw new StubNotFoundException(
                "class implements an illegal remote interface", e);
        } catch (ClassNotFoundException e) {
            throw new StubNotFoundException("unable to load class: " +
                                            className, e);
        } catch (MalformedURLException e) {
            throw new StubNotFoundException("malformed URL", e);
        }
    }
    public Object invoke(Remote obj,
                         java.lang.reflect.Method method,
                         Object[] params,
                         long opnum)
        throws Exception
    {
        boolean force = false;
        RemoteRef localRef;
        Exception exception = null;
        synchronized (this) {
            if (ref == null) {
                localRef = activate(force);
                force = true;
            } else {
                localRef = ref;
            }
        }
        for (int retries = MAX_RETRIES; retries > 0; retries--) {
            try {
                return localRef.invoke(obj, method, params, opnum);
            } catch (NoSuchObjectException e) {
                exception = e;
            } catch (ConnectException e) {
                exception = e;
            } catch (UnknownHostException e) {
                exception = e;
            } catch (ConnectIOException e) {
                exception = e;
            } catch (MarshalException e) {
                throw e;
            } catch (ServerError e) {
                throw e;
            } catch (ServerException e) {
                throw e;
            } catch (RemoteException e) {
                synchronized (this) {
                    if (localRef == ref) {
                        ref = null;     
                    }
                }
                throw e;
            }
            if (retries > 1) {
                synchronized (this) {
                    if (localRef.remoteEquals(ref) || ref == null) {
                        RemoteRef newRef = activate(force);
                        if (newRef.remoteEquals(localRef) &&
                            exception instanceof NoSuchObjectException &&
                            force == false) {
                            newRef = activate(true);
                        }
                        localRef = newRef;
                        force = true;
                    } else {
                        localRef = ref;
                        force = false;
                    }
                }
            }
        }
        throw exception;
    }
    private synchronized RemoteRef getRef()
        throws RemoteException
    {
        if (ref == null) {
            ref = activate(false);
        }
        return ref;
    }
    private RemoteRef activate(boolean force)
        throws RemoteException
    {
        assert Thread.holdsLock(this);
        ref = null;
        try {
            Remote proxy = id.activate(force);
            ActivatableRef newRef = null;
            if (proxy instanceof RemoteStub) {
                newRef = (ActivatableRef) ((RemoteStub) proxy).getRef();
            } else {
                RemoteObjectInvocationHandler handler =
                    (RemoteObjectInvocationHandler)
                    Proxy.getInvocationHandler(proxy);
                newRef = (ActivatableRef) handler.getRef();
            }
            ref = newRef.ref;
            return ref;
        } catch (ConnectException e) {
            throw new ConnectException("activation failed", e);
        } catch (RemoteException e) {
            throw new ConnectIOException("activation failed", e);
        } catch (UnknownObjectException e) {
            throw new NoSuchObjectException("object not registered");
        } catch (ActivationException e) {
            throw new ActivateFailedException("activation failed", e);
        }
    }
    public synchronized RemoteCall newCall(RemoteObject obj,
                                           Operation[] ops,
                                           int opnum,
                                           long hash)
        throws RemoteException
    {
        throw new UnsupportedOperationException(versionComplaint);
    }
    public void invoke(RemoteCall call) throws Exception
    {
        throw new UnsupportedOperationException(versionComplaint);
    }
    public void done(RemoteCall call) throws RemoteException {
        throw new UnsupportedOperationException(versionComplaint);
    }
    public String getRefClass(ObjectOutput out)
    {
        return "ActivatableRef";
    }
    public void writeExternal(ObjectOutput out) throws IOException
    {
        RemoteRef localRef = ref;
        out.writeObject(id);
        if (localRef == null) {
            out.writeUTF("");
        } else {
            out.writeUTF(localRef.getRefClass(out));
            localRef.writeExternal(out);
        }
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        id = (ActivationID)in.readObject();
        ref = null;
        String className = in.readUTF();
        if (className.equals("")) return;
        try {
            Class refClass = Class.forName(RemoteRef.packagePrefix + "." +
                                           className);
            ref = (RemoteRef)refClass.newInstance();
            ref.readExternal(in);
        } catch (InstantiationException e) {
            throw new UnmarshalException("Unable to create remote reference",
                                         e);
        } catch (IllegalAccessException e) {
            throw new UnmarshalException("Illegal access creating remote reference");
        }
    }
    public String remoteToString() {
        return Util.getUnqualifiedName(getClass()) +
                " [remoteRef: " + ref + "]";
    }
    public int remoteHashCode() {
        return id.hashCode();
    }
    public boolean remoteEquals(RemoteRef ref) {
        if (ref instanceof ActivatableRef)
            return id.equals(((ActivatableRef)ref).id);
        return false;
    }
}
