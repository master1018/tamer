public final class ConnectorBootstrap {
    public static interface DefaultValues {
        public static final String PORT = "0";
        public static final String CONFIG_FILE_NAME = "management.properties";
        public static final String USE_SSL = "true";
        public static final String USE_LOCAL_ONLY = "true";
        public static final String USE_REGISTRY_SSL = "false";
        public static final String USE_AUTHENTICATION = "true";
        public static final String PASSWORD_FILE_NAME = "jmxremote.password";
        public static final String ACCESS_FILE_NAME = "jmxremote.access";
        public static final String SSL_NEED_CLIENT_AUTH = "false";
    }
    public static interface PropertyNames {
        public static final String PORT =
                "com.sun.management.jmxremote.port";
        public static final String CONFIG_FILE_NAME =
                "com.sun.management.config.file";
        public static final String USE_LOCAL_ONLY =
                "com.sun.management.jmxremote.local.only";
        public static final String USE_SSL =
                "com.sun.management.jmxremote.ssl";
        public static final String USE_REGISTRY_SSL =
                "com.sun.management.jmxremote.registry.ssl";
        public static final String USE_AUTHENTICATION =
                "com.sun.management.jmxremote.authenticate";
        public static final String PASSWORD_FILE_NAME =
                "com.sun.management.jmxremote.password.file";
        public static final String ACCESS_FILE_NAME =
                "com.sun.management.jmxremote.access.file";
        public static final String LOGIN_CONFIG_NAME =
                "com.sun.management.jmxremote.login.config";
        public static final String SSL_ENABLED_CIPHER_SUITES =
                "com.sun.management.jmxremote.ssl.enabled.cipher.suites";
        public static final String SSL_ENABLED_PROTOCOLS =
                "com.sun.management.jmxremote.ssl.enabled.protocols";
        public static final String SSL_NEED_CLIENT_AUTH =
                "com.sun.management.jmxremote.ssl.need.client.auth";
        public static final String SSL_CONFIG_FILE_NAME =
                "com.sun.management.jmxremote.ssl.config.file";
    }
    private static class JMXConnectorServerData {
        public JMXConnectorServerData(
                JMXConnectorServer jmxConnectorServer,
                JMXServiceURL jmxRemoteURL) {
            this.jmxConnectorServer = jmxConnectorServer;
            this.jmxRemoteURL = jmxRemoteURL;
        }
        JMXConnectorServer jmxConnectorServer;
        JMXServiceURL jmxRemoteURL;
    }
    private static class PermanentExporter implements RMIExporter {
        public Remote exportObject(Remote obj,
                int port,
                RMIClientSocketFactory csf,
                RMIServerSocketFactory ssf)
                throws RemoteException {
            synchronized (this) {
                if (firstExported == null) {
                    firstExported = obj;
                }
            }
            final UnicastServerRef ref;
            if (csf == null && ssf == null) {
                ref = new UnicastServerRef(port);
            } else {
                ref = new UnicastServerRef2(port, csf, ssf);
            }
            return ref.exportObject(obj, null, true);
        }
        public boolean unexportObject(Remote obj, boolean force)
                throws NoSuchObjectException {
            return UnicastRemoteObject.unexportObject(obj, force);
        }
        Remote firstExported;
    }
    private static class AccessFileCheckerAuthenticator
            implements JMXAuthenticator {
        public AccessFileCheckerAuthenticator(Map<String, Object> env) throws IOException {
            environment = env;
            accessFile = (String) env.get("jmx.remote.x.access.file");
            properties = propertiesFromFile(accessFile);
        }
        public Subject authenticate(Object credentials) {
            final JMXAuthenticator authenticator =
                    new JMXPluggableAuthenticator(environment);
            final Subject subject = authenticator.authenticate(credentials);
            checkAccessFileEntries(subject);
            return subject;
        }
        private void checkAccessFileEntries(Subject subject) {
            if (subject == null) {
                throw new SecurityException(
                        "Access denied! No matching entries found in " +
                        "the access file [" + accessFile + "] as the " +
                        "authenticated Subject is null");
            }
            final Set principals = subject.getPrincipals();
            for (Iterator i = principals.iterator(); i.hasNext();) {
                final Principal p = (Principal) i.next();
                if (properties.containsKey(p.getName())) {
                    return;
                }
            }
            final Set<String> principalsStr = new HashSet<String>();
            for (Iterator i = principals.iterator(); i.hasNext();) {
                final Principal p = (Principal) i.next();
                principalsStr.add(p.getName());
            }
            throw new SecurityException(
                    "Access denied! No entries found in the access file [" +
                    accessFile + "] for any of the authenticated identities " +
                    principalsStr);
        }
        private static Properties propertiesFromFile(String fname)
                throws IOException {
            Properties p = new Properties();
            if (fname == null) {
                return p;
            }
            FileInputStream fin = new FileInputStream(fname);
            p.load(fin);
            fin.close();
            return p;
        }
        private final Map<String, Object> environment;
        private final Properties properties;
        private final String accessFile;
    }
    public static synchronized JMXConnectorServer initialize() {
        final Properties props = Agent.loadManagementProperties();
        if (props == null) {
            return null;
        }
        final String portStr = props.getProperty(PropertyNames.PORT);
        return initialize(portStr, props);
    }
    public static synchronized JMXConnectorServer initialize(String portStr, Properties props) {
        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException x) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_PORT, x, portStr);
        }
        if (port < 0) {
            throw new AgentConfigurationError(INVALID_JMXREMOTE_PORT, portStr);
        }
        final String useAuthenticationStr =
                props.getProperty(PropertyNames.USE_AUTHENTICATION,
                DefaultValues.USE_AUTHENTICATION);
        final boolean useAuthentication =
                Boolean.valueOf(useAuthenticationStr).booleanValue();
        final String useSslStr =
                props.getProperty(PropertyNames.USE_SSL,
                DefaultValues.USE_SSL);
        final boolean useSsl =
                Boolean.valueOf(useSslStr).booleanValue();
        final String useRegistrySslStr =
                props.getProperty(PropertyNames.USE_REGISTRY_SSL,
                DefaultValues.USE_REGISTRY_SSL);
        final boolean useRegistrySsl =
                Boolean.valueOf(useRegistrySslStr).booleanValue();
        final String enabledCipherSuites =
                props.getProperty(PropertyNames.SSL_ENABLED_CIPHER_SUITES);
        String enabledCipherSuitesList[] = null;
        if (enabledCipherSuites != null) {
            StringTokenizer st = new StringTokenizer(enabledCipherSuites, ",");
            int tokens = st.countTokens();
            enabledCipherSuitesList = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledCipherSuitesList[i] = st.nextToken();
            }
        }
        final String enabledProtocols =
                props.getProperty(PropertyNames.SSL_ENABLED_PROTOCOLS);
        String enabledProtocolsList[] = null;
        if (enabledProtocols != null) {
            StringTokenizer st = new StringTokenizer(enabledProtocols, ",");
            int tokens = st.countTokens();
            enabledProtocolsList = new String[tokens];
            for (int i = 0; i < tokens; i++) {
                enabledProtocolsList[i] = st.nextToken();
            }
        }
        final String sslNeedClientAuthStr =
                props.getProperty(PropertyNames.SSL_NEED_CLIENT_AUTH,
                DefaultValues.SSL_NEED_CLIENT_AUTH);
        final boolean sslNeedClientAuth =
                Boolean.valueOf(sslNeedClientAuthStr).booleanValue();
        final String sslConfigFileName =
                props.getProperty(PropertyNames.SSL_CONFIG_FILE_NAME);
        String loginConfigName = null;
        String passwordFileName = null;
        String accessFileName = null;
        if (useAuthentication) {
            loginConfigName =
                    props.getProperty(PropertyNames.LOGIN_CONFIG_NAME);
            if (loginConfigName == null) {
                passwordFileName =
                        props.getProperty(PropertyNames.PASSWORD_FILE_NAME,
                        getDefaultFileName(DefaultValues.PASSWORD_FILE_NAME));
                checkPasswordFile(passwordFileName);
            }
            accessFileName = props.getProperty(PropertyNames.ACCESS_FILE_NAME,
                    getDefaultFileName(DefaultValues.ACCESS_FILE_NAME));
            checkAccessFile(accessFileName);
        }
        if (log.debugOn()) {
            log.debug("initialize",
                    Agent.getText("jmxremote.ConnectorBootstrap.initialize") +
                    "\n\t" + PropertyNames.PORT + "=" + port +
                    "\n\t" + PropertyNames.USE_SSL + "=" + useSsl +
                    "\n\t" + PropertyNames.USE_REGISTRY_SSL + "=" + useRegistrySsl +
                    "\n\t" + PropertyNames.SSL_CONFIG_FILE_NAME + "=" + sslConfigFileName +
                    "\n\t" + PropertyNames.SSL_ENABLED_CIPHER_SUITES + "=" +
                    enabledCipherSuites +
                    "\n\t" + PropertyNames.SSL_ENABLED_PROTOCOLS + "=" +
                    enabledProtocols +
                    "\n\t" + PropertyNames.SSL_NEED_CLIENT_AUTH + "=" +
                    sslNeedClientAuth +
                    "\n\t" + PropertyNames.USE_AUTHENTICATION + "=" +
                    useAuthentication +
                    (useAuthentication ? (loginConfigName == null ? ("\n\t" + PropertyNames.PASSWORD_FILE_NAME + "=" +
                    passwordFileName) : ("\n\t" + PropertyNames.LOGIN_CONFIG_NAME + "=" +
                    loginConfigName)) : "\n\t" +
                    Agent.getText("jmxremote.ConnectorBootstrap.initialize.noAuthentication")) +
                    (useAuthentication ? ("\n\t" + PropertyNames.ACCESS_FILE_NAME + "=" +
                    accessFileName) : "") +
                    "");
        }
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer cs = null;
        JMXServiceURL url = null;
        try {
            final JMXConnectorServerData data = exportMBeanServer(
                    mbs, port, useSsl, useRegistrySsl,
                    sslConfigFileName, enabledCipherSuitesList,
                    enabledProtocolsList, sslNeedClientAuth,
                    useAuthentication, loginConfigName,
                    passwordFileName, accessFileName);
            cs = data.jmxConnectorServer;
            url = data.jmxRemoteURL;
            log.config("initialize",
                    Agent.getText("jmxremote.ConnectorBootstrap.initialize.ready",
                    url.toString()));
        } catch (Exception e) {
            throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
        }
        try {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("remoteAddress", url.toString());
            properties.put("authenticate", useAuthenticationStr);
            properties.put("ssl", useSslStr);
            properties.put("sslRegistry", useRegistrySslStr);
            properties.put("sslNeedClientAuth", sslNeedClientAuthStr);
            ConnectorAddressLink.exportRemote(properties);
        } catch (Exception e) {
            log.debug("initialize", e);
        }
        return cs;
    }
    public static JMXConnectorServer startLocalConnectorServer() {
        System.setProperty("java.rmi.server.randomIDs", "true");
        Map<String, Object> env = new HashMap<String, Object>();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, new PermanentExporter());
        String localhost = "localhost";
        InetAddress lh = null;
        try {
            lh = InetAddress.getByName(localhost);
            localhost = lh.getHostAddress();
        } catch (UnknownHostException x) {
        }
        if (lh == null || !lh.isLoopbackAddress()) {
            localhost = "127.0.0.1";
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", localhost, 0);
            Properties props = Agent.getManagementProperties();
            if (props ==  null) {
                props = new Properties();
            }
            String useLocalOnlyStr = props.getProperty(
                    PropertyNames.USE_LOCAL_ONLY, DefaultValues.USE_LOCAL_ONLY);
            boolean useLocalOnly = Boolean.valueOf(useLocalOnlyStr).booleanValue();
            if (useLocalOnly) {
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                        new LocalRMIServerSocketFactory());
            }
            JMXConnectorServer server =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            server.start();
            return server;
        } catch (Exception e) {
            throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
        }
    }
    private static void checkPasswordFile(String passwordFileName) {
        if (passwordFileName == null || passwordFileName.length() == 0) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_SET);
        }
        File file = new File(passwordFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_FOUND, passwordFileName);
        }
        if (!file.canRead()) {
            throw new AgentConfigurationError(PASSWORD_FILE_NOT_READABLE, passwordFileName);
        }
        FileSystem fs = FileSystem.open();
        try {
            if (fs.supportsFileSecurity(file)) {
                if (!fs.isAccessUserOnly(file)) {
                    final String msg = Agent.getText("jmxremote.ConnectorBootstrap.initialize.password.readonly",
                            passwordFileName);
                    log.config("initialize", msg);
                    throw new AgentConfigurationError(PASSWORD_FILE_ACCESS_NOT_RESTRICTED,
                            passwordFileName);
                }
            }
        } catch (IOException e) {
            throw new AgentConfigurationError(PASSWORD_FILE_READ_FAILED,
                    e, passwordFileName);
        }
    }
    private static void checkAccessFile(String accessFileName) {
        if (accessFileName == null || accessFileName.length() == 0) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_SET);
        }
        File file = new File(accessFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_FOUND, accessFileName);
        }
        if (!file.canRead()) {
            throw new AgentConfigurationError(ACCESS_FILE_NOT_READABLE, accessFileName);
        }
    }
    private static void checkRestrictedFile(String restrictedFileName) {
        if (restrictedFileName == null || restrictedFileName.length() == 0) {
            throw new AgentConfigurationError(FILE_NOT_SET);
        }
        File file = new File(restrictedFileName);
        if (!file.exists()) {
            throw new AgentConfigurationError(FILE_NOT_FOUND, restrictedFileName);
        }
        if (!file.canRead()) {
            throw new AgentConfigurationError(FILE_NOT_READABLE, restrictedFileName);
        }
        FileSystem fs = FileSystem.open();
        try {
            if (fs.supportsFileSecurity(file)) {
                if (!fs.isAccessUserOnly(file)) {
                    final String msg = Agent.getText(
                            "jmxremote.ConnectorBootstrap.initialize.file.readonly",
                            restrictedFileName);
                    log.config("initialize", msg);
                    throw new AgentConfigurationError(
                            FILE_ACCESS_NOT_RESTRICTED, restrictedFileName);
                }
            }
        } catch (IOException e) {
            throw new AgentConfigurationError(
                    FILE_READ_FAILED, e, restrictedFileName);
        }
    }
    private static String getDefaultFileName(String basename) {
        final String fileSeparator = File.separator;
        return System.getProperty("java.home") + fileSeparator + "lib" +
                fileSeparator + "management" + fileSeparator +
                basename;
    }
    private static SslRMIServerSocketFactory createSslRMIServerSocketFactory(
            String sslConfigFileName,
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean sslNeedClientAuth) {
        if (sslConfigFileName == null) {
            return new SslRMIServerSocketFactory(
                    enabledCipherSuites,
                    enabledProtocols,
                    sslNeedClientAuth);
        } else {
            checkRestrictedFile(sslConfigFileName);
            try {
                Properties p = new Properties();
                InputStream in = new FileInputStream(sslConfigFileName);
                try {
                    BufferedInputStream bin = new BufferedInputStream(in);
                    p.load(bin);
                } finally {
                    in.close();
                }
                String keyStore =
                        p.getProperty("javax.net.ssl.keyStore");
                String keyStorePassword =
                        p.getProperty("javax.net.ssl.keyStorePassword", "");
                String trustStore =
                        p.getProperty("javax.net.ssl.trustStore");
                String trustStorePassword =
                        p.getProperty("javax.net.ssl.trustStorePassword", "");
                char[] keyStorePasswd = null;
                if (keyStorePassword.length() != 0) {
                    keyStorePasswd = keyStorePassword.toCharArray();
                }
                char[] trustStorePasswd = null;
                if (trustStorePassword.length() != 0) {
                    trustStorePasswd = trustStorePassword.toCharArray();
                }
                KeyStore ks = null;
                if (keyStore != null) {
                    ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream ksfis = new FileInputStream(keyStore);
                    try {
                        ks.load(ksfis, keyStorePasswd);
                    } finally {
                        ksfis.close();
                    }
                }
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, keyStorePasswd);
                KeyStore ts = null;
                if (trustStore != null) {
                    ts = KeyStore.getInstance(KeyStore.getDefaultType());
                    FileInputStream tsfis = new FileInputStream(trustStore);
                    try {
                        ts.load(tsfis, trustStorePasswd);
                    } finally {
                        tsfis.close();
                    }
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) ts);
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                return new SslRMIServerSocketFactory(
                        ctx,
                        enabledCipherSuites,
                        enabledProtocols,
                        sslNeedClientAuth);
            } catch (Exception e) {
                throw new AgentConfigurationError(AGENT_EXCEPTION, e, e.toString());
            }
        }
    }
    private static JMXConnectorServerData exportMBeanServer(
            MBeanServer mbs,
            int port,
            boolean useSsl,
            boolean useRegistrySsl,
            String sslConfigFileName,
            String[] enabledCipherSuites,
            String[] enabledProtocols,
            boolean sslNeedClientAuth,
            boolean useAuthentication,
            String loginConfigName,
            String passwordFileName,
            String accessFileName)
            throws IOException, MalformedURLException {
        System.setProperty("java.rmi.server.randomIDs", "true");
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        Map<String, Object> env = new HashMap<String, Object>();
        PermanentExporter exporter = new PermanentExporter();
        env.put(RMIExporter.EXPORTER_ATTRIBUTE, exporter);
        if (useAuthentication) {
            if (loginConfigName != null) {
                env.put("jmx.remote.x.login.config", loginConfigName);
            }
            if (passwordFileName != null) {
                env.put("jmx.remote.x.password.file", passwordFileName);
            }
            env.put("jmx.remote.x.access.file", accessFileName);
            if (env.get("jmx.remote.x.password.file") != null ||
                    env.get("jmx.remote.x.login.config") != null) {
                env.put(JMXConnectorServer.AUTHENTICATOR,
                        new AccessFileCheckerAuthenticator(env));
            }
        }
        RMIClientSocketFactory csf = null;
        RMIServerSocketFactory ssf = null;
        if (useSsl || useRegistrySsl) {
            csf = new SslRMIClientSocketFactory();
            ssf = createSslRMIServerSocketFactory(
                    sslConfigFileName, enabledCipherSuites,
                    enabledProtocols, sslNeedClientAuth);
        }
        if (useSsl) {
            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
                    csf);
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
                    ssf);
        }
        JMXConnectorServer connServer = null;
        try {
            connServer =
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
            connServer.start();
        } catch (IOException e) {
            if (connServer == null) {
                throw new AgentConfigurationError(CONNECTOR_SERVER_IO_ERROR,
                        e, url.toString());
            } else {
                throw new AgentConfigurationError(CONNECTOR_SERVER_IO_ERROR,
                        e, connServer.getAddress().toString());
            }
        }
        final Registry registry;
        if (useRegistrySsl) {
            registry =
                    new SingleEntryRegistry(port, csf, ssf,
                    "jmxrmi", exporter.firstExported);
        } else {
            registry =
                    new SingleEntryRegistry(port,
                    "jmxrmi", exporter.firstExported);
        }
        JMXServiceURL remoteURL = new JMXServiceURL(
                "service:jmx:rmi:
                ((UnicastRef) ((RemoteObject) registry).getRef()).getLiveRef().getPort() +
                "/jmxrmi");
        return new JMXConnectorServerData(connServer, remoteURL);
    }
    private ConnectorBootstrap() {
    }
    private static final ClassLogger log =
        new ClassLogger(ConnectorBootstrap.class.getPackage().getName(),
                        "ConnectorBootstrap");
}
