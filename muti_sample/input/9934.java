public class Activation implements Serializable {
    private static final long serialVersionUID = 2921265612698155191L;
    private static final byte MAJOR_VERSION = 1;
    private static final byte MINOR_VERSION = 0;
    private static Object execPolicy;
    private static Method execPolicyMethod;
    private static boolean debugExec;
    private Map<ActivationID,ActivationGroupID> idTable =
        new ConcurrentHashMap<>();
    private Map<ActivationGroupID,GroupEntry> groupTable =
        new ConcurrentHashMap<>();
    private byte majorVersion = MAJOR_VERSION;
    private byte minorVersion = MINOR_VERSION;
    private transient int groupSemaphore;
    private transient int groupCounter;
    private transient ReliableLog log;
    private transient int numUpdates;
    private transient String[] command;
    private static final long groupTimeout =
        getInt("sun.rmi.activation.groupTimeout", 60000);
    private static final int snapshotInterval =
        getInt("sun.rmi.activation.snapshotInterval", 200);
    private static final long execTimeout =
        getInt("sun.rmi.activation.execTimeout", 30000);
    private static final Object initLock = new Object();
    private static boolean initDone = false;
    private static int getInt(String name, int def) {
        return AccessController.doPrivileged(new GetIntegerAction(name, def));
    }
    private transient Activator activator;
    private transient Activator activatorStub;
    private transient ActivationSystem system;
    private transient ActivationSystem systemStub;
    private transient ActivationMonitor monitor;
    private transient Registry registry;
    private transient volatile boolean shuttingDown = false;
    private transient volatile Object startupLock;
    private transient Thread shutdownHook;
    private static ResourceBundle resources = null;
    private Activation() {}
    private static void startActivation(int port,
                                        RMIServerSocketFactory ssf,
                                        String logName,
                                        String[] childArgs)
        throws Exception
    {
        ReliableLog log = new ReliableLog(logName, new ActLogHandler());
        Activation state = (Activation) log.recover();
        state.init(port, ssf, log, childArgs);
    }
    private void init(int port,
                      RMIServerSocketFactory ssf,
                      ReliableLog log,
                      String[] childArgs)
        throws Exception
    {
        this.log = log;
        numUpdates = 0;
        shutdownHook =  new ShutdownHook();
        groupSemaphore = getInt("sun.rmi.activation.groupThrottle", 3);
        groupCounter = 0;
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        ActivationGroupID[] gids =
            groupTable.keySet().toArray(new ActivationGroupID[0]);
        synchronized (startupLock = new Object()) {
            activator = new ActivatorImpl(port, ssf);
            activatorStub = (Activator) RemoteObject.toStub(activator);
            system = new ActivationSystemImpl(port, ssf);
            systemStub = (ActivationSystem) RemoteObject.toStub(system);
            monitor = new ActivationMonitorImpl(port, ssf);
            initCommand(childArgs);
            registry = new SystemRegistryImpl(port, null, ssf, systemStub);
            if (ssf != null) {
                synchronized (initLock) {
                    initDone = true;
                    initLock.notifyAll();
                }
            }
        }
        startupLock = null;
        for (int i = gids.length; --i >= 0; ) {
            try {
                getGroupEntry(gids[i]).restartServices();
            } catch (UnknownGroupException e) {
                System.err.println(
                    getTextResource("rmid.restart.group.warning"));
                e.printStackTrace();
            }
        }
    }
    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        if (! (groupTable instanceof ConcurrentHashMap)) {
            groupTable = new ConcurrentHashMap<>(groupTable);
        }
        if (! (idTable instanceof ConcurrentHashMap)) {
            idTable = new ConcurrentHashMap<>(idTable);
        }
    }
    private static class SystemRegistryImpl extends RegistryImpl {
        private static final String NAME = ActivationSystem.class.getName();
        private final ActivationSystem systemStub;
        SystemRegistryImpl(int port,
                           RMIClientSocketFactory csf,
                           RMIServerSocketFactory ssf,
                           ActivationSystem systemStub)
            throws RemoteException
        {
            super(port, csf, ssf);
            this.systemStub = systemStub;
        }
        public Remote lookup(String name)
            throws RemoteException, NotBoundException
        {
            if (name.equals(NAME)) {
                return systemStub;
            } else {
                return super.lookup(name);
            }
        }
        public String[] list() throws RemoteException {
            String[] list1 = super.list();
            int length = list1.length;
            String[] list2 = new String[length + 1];
            if (length > 0) {
                System.arraycopy(list1, 0, list2, 0, length);
            }
            list2[length] = NAME;
            return list2;
        }
        public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException
        {
            if (name.equals(NAME)) {
                throw new AccessException(
                    "binding ActivationSystem is disallowed");
            } else {
                super.bind(name, obj);
            }
        }
        public void unbind(String name)
            throws RemoteException, NotBoundException, AccessException
        {
            if (name.equals(NAME)) {
                throw new AccessException(
                    "unbinding ActivationSystem is disallowed");
            } else {
                super.unbind(name);
            }
        }
        public void rebind(String name, Remote obj)
            throws RemoteException, AccessException
        {
            if (name.equals(NAME)) {
                throw new AccessException(
                    "binding ActivationSystem is disallowed");
            } else {
                super.rebind(name, obj);
            }
        }
    }
    class ActivatorImpl extends RemoteServer implements Activator {
        private static final long serialVersionUID = -3654244726254566136L;
        ActivatorImpl(int port, RMIServerSocketFactory ssf)
            throws RemoteException
        {
            LiveRef lref =
                new LiveRef(new ObjID(ObjID.ACTIVATOR_ID), port, null, ssf);
            UnicastServerRef uref = new UnicastServerRef(lref);
            ref = uref;
            uref.exportObject(this, null, false);
        }
        public MarshalledObject<? extends Remote> activate(ActivationID id,
                                                           boolean force)
            throws ActivationException, UnknownObjectException, RemoteException
        {
            checkShutdown();
            return getGroupEntry(id).activate(id, force);
        }
    }
    class ActivationMonitorImpl extends UnicastRemoteObject
        implements ActivationMonitor
    {
        private static final long serialVersionUID = -6214940464757948867L;
        ActivationMonitorImpl(int port, RMIServerSocketFactory ssf)
            throws RemoteException
        {
            super(port, null, ssf);
        }
        public void inactiveObject(ActivationID id)
            throws UnknownObjectException, RemoteException
        {
            try {
                checkShutdown();
            } catch (ActivationException e) {
                return;
            }
            RegistryImpl.checkAccess("Activator.inactiveObject");
            getGroupEntry(id).inactiveObject(id);
        }
        public void activeObject(ActivationID id,
                                 MarshalledObject<? extends Remote> mobj)
            throws UnknownObjectException, RemoteException
        {
            try {
                checkShutdown();
            } catch (ActivationException e) {
                return;
            }
            RegistryImpl.checkAccess("ActivationSystem.activeObject");
            getGroupEntry(id).activeObject(id, mobj);
        }
        public void inactiveGroup(ActivationGroupID id,
                                  long incarnation)
            throws UnknownGroupException, RemoteException
        {
            try {
                checkShutdown();
            } catch (ActivationException e) {
                return;
            }
            RegistryImpl.checkAccess("ActivationMonitor.inactiveGroup");
            getGroupEntry(id).inactiveGroup(incarnation, false);
        }
    }
    class ActivationSystemImpl
        extends RemoteServer
        implements ActivationSystem
    {
        private static final long serialVersionUID = 9100152600327688967L;
        ActivationSystemImpl(int port, RMIServerSocketFactory ssf)
            throws RemoteException
        {
            LiveRef lref = new LiveRef(new ObjID(4), port, null, ssf);
            UnicastServerRef uref = new UnicastServerRef(lref);
            ref = uref;
            uref.exportObject(this, null);
        }
        public ActivationID registerObject(ActivationDesc desc)
            throws ActivationException, UnknownGroupException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.registerObject");
            ActivationGroupID groupID = desc.getGroupID();
            ActivationID id = new ActivationID(activatorStub);
            getGroupEntry(groupID).registerObject(id, desc, true);
            return id;
        }
        public void unregisterObject(ActivationID id)
            throws ActivationException, UnknownObjectException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.unregisterObject");
            getGroupEntry(id).unregisterObject(id, true);
        }
        public ActivationGroupID registerGroup(ActivationGroupDesc desc)
            throws ActivationException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.registerGroup");
            checkArgs(desc, null);
            ActivationGroupID id = new ActivationGroupID(systemStub);
            GroupEntry entry = new GroupEntry(id, desc);
            groupTable.put(id, entry);
            addLogRecord(new LogRegisterGroup(id, desc));
            return id;
        }
        public ActivationMonitor activeGroup(ActivationGroupID id,
                                             ActivationInstantiator group,
                                             long incarnation)
            throws ActivationException, UnknownGroupException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.activeGroup");
            getGroupEntry(id).activeGroup(group, incarnation);
            return monitor;
        }
        public void unregisterGroup(ActivationGroupID id)
            throws ActivationException, UnknownGroupException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.unregisterGroup");
            removeGroupEntry(id).unregisterGroup(true);
        }
        public ActivationDesc setActivationDesc(ActivationID id,
                                                ActivationDesc desc)
            throws ActivationException, UnknownObjectException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.setActivationDesc");
            if (!getGroupID(id).equals(desc.getGroupID())) {
                throw new ActivationException(
                    "ActivationDesc contains wrong group");
            }
            return getGroupEntry(id).setActivationDesc(id, desc, true);
        }
        public ActivationGroupDesc setActivationGroupDesc(ActivationGroupID id,
                                                          ActivationGroupDesc desc)
            throws ActivationException, UnknownGroupException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess(
                "ActivationSystem.setActivationGroupDesc");
            checkArgs(desc, null);
            return getGroupEntry(id).setActivationGroupDesc(id, desc, true);
        }
        public ActivationDesc getActivationDesc(ActivationID id)
            throws ActivationException, UnknownObjectException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess("ActivationSystem.getActivationDesc");
            return getGroupEntry(id).getActivationDesc(id);
        }
        public ActivationGroupDesc getActivationGroupDesc(ActivationGroupID id)
            throws ActivationException, UnknownGroupException, RemoteException
        {
            checkShutdown();
            RegistryImpl.checkAccess
                ("ActivationSystem.getActivationGroupDesc");
            return getGroupEntry(id).desc;
        }
        public void shutdown() throws AccessException {
            RegistryImpl.checkAccess("ActivationSystem.shutdown");
            Object lock = startupLock;
            if (lock != null) {
                synchronized (lock) {
                }
            }
            synchronized (Activation.this) {
                if (!shuttingDown) {
                    shuttingDown = true;
                    (new Shutdown()).start();
                }
            }
        }
    }
    private void checkShutdown() throws ActivationException {
        Object lock = startupLock;
        if (lock != null) {
            synchronized (lock) {
            }
        }
        if (shuttingDown == true) {
            throw new ActivationException(
                "activation system shutting down");
        }
    }
    private static void unexport(Remote obj) {
        for (;;) {
            try {
                if (UnicastRemoteObject.unexportObject(obj, false) == true) {
                    break;
                } else {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                continue;
            }
        }
    }
    private class Shutdown extends Thread {
        Shutdown() {
            super("rmid Shutdown");
        }
        public void run() {
            try {
                unexport(activator);
                unexport(system);
                for (GroupEntry groupEntry : groupTable.values()) {
                    groupEntry.shutdown();
                }
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                unexport(monitor);
                try {
                    synchronized (log) {
                        log.close();
                    }
                } catch (IOException e) {
                }
            } finally {
                System.err.println(getTextResource("rmid.daemon.shutdown"));
                System.exit(0);
            }
        }
    }
    private class ShutdownHook extends Thread {
        ShutdownHook() {
            super("rmid ShutdownHook");
        }
        public void run() {
            synchronized (Activation.this) {
                shuttingDown = true;
            }
            for (GroupEntry groupEntry : groupTable.values()) {
                groupEntry.shutdownFast();
            }
        }
    }
    private ActivationGroupID getGroupID(ActivationID id)
        throws UnknownObjectException
    {
        ActivationGroupID groupID = idTable.get(id);
        if (groupID != null) {
            return groupID;
        }
        throw new UnknownObjectException("unknown object: " + id);
    }
    private GroupEntry getGroupEntry(ActivationGroupID id, boolean rm)
        throws UnknownGroupException
    {
        if (id.getClass() == ActivationGroupID.class) {
            GroupEntry entry;
            if (rm) {
                entry = groupTable.remove(id);
            } else {
                entry = groupTable.get(id);
            }
            if (entry != null && !entry.removed) {
                return entry;
            }
        }
        throw new UnknownGroupException("group unknown");
    }
    private GroupEntry getGroupEntry(ActivationGroupID id)
        throws UnknownGroupException
    {
        return getGroupEntry(id, false);
    }
    private GroupEntry removeGroupEntry(ActivationGroupID id)
        throws UnknownGroupException
    {
        return getGroupEntry(id, true);
    }
    private GroupEntry getGroupEntry(ActivationID id)
        throws UnknownObjectException
    {
        ActivationGroupID gid = getGroupID(id);
        GroupEntry entry = groupTable.get(gid);
        if (entry != null && !entry.removed) {
            return entry;
        }
        throw new UnknownObjectException("object's group removed");
    }
    private class GroupEntry implements Serializable {
        private static final long serialVersionUID = 7222464070032993304L;
        private static final int MAX_TRIES = 2;
        private static final int NORMAL = 0;
        private static final int CREATING = 1;
        private static final int TERMINATE = 2;
        private static final int TERMINATING = 3;
        ActivationGroupDesc desc = null;
        ActivationGroupID groupID = null;
        long incarnation = 0;
        Map<ActivationID,ObjectEntry> objects =
            new HashMap<ActivationID,ObjectEntry>();
        Set<ActivationID> restartSet = new HashSet<ActivationID>();
        transient ActivationInstantiator group = null;
        transient int status = NORMAL;
        transient long waitTime = 0;
        transient String groupName = null;
        transient Process child = null;
        transient boolean removed = false;
        transient Watchdog watchdog = null;
        GroupEntry(ActivationGroupID groupID, ActivationGroupDesc desc) {
            this.groupID = groupID;
            this.desc = desc;
        }
        void restartServices() {
            Iterator<ActivationID> iter = null;
            synchronized (this) {
                if (restartSet.isEmpty()) {
                    return;
                }
                iter = (new HashSet<ActivationID>(restartSet)).iterator();
            }
            while (iter.hasNext()) {
                ActivationID id = iter.next();
                try {
                    activate(id, true);
                } catch (Exception e) {
                    if (shuttingDown) {
                        return;
                    }
                    System.err.println(
                        getTextResource("rmid.restart.service.warning"));
                    e.printStackTrace();
                }
            }
        }
        synchronized void activeGroup(ActivationInstantiator inst,
                                      long instIncarnation)
            throws ActivationException, UnknownGroupException
        {
            if (incarnation != instIncarnation) {
                throw new ActivationException("invalid incarnation");
            }
            if (group != null) {
                if (group.equals(inst)) {
                    return;
                } else {
                    throw new ActivationException("group already active");
                }
            }
            if (child != null && status != CREATING) {
                throw new ActivationException("group not being created");
            }
            group = inst;
            status = NORMAL;
            notifyAll();
        }
        private void checkRemoved() throws UnknownGroupException {
            if (removed) {
                throw new UnknownGroupException("group removed");
            }
        }
        private ObjectEntry getObjectEntry(ActivationID id)
            throws UnknownObjectException
        {
            if (removed) {
                throw new UnknownObjectException("object's group removed");
            }
            ObjectEntry objEntry = objects.get(id);
            if (objEntry == null) {
                throw new UnknownObjectException("object unknown");
            }
            return objEntry;
        }
        synchronized void registerObject(ActivationID id,
                                         ActivationDesc desc,
                                         boolean addRecord)
            throws UnknownGroupException, ActivationException
        {
            checkRemoved();
            objects.put(id, new ObjectEntry(desc));
            if (desc.getRestartMode() == true) {
                restartSet.add(id);
            }
            idTable.put(id, groupID);
            if (addRecord) {
                addLogRecord(new LogRegisterObject(id, desc));
            }
        }
        synchronized void unregisterObject(ActivationID id, boolean addRecord)
            throws UnknownGroupException, ActivationException
        {
            ObjectEntry objEntry = getObjectEntry(id);
            objEntry.removed = true;
            objects.remove(id);
            if (objEntry.desc.getRestartMode() == true) {
                restartSet.remove(id);
            }
            idTable.remove(id);
            if (addRecord) {
                addLogRecord(new LogUnregisterObject(id));
            }
        }
        synchronized void unregisterGroup(boolean addRecord)
           throws UnknownGroupException, ActivationException
        {
            checkRemoved();
            removed = true;
            for (Map.Entry<ActivationID,ObjectEntry> entry :
                     objects.entrySet())
            {
                ActivationID id = entry.getKey();
                idTable.remove(id);
                ObjectEntry objEntry = entry.getValue();
                objEntry.removed = true;
            }
            objects.clear();
            restartSet.clear();
            reset();
            childGone();
            if (addRecord) {
                addLogRecord(new LogUnregisterGroup(groupID));
            }
        }
        synchronized ActivationDesc setActivationDesc(ActivationID id,
                                                      ActivationDesc desc,
                                                      boolean addRecord)
            throws UnknownObjectException, UnknownGroupException,
                   ActivationException
        {
            ObjectEntry objEntry = getObjectEntry(id);
            ActivationDesc oldDesc = objEntry.desc;
            objEntry.desc = desc;
            if (desc.getRestartMode() == true) {
                restartSet.add(id);
            } else {
                restartSet.remove(id);
            }
            if (addRecord) {
                addLogRecord(new LogUpdateDesc(id, desc));
            }
            return oldDesc;
        }
        synchronized ActivationDesc getActivationDesc(ActivationID id)
            throws UnknownObjectException, UnknownGroupException
        {
            return getObjectEntry(id).desc;
        }
        synchronized ActivationGroupDesc setActivationGroupDesc(
                ActivationGroupID id,
                ActivationGroupDesc desc,
                boolean addRecord)
            throws UnknownGroupException, ActivationException
        {
            checkRemoved();
            ActivationGroupDesc oldDesc = this.desc;
            this.desc = desc;
            if (addRecord) {
                addLogRecord(new LogUpdateGroupDesc(id, desc));
            }
            return oldDesc;
        }
        synchronized void inactiveGroup(long incarnation, boolean failure)
            throws UnknownGroupException
        {
            checkRemoved();
            if (this.incarnation != incarnation) {
                throw new UnknownGroupException("invalid incarnation");
            }
            reset();
            if (failure) {
                terminate();
            } else if (child != null && status == NORMAL) {
                status = TERMINATE;
                watchdog.noRestart();
            }
        }
        synchronized void activeObject(ActivationID id,
                                       MarshalledObject<? extends Remote> mobj)
                throws UnknownObjectException
        {
            getObjectEntry(id).stub = mobj;
        }
        synchronized void inactiveObject(ActivationID id)
            throws UnknownObjectException
        {
            getObjectEntry(id).reset();
        }
        private synchronized void reset() {
            group = null;
            for (ObjectEntry objectEntry : objects.values()) {
                objectEntry.reset();
            }
        }
        private void childGone() {
            if (child != null) {
                child = null;
                watchdog.dispose();
                watchdog = null;
                status = NORMAL;
                notifyAll();
            }
        }
        private void terminate() {
            if (child != null && status != TERMINATING) {
                child.destroy();
                status = TERMINATING;
                waitTime = System.currentTimeMillis() + groupTimeout;
                notifyAll();
            }
        }
        private void await() {
            while (true) {
                switch (status) {
                case NORMAL:
                    return;
                case TERMINATE:
                    terminate();
                case TERMINATING:
                    try {
                        child.exitValue();
                    } catch (IllegalThreadStateException e) {
                        long now = System.currentTimeMillis();
                        if (waitTime > now) {
                            try {
                                wait(waitTime - now);
                            } catch (InterruptedException ee) {
                            }
                            continue;
                        }
                    }
                    childGone();
                    return;
                case CREATING:
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        void shutdownFast() {
            Process p = child;
            if (p != null) {
                p.destroy();
            }
        }
        synchronized void shutdown() {
            reset();
            terminate();
            await();
        }
        MarshalledObject<? extends Remote> activate(ActivationID id,
                                                    boolean force)
            throws ActivationException
        {
            Exception detail = null;
            for (int tries = MAX_TRIES; tries > 0; tries--) {
                ActivationInstantiator inst;
                long currentIncarnation;
                ObjectEntry objEntry;
                synchronized (this) {
                    objEntry = getObjectEntry(id);
                    if (!force && objEntry.stub != null) {
                        return objEntry.stub;
                    }
                    inst = getInstantiator(groupID);
                    currentIncarnation = incarnation;
                }
                boolean groupInactive = false;
                boolean failure = false;
                try {
                    return objEntry.activate(id, force, inst);
                } catch (NoSuchObjectException e) {
                    groupInactive = true;
                    detail = e;
                } catch (ConnectException e) {
                    groupInactive = true;
                    failure = true;
                    detail = e;
                } catch (ConnectIOException e) {
                    groupInactive = true;
                    failure = true;
                    detail = e;
                } catch (InactiveGroupException e) {
                    groupInactive = true;
                    detail = e;
                } catch (RemoteException e) {
                    if (detail == null) {
                        detail = e;
                    }
                }
                if (groupInactive) {
                    try {
                        System.err.println(
                            MessageFormat.format(
                                getTextResource("rmid.group.inactive"),
                                detail.toString()));
                        detail.printStackTrace();
                        getGroupEntry(groupID).
                            inactiveGroup(currentIncarnation, failure);
                    } catch (UnknownGroupException e) {
                    }
                }
            }
            throw new ActivationException("object activation failed after " +
                                          MAX_TRIES + " tries", detail);
        }
        private ActivationInstantiator getInstantiator(ActivationGroupID id)
            throws ActivationException
        {
            assert Thread.holdsLock(this);
            await();
            if (group != null) {
                return group;
            }
            checkRemoved();
            boolean acquired = false;
            try {
                groupName = Pstartgroup();
                acquired = true;
                String[] argv = activationArgs(desc);
                checkArgs(desc, argv);
                if (debugExec) {
                    StringBuffer sb = new StringBuffer(argv[0]);
                    int j;
                    for (j = 1; j < argv.length; j++) {
                        sb.append(' ');
                        sb.append(argv[j]);
                    }
                    System.err.println(
                        MessageFormat.format(
                            getTextResource("rmid.exec.command"),
                            sb.toString()));
                }
                try {
                    child = Runtime.getRuntime().exec(argv);
                    status = CREATING;
                    ++incarnation;
                    watchdog = new Watchdog();
                    watchdog.start();
                    addLogRecord(new LogGroupIncarnation(id, incarnation));
                    PipeWriter.plugTogetherPair
                        (child.getInputStream(), System.out,
                         child.getErrorStream(), System.err);
                    MarshalOutputStream out =
                        new MarshalOutputStream(child.getOutputStream());
                    out.writeObject(id);
                    out.writeObject(desc);
                    out.writeLong(incarnation);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    terminate();
                    throw new ActivationException(
                        "unable to create activation group", e);
                }
                try {
                    long now = System.currentTimeMillis();
                    long stop = now + execTimeout;
                    do {
                        wait(stop - now);
                        if (group != null) {
                            return group;
                        }
                        now = System.currentTimeMillis();
                    } while (status == CREATING && now < stop);
                } catch (InterruptedException e) {
                }
                terminate();
                throw new ActivationException(
                        (removed ?
                         "activation group unregistered" :
                         "timeout creating child process"));
            } finally {
                if (acquired) {
                    Vstartgroup();
                }
            }
        }
        private class Watchdog extends Thread {
            private final Process groupProcess = child;
            private final long groupIncarnation = incarnation;
            private boolean canInterrupt = true;
            private boolean shouldQuit = false;
            private boolean shouldRestart = true;
            Watchdog() {
                super("WatchDog-"  + groupName + "-" + incarnation);
                setDaemon(true);
            }
            public void run() {
                if (shouldQuit) {
                    return;
                }
                try {
                    groupProcess.waitFor();
                } catch (InterruptedException exit) {
                    return;
                }
                boolean restart = false;
                synchronized (GroupEntry.this) {
                    if (shouldQuit) {
                        return;
                    }
                    canInterrupt = false;
                    interrupted(); 
                    if (groupIncarnation == incarnation) {
                        restart = shouldRestart && !shuttingDown;
                        reset();
                        childGone();
                    }
                }
                if (restart) {
                    restartServices();
                }
            }
            void dispose() {
                shouldQuit = true;
                if (canInterrupt) {
                    interrupt();
                }
            }
            void noRestart() {
                shouldRestart = false;
            }
        }
    }
    private String[] activationArgs(ActivationGroupDesc desc) {
        ActivationGroupDesc.CommandEnvironment cmdenv;
        cmdenv = desc.getCommandEnvironment();
        List<String> argv = new ArrayList<String>();
        argv.add((cmdenv != null && cmdenv.getCommandPath() != null)
                    ? cmdenv.getCommandPath()
                    : command[0]);
        if (cmdenv != null && cmdenv.getCommandOptions() != null) {
            argv.addAll(Arrays.asList(cmdenv.getCommandOptions()));
        }
        Properties props = desc.getPropertyOverrides();
        if (props != null) {
            for (Enumeration<?> p = props.propertyNames();
                 p.hasMoreElements();)
            {
                String name = (String) p.nextElement();
                argv.add("-D" + name + "=" + props.getProperty(name));
            }
        }
        for (int i = 1; i < command.length; i++) {
            argv.add(command[i]);
        }
        String[] realArgv = new String[argv.size()];
        System.arraycopy(argv.toArray(), 0, realArgv, 0, realArgv.length);
        return realArgv;
    }
    private void checkArgs(ActivationGroupDesc desc, String[] cmd)
        throws SecurityException, ActivationException
    {
        if (execPolicyMethod != null) {
            if (cmd == null) {
                cmd = activationArgs(desc);
            }
            try {
                execPolicyMethod.invoke(execPolicy, desc, cmd);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof SecurityException) {
                    throw (SecurityException) targetException;
                } else {
                    throw new ActivationException(
                        execPolicyMethod.getName() + ": unexpected exception",
                        e);
                }
            } catch (Exception e) {
                throw new ActivationException(
                    execPolicyMethod.getName() + ": unexpected exception", e);
            }
        }
    }
    private static class ObjectEntry implements Serializable {
        private static final long serialVersionUID = -5500114225321357856L;
        ActivationDesc desc;
        volatile transient MarshalledObject<? extends Remote> stub = null;
        volatile transient boolean removed = false;
        ObjectEntry(ActivationDesc desc) {
            this.desc = desc;
        }
        synchronized MarshalledObject<? extends Remote>
            activate(ActivationID id,
                     boolean force,
                     ActivationInstantiator inst)
            throws RemoteException, ActivationException
        {
            MarshalledObject<? extends Remote> nstub = stub;
            if (removed) {
                throw new UnknownObjectException("object removed");
            } else if (!force && nstub != null) {
                return nstub;
            }
            nstub = inst.newInstance(id, desc);
            stub = nstub;
            return nstub;
        }
        void reset() {
            stub = null;
        }
    }
    private void addLogRecord(LogRecord rec) throws ActivationException {
        synchronized (log) {
            checkShutdown();
            try {
                log.update(rec, true);
            } catch (Exception e) {
                numUpdates = snapshotInterval;
                System.err.println(getTextResource("rmid.log.update.warning"));
                e.printStackTrace();
            }
            if (++numUpdates < snapshotInterval) {
                return;
            }
            try {
                log.snapshot(this);
                numUpdates = 0;
            } catch (Exception e) {
                System.err.println(
                    getTextResource("rmid.log.snapshot.warning"));
                e.printStackTrace();
                try {
                    system.shutdown();
                } catch (RemoteException ignore) {
                }
                throw new ActivationException("log snapshot failed", e);
            }
        }
    }
    private static class ActLogHandler extends LogHandler {
        ActLogHandler() {
        }
        public Object initialSnapshot()
        {
            return new Activation();
        }
        public Object applyUpdate(Object update, Object state)
            throws Exception
        {
            return ((LogRecord) update).apply(state);
        }
    }
    private static abstract class LogRecord implements Serializable {
        private static final long serialVersionUID = 8395140512322687529L;
        abstract Object apply(Object state) throws Exception;
    }
    private static class LogRegisterObject extends LogRecord {
        private static final long serialVersionUID = -6280336276146085143L;
        private ActivationID id;
        private ActivationDesc desc;
        LogRegisterObject(ActivationID id, ActivationDesc desc) {
            this.id = id;
            this.desc = desc;
        }
        Object apply(Object state) {
            try {
                ((Activation) state).getGroupEntry(desc.getGroupID()).
                    registerObject(id, desc, false);
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogRegisterObject"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private static class LogUnregisterObject extends LogRecord {
        private static final long serialVersionUID = 6269824097396935501L;
        private ActivationID id;
        LogUnregisterObject(ActivationID id) {
            this.id = id;
        }
        Object apply(Object state) {
            try {
                ((Activation) state).getGroupEntry(id).
                    unregisterObject(id, false);
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogUnregisterObject"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private static class LogRegisterGroup extends LogRecord {
        private static final long serialVersionUID = -1966827458515403625L;
        private ActivationGroupID id;
        private ActivationGroupDesc desc;
        LogRegisterGroup(ActivationGroupID id, ActivationGroupDesc desc) {
            this.id = id;
            this.desc = desc;
        }
        Object apply(Object state) {
            ((Activation) state).groupTable.put(id, ((Activation) state).new
                                                GroupEntry(id, desc));
            return state;
        }
    }
    private static class LogUpdateDesc extends LogRecord {
        private static final long serialVersionUID = 545511539051179885L;
        private ActivationID id;
        private ActivationDesc desc;
        LogUpdateDesc(ActivationID id, ActivationDesc desc) {
            this.id = id;
            this.desc = desc;
        }
        Object apply(Object state) {
            try {
                ((Activation) state).getGroupEntry(id).
                    setActivationDesc(id, desc, false);
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogUpdateDesc"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private static class LogUpdateGroupDesc extends LogRecord {
        private static final long serialVersionUID = -1271300989218424337L;
        private ActivationGroupID id;
        private ActivationGroupDesc desc;
        LogUpdateGroupDesc(ActivationGroupID id, ActivationGroupDesc desc) {
            this.id = id;
            this.desc = desc;
        }
        Object apply(Object state) {
            try {
                ((Activation) state).getGroupEntry(id).
                    setActivationGroupDesc(id, desc, false);
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogUpdateGroupDesc"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private static class LogUnregisterGroup extends LogRecord {
        private static final long serialVersionUID = -3356306586522147344L;
        private ActivationGroupID id;
        LogUnregisterGroup(ActivationGroupID id) {
            this.id = id;
        }
        Object apply(Object state) {
            GroupEntry entry = ((Activation) state).groupTable.remove(id);
            try {
                entry.unregisterGroup(false);
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogUnregisterGroup"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private static class LogGroupIncarnation extends LogRecord {
        private static final long serialVersionUID = 4146872747377631897L;
        private ActivationGroupID id;
        private long inc;
        LogGroupIncarnation(ActivationGroupID id, long inc) {
            this.id = id;
            this.inc = inc;
        }
        Object apply(Object state) {
            try {
                GroupEntry entry = ((Activation) state).getGroupEntry(id);
                entry.incarnation = inc;
            } catch (Exception ignore) {
                System.err.println(
                    MessageFormat.format(
                        getTextResource("rmid.log.recover.warning"),
                        "LogGroupIncarnation"));
                ignore.printStackTrace();
            }
            return state;
        }
    }
    private void initCommand(String[] childArgs) {
        command = new String[childArgs.length + 2];
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    command[0] = System.getProperty("java.home") +
                        File.separator + "bin" + File.separator + "java";
                } catch (Exception e) {
                    System.err.println(
                        getTextResource("rmid.unfound.java.home.property"));
                    command[0] = "java";
                }
                return null;
            }
        });
        System.arraycopy(childArgs, 0, command, 1, childArgs.length);
        command[command.length-1] = "sun.rmi.server.ActivationGroupInit";
    }
    private static void bomb(String error) {
        System.err.println("rmid: " + error); 
        System.err.println(MessageFormat.format(getTextResource("rmid.usage"),
                    "rmid"));
        System.exit(1);
    }
    public static class DefaultExecPolicy {
        public void checkExecCommand(ActivationGroupDesc desc, String[] cmd)
            throws SecurityException
        {
            PermissionCollection perms = getExecPermissions();
            Properties props = desc.getPropertyOverrides();
            if (props != null) {
                Enumeration<?> p = props.propertyNames();
                while (p.hasMoreElements()) {
                    String name = (String) p.nextElement();
                    String value = props.getProperty(name);
                    String option = "-D" + name + "=" + value;
                    try {
                        checkPermission(perms,
                            new ExecOptionPermission(option));
                    } catch (AccessControlException e) {
                        if (value.equals("")) {
                            checkPermission(perms,
                                new ExecOptionPermission("-D" + name));
                        } else {
                            throw e;
                        }
                    }
                }
            }
            String groupClassName = desc.getClassName();
            if ((groupClassName != null &&
                 !groupClassName.equals(
                    ActivationGroupImpl.class.getName())) ||
                (desc.getLocation() != null) ||
                (desc.getData() != null))
            {
                throw new AccessControlException(
                    "access denied (custom group implementation not allowed)");
            }
            ActivationGroupDesc.CommandEnvironment cmdenv;
            cmdenv = desc.getCommandEnvironment();
            if (cmdenv != null) {
                String path = cmdenv.getCommandPath();
                if (path != null) {
                    checkPermission(perms, new ExecPermission(path));
                }
                String[] options = cmdenv.getCommandOptions();
                if (options != null) {
                    for (String option : options) {
                        checkPermission(perms,
                                        new ExecOptionPermission(option));
                    }
                }
            }
        }
        static void checkConfiguration() {
            Policy policy =
                AccessController.doPrivileged(new PrivilegedAction<Policy>() {
                    public Policy run() {
                        return Policy.getPolicy();
                    }
                });
            if (!(policy instanceof PolicyFile)) {
                return;
            }
            PermissionCollection perms = getExecPermissions();
            for (Enumeration<Permission> e = perms.elements();
                 e.hasMoreElements();)
            {
                Permission p = e.nextElement();
                if (p instanceof AllPermission ||
                    p instanceof ExecPermission ||
                    p instanceof ExecOptionPermission)
                {
                    return;
                }
            }
            System.err.println(getTextResource("rmid.exec.perms.inadequate"));
        }
        private static PermissionCollection getExecPermissions() {
            PermissionCollection perms = AccessController.doPrivileged(
                new PrivilegedAction<PermissionCollection>() {
                    public PermissionCollection run() {
                        CodeSource codesource =
                            new CodeSource(null, (Certificate[]) null);
                        Policy p = Policy.getPolicy();
                        if (p != null) {
                            return p.getPermissions(codesource);
                        } else {
                            return new Permissions();
                        }
                    }
                });
            return perms;
        }
        private static void checkPermission(PermissionCollection perms,
                                            Permission p)
            throws AccessControlException
        {
            if (!perms.implies(p)) {
                throw new AccessControlException(
                   "access denied " + p.toString());
            }
        }
    }
    public static void main(String[] args) {
        boolean stop = false;
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            int port = ActivationSystem.SYSTEM_PORT;
            RMIServerSocketFactory ssf = null;
            Channel inheritedChannel = AccessController.doPrivileged(
                new PrivilegedExceptionAction<Channel>() {
                    public Channel run() throws IOException {
                        return System.inheritedChannel();
                    }
                });
            if (inheritedChannel != null &&
                inheritedChannel instanceof ServerSocketChannel)
            {
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Void>() {
                        public Void run() throws IOException {
                            File file =
                                File.createTempFile("rmid-err", null, null);
                            PrintStream errStream =
                                new PrintStream(new FileOutputStream(file));
                            System.setErr(errStream);
                            return null;
                        }
                    });
                ServerSocket serverSocket =
                    ((ServerSocketChannel) inheritedChannel).socket();
                port = serverSocket.getLocalPort();
                ssf = new ActivationServerSocketFactory(serverSocket);
                System.err.println(new Date());
                System.err.println(getTextResource(
                                       "rmid.inherited.channel.info") +
                                       ": " + inheritedChannel);
            }
            String log = null;
            List<String> childArgs = new ArrayList<String>();
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-port")) {
                    if (ssf != null) {
                        bomb(getTextResource("rmid.syntax.port.badarg"));
                    }
                    if ((i + 1) < args.length) {
                        try {
                            port = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException nfe) {
                            bomb(getTextResource("rmid.syntax.port.badnumber"));
                        }
                    } else {
                        bomb(getTextResource("rmid.syntax.port.missing"));
                    }
                } else if (args[i].equals("-log")) {
                    if ((i + 1) < args.length) {
                        log = args[++i];
                    } else {
                        bomb(getTextResource("rmid.syntax.log.missing"));
                    }
                } else if (args[i].equals("-stop")) {
                    stop = true;
                } else if (args[i].startsWith("-C")) {
                    childArgs.add(args[i].substring(2));
                } else {
                    bomb(MessageFormat.format(
                        getTextResource("rmid.syntax.illegal.option"),
                        args[i]));
                }
            }
            if (log == null) {
                if (ssf != null) {
                    bomb(getTextResource("rmid.syntax.log.required"));
                } else {
                    log = "log";
                }
            }
            debugExec = AccessController.doPrivileged(
                new GetBooleanAction("sun.rmi.server.activation.debugExec"));
            String execPolicyClassName = AccessController.doPrivileged(
                new GetPropertyAction("sun.rmi.activation.execPolicy", null));
            if (execPolicyClassName == null) {
                if (!stop) {
                    DefaultExecPolicy.checkConfiguration();
                }
                execPolicyClassName = "default";
            }
            if (!execPolicyClassName.equals("none")) {
                if (execPolicyClassName.equals("") ||
                    execPolicyClassName.equals("default"))
                {
                    execPolicyClassName = DefaultExecPolicy.class.getName();
                }
                try {
                    Class<?> execPolicyClass =
                        RMIClassLoader.loadClass(execPolicyClassName);
                    execPolicy = execPolicyClass.newInstance();
                    execPolicyMethod =
                        execPolicyClass.getMethod("checkExecCommand",
                                                  ActivationGroupDesc.class,
                                                  String[].class);
                } catch (Exception e) {
                    if (debugExec) {
                        System.err.println(
                            getTextResource("rmid.exec.policy.exception"));
                        e.printStackTrace();
                    }
                    bomb(getTextResource("rmid.exec.policy.invalid"));
                }
            }
            if (stop == true) {
                final int finalPort = port;
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        System.setProperty("java.rmi.activation.port",
                                           Integer.toString(finalPort));
                        return null;
                    }
                });
                ActivationSystem system = ActivationGroup.getSystem();
                system.shutdown();
                System.exit(0);
            }
            startActivation(port, ssf, log,
                            childArgs.toArray(new String[childArgs.size()]));
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                }
            }
        } catch (Exception e) {
            System.err.println(
                MessageFormat.format(
                    getTextResource("rmid.unexpected.exception"), e));
            e.printStackTrace();
        }
        System.exit(1);
    }
    private static String getTextResource(String key) {
        if (Activation.resources == null) {
            try {
                Activation.resources = ResourceBundle.getBundle(
                    "sun.rmi.server.resources.rmid");
            } catch (MissingResourceException mre) {
            }
            if (Activation.resources == null) {
                return ("[missing resource file: " + key + "]");
            }
        }
        String val = null;
        try {
            val = Activation.resources.getString (key);
        } catch (MissingResourceException mre) {
        }
        if (val == null) {
            return ("[missing resource: " + key + "]");
        } else {
            return val;
        }
    }
    private synchronized String Pstartgroup() throws ActivationException {
        while (true) {
            checkShutdown();
            if (groupSemaphore > 0) {
                groupSemaphore--;
                return "Group-" + groupCounter++;
            }
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    private synchronized void Vstartgroup() {
        groupSemaphore++;
        notifyAll();
    }
    private static class ActivationServerSocketFactory
        implements RMIServerSocketFactory
    {
        private final ServerSocket serverSocket;
        ActivationServerSocketFactory(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        public ServerSocket createServerSocket(int port)
            throws IOException
        {
            return new DelayedAcceptServerSocket(serverSocket);
        }
    }
    private static class DelayedAcceptServerSocket extends ServerSocket {
        private final ServerSocket serverSocket;
        DelayedAcceptServerSocket(ServerSocket serverSocket)
            throws IOException
        {
            this.serverSocket = serverSocket;
        }
        public void bind(SocketAddress endpoint) throws IOException {
            serverSocket.bind(endpoint);
        }
        public void bind(SocketAddress endpoint, int backlog)
                throws IOException
        {
            serverSocket.bind(endpoint, backlog);
        }
        public InetAddress getInetAddress() {
            return serverSocket.getInetAddress();
        }
        public int getLocalPort() {
            return serverSocket.getLocalPort();
        }
        public SocketAddress getLocalSocketAddress() {
            return serverSocket.getLocalSocketAddress();
        }
        public Socket accept() throws IOException {
            synchronized (initLock) {
                try {
                    while (!initDone) {
                        initLock.wait();
                    }
                } catch (InterruptedException ignore) {
                    throw new AssertionError(ignore);
                }
            }
            return serverSocket.accept();
        }
        public void close() throws IOException {
            serverSocket.close();
        }
        public ServerSocketChannel getChannel() {
            return serverSocket.getChannel();
        }
        public boolean isBound() {
            return serverSocket.isBound();
        }
        public boolean isClosed() {
            return serverSocket.isClosed();
        }
        public void setSoTimeout(int timeout)
            throws SocketException
        {
            serverSocket.setSoTimeout(timeout);
        }
        public int getSoTimeout() throws IOException {
            return serverSocket.getSoTimeout();
        }
        public void setReuseAddress(boolean on) throws SocketException {
            serverSocket.setReuseAddress(on);
        }
        public boolean getReuseAddress() throws SocketException {
            return serverSocket.getReuseAddress();
        }
        public String toString() {
            return serverSocket.toString();
        }
        public void setReceiveBufferSize(int size)
            throws SocketException
        {
            serverSocket.setReceiveBufferSize(size);
        }
        public int getReceiveBufferSize()
            throws SocketException
        {
            return serverSocket.getReceiveBufferSize();
        }
    }
}
class PipeWriter implements Runnable {
    private ByteArrayOutputStream bufOut;
    private int cLast;
    private byte[] currSep;
    private PrintWriter out;
    private InputStream in;
    private String pipeString;
    private String execString;
    private static String lineSeparator;
    private static int lineSeparatorLength;
    private static int numExecs = 0;
    static {
        lineSeparator = AccessController.doPrivileged(
            new GetPropertyAction("line.separator"));
        lineSeparatorLength = lineSeparator.length();
    }
    private PipeWriter
        (InputStream in, OutputStream out, String tag, int nExecs) {
        this.in = in;
        this.out = new PrintWriter(out);
        bufOut = new ByteArrayOutputStream();
        currSep = new byte[lineSeparatorLength];
        execString = ":ExecGroup-" +
            Integer.toString(nExecs) + ':' + tag + ':';
    }
    public void run() {
        byte[] buf = new byte[256];
        int count;
        try {
            while ((count = in.read(buf)) != -1) {
                write(buf, 0, count);
            }
            String lastInBuffer = bufOut.toString();
            bufOut.reset();
            if (lastInBuffer.length() > 0) {
                out.println (createAnnotation() + lastInBuffer);
                out.flush();                    
            }
        } catch (IOException e) {
        }
    }
    private void write(byte b[], int off, int len) throws IOException {
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException(len);
        }
        for (int i = 0; i < len; ++ i) {
            write(b[off + i]);
        }
    }
    private void write(byte b) throws IOException {
        int i = 0;
        for (i = 1 ; i < (currSep.length); i ++) {
            currSep[i-1] = currSep[i];
        }
        currSep[i-1] = b;
        bufOut.write(b);
        if ( (cLast >= (lineSeparatorLength - 1)) &&
             (lineSeparator.equals(new String(currSep))) ) {
            cLast = 0;
            out.print(createAnnotation() + bufOut.toString());
            out.flush();
            bufOut.reset();
            if (out.checkError()) {
                throw new IOException
                    ("PipeWriter: IO Exception when"+
                     " writing to output stream.");
            }
        } else {
            cLast++;
        }
    }
    private String createAnnotation() {
        return ((new Date()).toString()  +
                 (execString));
    }
    static void plugTogetherPair(InputStream in,
                                 OutputStream out,
                                 InputStream in1,
                                 OutputStream out1) {
        Thread inThread = null;
        Thread outThread = null;
        int nExecs = getNumExec();
        inThread = AccessController.doPrivileged(
            new NewThreadAction(new PipeWriter(in, out, "out", nExecs),
                                "out", true));
        outThread = AccessController.doPrivileged(
            new NewThreadAction(new PipeWriter(in1, out1, "err", nExecs),
                                "err", true));
        inThread.start();
        outThread.start();
    }
    private static synchronized int getNumExec() {
        return numExecs++;
    }
}
