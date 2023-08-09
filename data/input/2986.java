public class JmxProperties {
    private JmxProperties() {
    }
    public static final String JMX_INITIAL_BUILDER =
            "javax.management.builder.initial";
    public static final String MLET_LIB_DIR = "jmx.mlet.library.dir";
    public static final String JMX_SPEC_NAME = "jmx.specification.name";
    public static final String JMX_SPEC_VERSION = "jmx.specification.version";
    public static final String JMX_SPEC_VENDOR = "jmx.specification.vendor";
    public static final String JMX_IMPL_NAME = "jmx.implementation.name";
    public static final String JMX_IMPL_VENDOR = "jmx.implementation.vendor";
    public static final String JMX_IMPL_VERSION = "jmx.implementation.version";
    public static final String MBEANSERVER_LOGGER_NAME =
            "javax.management.mbeanserver";
    public static final Logger MBEANSERVER_LOGGER =
            Logger.getLogger(MBEANSERVER_LOGGER_NAME);
    public static final String MLET_LOGGER_NAME =
            "javax.management.mlet";
    public static final Logger MLET_LOGGER =
            Logger.getLogger(MLET_LOGGER_NAME);
    public static final String MONITOR_LOGGER_NAME =
            "javax.management.monitor";
    public static final Logger MONITOR_LOGGER =
            Logger.getLogger(MONITOR_LOGGER_NAME);
    public static final String TIMER_LOGGER_NAME =
            "javax.management.timer";
    public static final Logger TIMER_LOGGER =
            Logger.getLogger(TIMER_LOGGER_NAME);
    public static final String NOTIFICATION_LOGGER_NAME =
            "javax.management.notification";
    public static final Logger NOTIFICATION_LOGGER =
            Logger.getLogger(NOTIFICATION_LOGGER_NAME);
    public static final String RELATION_LOGGER_NAME =
            "javax.management.relation";
    public static final Logger RELATION_LOGGER =
            Logger.getLogger(RELATION_LOGGER_NAME);
    public static final String MODELMBEAN_LOGGER_NAME =
            "javax.management.modelmbean";
    public static final Logger MODELMBEAN_LOGGER =
            Logger.getLogger(MODELMBEAN_LOGGER_NAME);
    public static final String MISC_LOGGER_NAME =
            "javax.management.misc";
    public static final Logger MISC_LOGGER =
            Logger.getLogger(MISC_LOGGER_NAME);
    public static final String SNMP_LOGGER_NAME =
            "javax.management.snmp";
    public static final Logger SNMP_LOGGER =
            Logger.getLogger(SNMP_LOGGER_NAME);
    public static final String SNMP_ADAPTOR_LOGGER_NAME =
            "javax.management.snmp.daemon";
    public static final Logger SNMP_ADAPTOR_LOGGER =
            Logger.getLogger(SNMP_ADAPTOR_LOGGER_NAME);
}
