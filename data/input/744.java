public class HotspotInternal
    implements HotspotInternalMBean, MBeanRegistration {
    private final static String HOTSPOT_INTERNAL_MBEAN_NAME =
        "sun.management:type=HotspotInternal";
    private static ObjectName objName = Util.newObjectName(HOTSPOT_INTERNAL_MBEAN_NAME);
    private MBeanServer server = null;
    public HotspotInternal() {
    }
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws java.lang.Exception {
        ManagementFactoryHelper.registerInternalMBeans(server);
        this.server = server;
        return objName;
    }
    public void postRegister(Boolean registrationDone) {};
    public void preDeregister() throws java.lang.Exception {
        ManagementFactoryHelper.unregisterInternalMBeans(server);
    }
    public void postDeregister() {};
}
