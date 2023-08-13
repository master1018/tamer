public final class AdaptorBootstrap {
    private static final MibLogger log = new MibLogger(AdaptorBootstrap.class);
    public static interface DefaultValues {
        public static final String PORT="161";
        public static final String CONFIG_FILE_NAME="management.properties";
        public static final String TRAP_PORT="162";
        public static final String USE_ACL="true";
        public static final String ACL_FILE_NAME="snmp.acl";
        public static final String BIND_ADDRESS="localhost";
    }
    public static interface PropertyNames {
        public static final String PORT="com.sun.management.snmp.port";
        public static final String CONFIG_FILE_NAME=
            "com.sun.management.config.file";
        public static final String TRAP_PORT=
            "com.sun.management.snmp.trap";
        public static final String USE_ACL=
            "com.sun.management.snmp.acl";
        public static final String ACL_FILE_NAME=
            "com.sun.management.snmp.acl.file";
        public static final String BIND_ADDRESS=
            "com.sun.management.snmp.interface";
    }
    private SnmpAdaptorServer       adaptor;
    private JVM_MANAGEMENT_MIB_IMPL jvmmib;
    private AdaptorBootstrap(SnmpAdaptorServer snmpas,
                             JVM_MANAGEMENT_MIB_IMPL mib) {
        jvmmib  = mib;
        adaptor = snmpas;
    }
    private static String getDefaultFileName(String basename) {
        final String fileSeparator = File.separator;
        return System.getProperty("java.home") + fileSeparator + "lib" +
            fileSeparator + "management" + fileSeparator + basename;
    }
    private static List<NotificationTarget> getTargetList(InetAddressAcl acl,
                                                          int defaultTrapPort) {
        final ArrayList<NotificationTarget> result =
                new ArrayList<NotificationTarget>();
        if (acl != null) {
            if (log.isDebugOn())
                log.debug("getTargetList",Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.processing"));
            final Enumeration td=acl.getTrapDestinations();
            for (; td.hasMoreElements() ;) {
                final InetAddress targetAddr = (InetAddress)td.nextElement();
                final Enumeration tc =
                    acl.getTrapCommunities(targetAddr);
                for (;tc.hasMoreElements() ;) {
                    final String community = (String) tc.nextElement();
                    final NotificationTarget target =
                        new NotificationTargetImpl(targetAddr,
                                                   defaultTrapPort,
                                                   community);
                    if (log.isDebugOn())
                        log.debug("getTargetList",
                                  Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.adding",
                                                target.toString()));
                    result.add(target);
                }
            }
        }
        return result;
    }
    public static synchronized AdaptorBootstrap initialize() {
        final Properties props = Agent.loadManagementProperties();
        if (props == null) return null;
        final String portStr = props.getProperty(PropertyNames.PORT);
        return initialize(portStr,props);
    }
    public static synchronized
        AdaptorBootstrap initialize(String portStr, Properties props) {
        if (portStr.length()==0) portStr=DefaultValues.PORT;
        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException x) {
            throw new AgentConfigurationError(INVALID_SNMP_PORT, x, portStr);
        }
        if (port < 0) {
            throw new AgentConfigurationError(INVALID_SNMP_PORT, portStr);
        }
        final String trapPortStr =
            props.getProperty(PropertyNames.TRAP_PORT,
                              DefaultValues.TRAP_PORT);
        final int trapPort;
        try {
            trapPort = Integer.parseInt(trapPortStr);
        } catch (NumberFormatException x) {
            throw new AgentConfigurationError(INVALID_SNMP_TRAP_PORT, x, trapPortStr);
        }
        if (trapPort < 0) {
            throw new AgentConfigurationError(INVALID_SNMP_TRAP_PORT, trapPortStr);
        }
        final String addrStr =
            props.getProperty(PropertyNames.BIND_ADDRESS,
                              DefaultValues.BIND_ADDRESS);
        final String defaultAclFileName   =
            getDefaultFileName(DefaultValues.ACL_FILE_NAME);
        final String aclFileName =
            props.getProperty(PropertyNames.ACL_FILE_NAME,
                               defaultAclFileName);
        final String  useAclStr =
            props.getProperty(PropertyNames.USE_ACL,DefaultValues.USE_ACL);
        final boolean useAcl =
            Boolean.valueOf(useAclStr).booleanValue();
        if (useAcl) checkAclFile(aclFileName);
        AdaptorBootstrap adaptor = null;
        try {
            adaptor = getAdaptorBootstrap(port, trapPort, addrStr,
                                          useAcl, aclFileName);
        } catch (Exception e) {
            throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.getMessage());
        }
        return adaptor;
    }
    private static AdaptorBootstrap getAdaptorBootstrap
        (int port, int trapPort, String bindAddress, boolean useAcl,
         String aclFileName) {
        final InetAddress address;
        try {
            address = InetAddress.getByName(bindAddress);
        } catch (UnknownHostException e) {
            throw new AgentConfigurationError(UNKNOWN_SNMP_INTERFACE, e, bindAddress);
        }
        if (log.isDebugOn()) {
            log.debug("initialize",
                      Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.starting" +
                      "\n\t" + PropertyNames.PORT + "=" + port +
                      "\n\t" + PropertyNames.TRAP_PORT + "=" + trapPort +
                      "\n\t" + PropertyNames.BIND_ADDRESS + "=" + address +
                      (useAcl?("\n\t" + PropertyNames.ACL_FILE_NAME + "="
                               + aclFileName):"\n\tNo ACL")+
                      ""));
        }
        final InetAddressAcl acl;
        try {
            acl = useAcl ? new SnmpAcl(System.getProperty("user.name"),aclFileName)
                         : null;
        } catch (UnknownHostException e) {
            throw new AgentConfigurationError(UNKNOWN_SNMP_INTERFACE, e, e.getMessage());
        }
        final SnmpAdaptorServer adaptor =
            new SnmpAdaptorServer(acl, port, address);
        adaptor.setUserDataFactory(new JvmContextFactory());
        adaptor.setTrapPort(trapPort);
        final JVM_MANAGEMENT_MIB_IMPL mib = new JVM_MANAGEMENT_MIB_IMPL();
        try {
            mib.init();
        } catch (IllegalAccessException x) {
            throw new AgentConfigurationError(SNMP_MIB_INIT_FAILED, x, x.getMessage());
        }
        mib.addTargets(getTargetList(acl,trapPort));
        try {
            adaptor.start(Long.MAX_VALUE);
        } catch (Exception x) {
            Throwable t=x;
            if (x instanceof com.sun.jmx.snmp.daemon.CommunicationException) {
                final Throwable next = t.getCause();
                if (next != null) t = next;
            }
            throw new AgentConfigurationError(SNMP_ADAPTOR_START_FAILED, t,
                                              address + ":" + port,
                                              "(" + t.getMessage() + ")");
        }
        if (!adaptor.isActive()) {
            throw new AgentConfigurationError(SNMP_ADAPTOR_START_FAILED,
                                              address + ":" + port);
        }
        try {
            adaptor.addMib(mib);
            mib.setSnmpAdaptor(adaptor);
        } catch (RuntimeException x) {
            new AdaptorBootstrap(adaptor,mib).terminate();
            throw x;
        }
        log.debug("initialize",
                  Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.initialize1"));
        log.config("initialize",
                   Agent.getText("jmxremote.AdaptorBootstrap.getTargetList.initialize2",
                                 address.toString(), java.lang.Integer.toString(adaptor.getPort())));
        return new AdaptorBootstrap(adaptor,mib);
    }
    private static void checkAclFile(String aclFileName) {
        if (aclFileName == null || aclFileName.length()==0) {
            throw new AgentConfigurationError(SNMP_ACL_FILE_NOT_SET);
        }
        final File file = new File(aclFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(SNMP_ACL_FILE_NOT_FOUND, aclFileName);
        }
        if (!file.canRead()) {
            throw new AgentConfigurationError(SNMP_ACL_FILE_NOT_READABLE, aclFileName);
        }
        FileSystem fs = FileSystem.open();
        try {
            if (fs.supportsFileSecurity(file)) {
                if (!fs.isAccessUserOnly(file)) {
                    throw new AgentConfigurationError(SNMP_ACL_FILE_ACCESS_NOT_RESTRICTED,
                        aclFileName);
                }
            }
        } catch (IOException e) {
            throw new AgentConfigurationError(SNMP_ACL_FILE_READ_FAILED, aclFileName);
        }
    }
    public synchronized int getPort() {
        if (adaptor != null) return adaptor.getPort();
        return 0;
    }
    public synchronized void terminate() {
        if (adaptor == null) return;
        try {
            jvmmib.terminate();
        } catch (Exception x) {
            log.debug("jmxremote.AdaptorBootstrap.getTargetList.terminate",
                      x.toString());
        } finally {
            jvmmib=null;
        }
        try {
            adaptor.stop();
        } finally {
            adaptor = null;
        }
    }
}
