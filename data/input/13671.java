public abstract class AttachProvider {
    private static final Object lock = new Object();
    private static List<AttachProvider> providers = null;
    protected AttachProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new AttachPermission("createAttachProvider"));
    }
    public abstract String name();
    public abstract String type();
    public abstract VirtualMachine attachVirtualMachine(String id)
        throws AttachNotSupportedException, IOException;
    public VirtualMachine attachVirtualMachine(VirtualMachineDescriptor vmd)
        throws AttachNotSupportedException, IOException
    {
        if (vmd.provider() != this) {
            throw new AttachNotSupportedException("provider mismatch");
        }
        return attachVirtualMachine(vmd.id());
    }
    public abstract List<VirtualMachineDescriptor> listVirtualMachines();
    public static List<AttachProvider> providers() {
        synchronized (lock) {
            if (providers == null) {
                providers = new ArrayList<AttachProvider>();
                ServiceLoader<AttachProvider> providerLoader =
                    ServiceLoader.load(AttachProvider.class,
                                       AttachProvider.class.getClassLoader());
                Iterator<AttachProvider> i = providerLoader.iterator();
                while (i.hasNext()) {
                    try {
                        providers.add(i.next());
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            ThreadDeath td = (ThreadDeath)t;
                            throw td;
                        }
                        System.err.println(t);
                    }
                }
            }
            return Collections.unmodifiableList(providers);
        }
    }
}
