public class Agent {
    private static Properties mgmtProps;
    private static ResourceBundle messageRB;
    private static final String CONFIG_FILE =
        "com.sun.management.config.file";
    private static final String SNMP_PORT =
        "com.sun.management.snmp.port";
    private static final String JMXREMOTE =
        "com.sun.management.jmxremote";
    private static final String JMXREMOTE_PORT =
        "com.sun.management.jmxremote.port";
    private static final String ENABLE_THREAD_CONTENTION_MONITORING =
        "com.sun.management.enableThreadContentionMonitoring";
    private static final String LOCAL_CONNECTOR_ADDRESS_PROP =
        "com.sun.management.jmxremote.localConnectorAddress";
    private static final String SNMP_ADAPTOR_BOOTSTRAP_CLASS_NAME =
            "sun.management.snmp.AdaptorBootstrap";
    public static void premain(String args) throws Exception {
        agentmain(args);
    }
    public static void agentmain(String args) throws Exception {
        if (args == null || args.length() == 0) {
            args = JMXREMOTE;           
        }
        Properties arg_props = new Properties();
        if (args != null) {
            String[] options = args.split(",");
            for (int i=0; i<options.length; i++) {
                String[] option = options[i].split("=");
                if (option.length >= 1 && option.length <= 2) {
                    String name = option[0];
                    String value = (option.length == 1) ? "" : option[1];
                    if (name != null && name.length() > 0) {
                        if (name.startsWith("com.sun.management.")) {
                            arg_props.setProperty(name, value);
                        } else {
                            error(INVALID_OPTION, name);
                        }
                    }
                }
            }
        }
        Properties config_props = new Properties();
        String fname = arg_props.getProperty(CONFIG_FILE);
        readConfiguration(fname, config_props);
        config_props.putAll(arg_props);
        startAgent(config_props);
    }
    private static void startAgent(Properties props) throws Exception {
        String snmpPort = props.getProperty(SNMP_PORT);
        String jmxremote = props.getProperty(JMXREMOTE);
        String jmxremotePort = props.getProperty(JMXREMOTE_PORT);
        final String enableThreadContentionMonitoring =
            props.getProperty(ENABLE_THREAD_CONTENTION_MONITORING);
        if (enableThreadContentionMonitoring != null) {
            ManagementFactory.getThreadMXBean().
                setThreadContentionMonitoringEnabled(true);
        }
        try {
            if (snmpPort != null) {
                loadSnmpAgent(snmpPort, props);
            }
            if (jmxremote != null || jmxremotePort != null) {
                if (jmxremotePort != null) {
                    ConnectorBootstrap.initialize(jmxremotePort, props);
                }
                Properties agentProps = VMSupport.getAgentProperties();
                if (agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP) == null) {
                    JMXConnectorServer cs = ConnectorBootstrap.startLocalConnectorServer();
                    String address = cs.getAddress().toString();
                    agentProps.put(LOCAL_CONNECTOR_ADDRESS_PROP, address);
                    try {
                        ConnectorAddressLink.export(address);
                    } catch (Exception x) {
                        warning(EXPORT_ADDRESS_FAILED, x.getMessage());
                    }
                }
            }
        } catch (AgentConfigurationError e) {
            error(e.getError(), e.getParams());
        } catch (Exception e) {
            error(e);
        }
    }
    public static Properties loadManagementProperties() {
        Properties props = new Properties();
        String fname = System.getProperty(CONFIG_FILE);
        readConfiguration(fname, props);
        props.putAll(System.getProperties());
        return props;
    }
    public static synchronized Properties getManagementProperties() {
        if (mgmtProps == null) {
            String configFile = System.getProperty(CONFIG_FILE);
            String snmpPort = System.getProperty(SNMP_PORT);
            String jmxremote = System.getProperty(JMXREMOTE);
            String jmxremotePort = System.getProperty(JMXREMOTE_PORT);
            if (configFile == null && snmpPort == null &&
                jmxremote == null && jmxremotePort == null) {
                return null;
            }
            mgmtProps = loadManagementProperties();
        }
        return mgmtProps;
    }
    private static void loadSnmpAgent(String snmpPort, Properties props) {
        try {
            final Class<?> adaptorClass =
                Class.forName(SNMP_ADAPTOR_BOOTSTRAP_CLASS_NAME,true,null);
            final Method initializeMethod =
                    adaptorClass.getMethod("initialize",
                        String.class, Properties.class);
            initializeMethod.invoke(null,snmpPort,props);
        } catch (ClassNotFoundException x) {
            throw new UnsupportedOperationException("Unsupported management property: " + SNMP_PORT,x);
        } catch (NoSuchMethodException x) {
            throw new UnsupportedOperationException("Unsupported management property: " + SNMP_PORT,x);
        } catch (InvocationTargetException x) {
            final Throwable cause = x.getCause();
            if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            else if (cause instanceof Error)
                throw (Error) cause;
            throw new UnsupportedOperationException("Unsupported management property: " + SNMP_PORT,cause);
        } catch (IllegalAccessException x) {
            throw new UnsupportedOperationException("Unsupported management property: " + SNMP_PORT,x);
        }
    }
    private static void readConfiguration(String fname, Properties p) {
        if (fname == null) {
            String home = System.getProperty("java.home");
            if (home == null) {
                throw new Error("Can't find java.home ??");
            }
            StringBuffer defaultFileName = new StringBuffer(home);
            defaultFileName.append(File.separator).append("lib");
            defaultFileName.append(File.separator).append("management");
            defaultFileName.append(File.separator).append("management.properties");
            fname = defaultFileName.toString();
        }
        final File configFile = new File(fname);
        if (!configFile.exists()) {
            error(CONFIG_FILE_NOT_FOUND, fname);
        }
        InputStream in = null;
        try {
            in = new FileInputStream(configFile);
            BufferedInputStream bin = new BufferedInputStream(in);
            p.load(bin);
        } catch (FileNotFoundException e) {
            error(CONFIG_FILE_OPEN_FAILED, e.getMessage());
        } catch (IOException e) {
            error(CONFIG_FILE_OPEN_FAILED, e.getMessage());
        } catch (SecurityException e) {
            error(CONFIG_FILE_ACCESS_DENIED, fname);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    error(CONFIG_FILE_CLOSE_FAILED, fname);
                }
            }
        }
    }
    public static void startAgent() throws Exception {
        String prop = System.getProperty("com.sun.management.agent.class");
        if (prop == null) {
            Properties props = getManagementProperties();
            if (props != null) {
                startAgent(props);
            }
            return;
        }
        String[] values = prop.split(":");
        if (values.length < 1 || values.length > 2) {
            error(AGENT_CLASS_INVALID, "\"" + prop + "\"");
        }
        String cname = values[0];
        String args = (values.length == 2 ? values[1] : null);
        if (cname == null || cname.length() == 0) {
            error(AGENT_CLASS_INVALID, "\"" + prop + "\"");
        }
        if (cname != null) {
            try {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cname);
                Method premain = clz.getMethod("premain",
                                               new Class[] { String.class });
                premain.invoke(null, 
                               new Object[] { args });
            } catch (ClassNotFoundException ex) {
                error(AGENT_CLASS_NOT_FOUND, "\"" + cname + "\"");
            } catch (NoSuchMethodException ex) {
                error(AGENT_CLASS_PREMAIN_NOT_FOUND, "\"" + cname + "\"");
            } catch (SecurityException ex) {
                error(AGENT_CLASS_ACCESS_DENIED);
            } catch (Exception ex) {
                String msg = (ex.getCause() == null
                                 ? ex.getMessage()
                                 : ex.getCause().getMessage());
                error(AGENT_CLASS_FAILED, msg);
            }
        }
    }
    public static void error(String key) {
        String keyText = getText(key);
        System.err.print(getText("agent.err.error") + ": " + keyText);
        throw new RuntimeException(keyText);
    }
    public static void error(String key, String[] params) {
        if (params == null || params.length == 0) {
            error(key);
        } else {
            StringBuffer message = new StringBuffer(params[0]);
            for (int i = 1; i < params.length; i++) {
                message.append(" " + params[i]);
            }
            error(key, message.toString());
        }
    }
    public static void error(String key, String message) {
        String keyText = getText(key);
        System.err.print(getText("agent.err.error") + ": " + keyText);
        System.err.println(": " + message);
        throw new RuntimeException(keyText);
    }
    public static void error(Exception e) {
        e.printStackTrace();
        System.err.println(getText(AGENT_EXCEPTION) + ": " + e.toString());
        throw new RuntimeException(e);
    }
    public static void warning(String key, String message) {
        System.err.print(getText("agent.err.warning") + ": " + getText(key));
        System.err.println(": " + message);
    }
    private static void initResource() {
        try {
            messageRB =
                ResourceBundle.getBundle("sun.management.resources.agent");
        } catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for management agent is missing");
        }
    }
    public static String getText(String key) {
        if (messageRB == null) {
            initResource();
        }
        try {
            return messageRB.getString(key);
        } catch (MissingResourceException e) {
            return "Missing management agent resource bundle: key = \"" + key + "\"";
        }
    }
    public static String getText(String key, String... args) {
        if (messageRB == null) {
            initResource();
        }
        String format = messageRB.getString(key);
        if (format == null) {
            format = "missing resource key: key = \"" + key + "\", " +
                "arguments = \"{0}\", \"{1}\", \"{2}\"";
        }
        return MessageFormat.format(format, (Object[]) args);
    }
}
