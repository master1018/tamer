public class VirtualMachineManagerImpl implements VirtualMachineManagerService {
    private List<Connector> connectors = new ArrayList<Connector>();
    private LaunchingConnector defaultConnector = null;
    private List<VirtualMachine> targets = new ArrayList<VirtualMachine>();
    private final ThreadGroup mainGroupForJDI;
    private ResourceBundle messages = null;
    private int vmSequenceNumber = 0;
    private static final int majorVersion = 1;
    private static final int minorVersion = 6;
    private static final Object lock = new Object();
    private static VirtualMachineManagerImpl vmm;
    public static VirtualMachineManager virtualMachineManager() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            JDIPermission vmmPermission =
                new JDIPermission("virtualMachineManager");
            sm.checkPermission(vmmPermission);
        }
        synchronized (lock) {
            if (vmm == null) {
                vmm = new VirtualMachineManagerImpl();
            }
        }
        return vmm;
    }
    protected VirtualMachineManagerImpl() {
        ThreadGroup top = Thread.currentThread().getThreadGroup();
        ThreadGroup parent = null;
        while ((parent = top.getParent()) != null) {
            top = parent;
        }
        mainGroupForJDI = new ThreadGroup(top, "JDI main");
        ServiceLoader<Connector> connectorLoader =
            ServiceLoader.load(Connector.class, Connector.class.getClassLoader());
        Iterator<Connector> connectors = connectorLoader.iterator();
        while (connectors.hasNext()) {
            Connector connector;
            try {
                connector = connectors.next();
            } catch (ThreadDeath x) {
                throw x;
            } catch (Exception x) {
                System.err.println(x);
                continue;
            } catch (Error x) {
                System.err.println(x);
                continue;
            }
            addConnector(connector);
        }
        ServiceLoader<TransportService> transportLoader =
            ServiceLoader.load(TransportService.class,
                               TransportService.class.getClassLoader());
        Iterator<TransportService> transportServices =
            transportLoader.iterator();
        while (transportServices.hasNext()) {
            TransportService transportService;
            try {
                transportService = transportServices.next();
            } catch (ThreadDeath x) {
                throw x;
            } catch (Exception x) {
                System.err.println(x);
                continue;
            } catch (Error x) {
                System.err.println(x);
                continue;
            }
            addConnector(GenericAttachingConnector.create(transportService));
            addConnector(GenericListeningConnector.create(transportService));
        }
        if (allConnectors().size() == 0) {
            throw new Error("no Connectors loaded");
        }
        boolean found = false;
        List<LaunchingConnector> launchers = launchingConnectors();
        for (LaunchingConnector lc: launchers) {
            if (lc.name().equals("com.sun.jdi.CommandLineLaunch")) {
                setDefaultConnector(lc);
                found = true;
                break;
            }
        }
        if (!found && launchers.size() > 0) {
            setDefaultConnector(launchers.get(0));
        }
    }
    public LaunchingConnector defaultConnector() {
        if (defaultConnector == null) {
            throw new Error("no default LaunchingConnector");
        }
        return defaultConnector;
    }
    public void setDefaultConnector(LaunchingConnector connector) {
        defaultConnector = connector;
    }
    public List<LaunchingConnector> launchingConnectors() {
        List<LaunchingConnector> launchingConnectors = new ArrayList<LaunchingConnector>(connectors.size());
        for (Connector connector: connectors) {
            if (connector instanceof LaunchingConnector) {
                launchingConnectors.add((LaunchingConnector)connector);
            }
        }
        return Collections.unmodifiableList(launchingConnectors);
    }
    public List<AttachingConnector> attachingConnectors() {
        List<AttachingConnector> attachingConnectors = new ArrayList<AttachingConnector>(connectors.size());
        for (Connector connector: connectors) {
            if (connector instanceof AttachingConnector) {
                attachingConnectors.add((AttachingConnector)connector);
            }
        }
        return Collections.unmodifiableList(attachingConnectors);
    }
    public List<ListeningConnector> listeningConnectors() {
        List<ListeningConnector> listeningConnectors = new ArrayList<ListeningConnector>(connectors.size());
        for (Connector connector: connectors) {
            if (connector instanceof ListeningConnector) {
                listeningConnectors.add((ListeningConnector)connector);
            }
        }
        return Collections.unmodifiableList(listeningConnectors);
    }
    public List<Connector> allConnectors() {
        return Collections.unmodifiableList(connectors);
    }
    public List<VirtualMachine> connectedVirtualMachines() {
        return Collections.unmodifiableList(targets);
    }
    public void addConnector(Connector connector) {
        connectors.add(connector);
    }
    public void removeConnector(Connector connector) {
        connectors.remove(connector);
    }
    public synchronized VirtualMachine createVirtualMachine(
                                        Connection connection,
                                        Process process) throws IOException {
        if (!connection.isOpen()) {
            throw new IllegalStateException("connection is not open");
        }
        VirtualMachine vm;
        try {
            vm = new VirtualMachineImpl(this, connection, process,
                                                   ++vmSequenceNumber);
        } catch (VMDisconnectedException e) {
            throw new IOException(e.getMessage());
        }
        targets.add(vm);
        return vm;
    }
    public VirtualMachine createVirtualMachine(Connection connection) throws IOException {
        return createVirtualMachine(connection, null);
    }
    public void addVirtualMachine(VirtualMachine vm) {
        targets.add(vm);
    }
    void disposeVirtualMachine(VirtualMachine vm) {
        targets.remove(vm);
    }
    public int majorInterfaceVersion() {
        return majorVersion;
    }
    public int minorInterfaceVersion() {
        return minorVersion;
    }
    ThreadGroup mainGroupForJDI() {
        return mainGroupForJDI;
    }
    String getString(String key) {
        if (messages == null) {
            messages = ResourceBundle.getBundle("com.sun.tools.jdi.resources.jdi");
        }
        return messages.getString(key);
    }
}
