public class ActivationGroupImpl extends ActivationGroup {
    private static final long serialVersionUID = 5758693559430427303L;
    private final Hashtable<ActivationID,ActiveEntry> active =
        new Hashtable<ActivationID,ActiveEntry>();
    private boolean groupInactive = false;
    private final ActivationGroupID groupID;
    private final List<ActivationID> lockedIDs = new ArrayList<ActivationID>();
    public ActivationGroupImpl(ActivationGroupID id, MarshalledObject<?> data)
        throws RemoteException
    {
        super(id);
        groupID = id;
        unexportObject(this, true);
        RMIServerSocketFactory ssf = new ServerSocketFactoryImpl();
        UnicastRemoteObject.exportObject(this, 0, null, ssf);
        if (System.getSecurityManager() == null) {
            try {
                System.setSecurityManager(new SecurityManager());
            } catch (Exception e) {
                throw new RemoteException("unable to set security manager", e);
            }
        }
    }
    private static class ServerSocketFactoryImpl
        implements RMIServerSocketFactory
    {
        public ServerSocket createServerSocket(int port) throws IOException
        {
            RMISocketFactory sf = RMISocketFactory.getSocketFactory();
            if (sf == null) {
                sf = RMISocketFactory.getDefaultSocketFactory();
            }
            return sf.createServerSocket(port);
        }
    }
    private void acquireLock(ActivationID id) {
        ActivationID waitForID;
        for (;;) {
            synchronized (lockedIDs) {
                int index = lockedIDs.indexOf(id);
                if (index < 0) {
                    lockedIDs.add(id);
                    return;
                } else {
                    waitForID = lockedIDs.get(index);
                }
            }
            synchronized (waitForID) {
                synchronized (lockedIDs) {
                    int index = lockedIDs.indexOf(waitForID);
                    if (index < 0) continue;
                    ActivationID actualID = lockedIDs.get(index);
                    if (actualID != waitForID)
                        continue;
                }
                try {
                    waitForID.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
    private void releaseLock(ActivationID id) {
        synchronized (lockedIDs) {
            id = lockedIDs.remove(lockedIDs.indexOf(id));
        }
        synchronized (id) {
            id.notifyAll();
        }
    }
    public MarshalledObject<? extends Remote>
                                      newInstance(final ActivationID id,
                                                  final ActivationDesc desc)
        throws ActivationException, RemoteException
    {
        RegistryImpl.checkAccess("ActivationInstantiator.newInstance");
        if (!groupID.equals(desc.getGroupID()))
            throw new ActivationException("newInstance in wrong group");
        try {
            acquireLock(id);
            synchronized (this) {
                if (groupInactive == true)
                    throw new InactiveGroupException("group is inactive");
            }
            ActiveEntry entry = active.get(id);
            if (entry != null)
                return entry.mobj;
            String className = desc.getClassName();
            final Class<? extends Remote> cl =
                RMIClassLoader.loadClass(desc.getLocation(), className)
                .asSubclass(Remote.class);
            Remote impl = null;
            final Thread t = Thread.currentThread();
            final ClassLoader savedCcl = t.getContextClassLoader();
            ClassLoader objcl = cl.getClassLoader();
            final ClassLoader ccl = covers(objcl, savedCcl) ? objcl : savedCcl;
            try {
                impl = AccessController.doPrivileged(
                      new PrivilegedExceptionAction<Remote>() {
                      public Remote run() throws InstantiationException,
                          NoSuchMethodException, IllegalAccessException,
                          InvocationTargetException
                      {
                          Constructor<? extends Remote> constructor =
                              cl.getDeclaredConstructor(
                                  ActivationID.class, MarshalledObject.class);
                          constructor.setAccessible(true);
                          try {
                              t.setContextClassLoader(ccl);
                              return constructor.newInstance(id,
                                                             desc.getData());
                          } finally {
                              t.setContextClassLoader(savedCcl);
                          }
                      }
                  });
            } catch (PrivilegedActionException pae) {
                Throwable e = pae.getException();
                if (e instanceof InstantiationException) {
                    throw (InstantiationException) e;
                } else if (e instanceof NoSuchMethodException) {
                    throw (NoSuchMethodException) e;
                } else if (e instanceof IllegalAccessException) {
                    throw (IllegalAccessException) e;
                } else if (e instanceof InvocationTargetException) {
                    throw (InvocationTargetException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else if (e instanceof Error) {
                    throw (Error) e;
                }
            }
            entry = new ActiveEntry(impl);
            active.put(id, entry);
            return entry.mobj;
        } catch (NoSuchMethodException e) {
            throw new ActivationException
                ("Activatable object must provide an activation"+
                 " constructor", e);
        } catch (NoSuchMethodError e) {
            throw new ActivationException
                ("Activatable object must provide an activation"+
                 " constructor", e );
        } catch (InvocationTargetException e) {
            throw new ActivationException("exception in object constructor",
                                          e.getTargetException());
        } catch (Exception e) {
            throw new ActivationException("unable to activate object", e);
        } finally {
            releaseLock(id);
            checkInactiveGroup();
        }
    }
    public boolean inactiveObject(ActivationID id)
        throws ActivationException, UnknownObjectException, RemoteException
    {
        try {
            acquireLock(id);
            synchronized (this) {
                if (groupInactive == true)
                    throw new ActivationException("group is inactive");
            }
            ActiveEntry entry = active.get(id);
            if (entry == null) {
                throw new UnknownObjectException("object not active");
            }
            try {
                if (Activatable.unexportObject(entry.impl, false) == false)
                    return false;
            } catch (NoSuchObjectException allowUnexportedObjects) {
            }
            try {
                super.inactiveObject(id);
            } catch (UnknownObjectException allowUnregisteredObjects) {
            }
            active.remove(id);
        } finally {
            releaseLock(id);
            checkInactiveGroup();
        }
        return true;
    }
    private void checkInactiveGroup() {
        boolean groupMarkedInactive = false;
        synchronized (this) {
            if (active.size() == 0 && lockedIDs.size() == 0 &&
                groupInactive == false)
            {
                groupInactive = true;
                groupMarkedInactive = true;
            }
        }
        if (groupMarkedInactive) {
            try {
                super.inactiveGroup();
            } catch (Exception ignoreDeactivateFailure) {
            }
            try {
                UnicastRemoteObject.unexportObject(this, true);
            } catch (NoSuchObjectException allowUnexportedGroup) {
            }
        }
    }
    public void activeObject(ActivationID id, Remote impl)
        throws ActivationException, UnknownObjectException, RemoteException
    {
        try {
            acquireLock(id);
            synchronized (this) {
                if (groupInactive == true)
                    throw new ActivationException("group is inactive");
            }
            if (!active.contains(id)) {
                ActiveEntry entry = new ActiveEntry(impl);
                active.put(id, entry);
                try {
                    super.activeObject(id, entry.mobj);
                } catch (RemoteException e) {
                }
            }
        } finally {
            releaseLock(id);
            checkInactiveGroup();
        }
    }
    private static class ActiveEntry {
        Remote impl;
        MarshalledObject<Remote> mobj;
        ActiveEntry(Remote impl) throws ActivationException {
            this.impl =  impl;
            try {
                this.mobj = new MarshalledObject<Remote>(impl);
            } catch (IOException e) {
                throw new
                    ActivationException("failed to marshal remote object", e);
            }
        }
    }
    private static boolean covers(ClassLoader sub, ClassLoader sup) {
        if (sup == null) {
            return true;
        } else if (sub == null) {
            return false;
        }
        do {
            if (sub == sup) {
                return true;
            }
            sub = sub.getParent();
        } while (sub != null);
        return false;
    }
}
