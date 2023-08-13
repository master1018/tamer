public class RmiBootstrapTest {
    static TestLogger log =
        new TestLogger("RmiBootstrapTest");
    static int testPort = 0;
    public static interface DefaultValues {
        public static final String PORT="0";
        public static final String CONFIG_FILE_NAME="management.properties";
        public static final String USE_SSL="true";
        public static final String USE_AUTHENTICATION="true";
        public static final String PASSWORD_FILE_NAME="jmxremote.password";
        public static final String ACCESS_FILE_NAME="jmxremote.access";
        public static final String KEYSTORE="keystore";
        public static final String KEYSTORE_PASSWD="password";
        public static final String TRUSTSTORE="truststore";
        public static final String TRUSTSTORE_PASSWD="trustword";
        public static final String SSL_NEED_CLIENT_AUTH="false";
    }
    public static interface PropertyNames {
        public static final String PORT=
            "com.sun.management.jmxremote.port";
        public static final String CONFIG_FILE_NAME=
            "com.sun.management.config.file";
        public static final String USE_SSL=
            "com.sun.management.jmxremote.ssl";
        public static final String USE_AUTHENTICATION=
            "com.sun.management.jmxremote.authenticate";
        public static final String PASSWORD_FILE_NAME=
            "com.sun.management.jmxremote.password.file";
        public static final String ACCESS_FILE_NAME=
            "com.sun.management.jmxremote.access.file";
        public static final String INSTRUMENT_ALL=
            "com.sun.management.instrumentall";
        public static final String CREDENTIALS =
            "jmx.remote.credentials";
        public static final String KEYSTORE=
            "javax.net.ssl.keyStore";
        public static final String KEYSTORE_PASSWD=
            "javax.net.ssl.keyStorePassword";
        public static final String TRUSTSTORE=
            "javax.net.ssl.trustStore";
        public static final String TRUSTSTORE_PASSWD=
            "javax.net.ssl.trustStorePassword";
        public static final String SSL_ENABLED_CIPHER_SUITES =
            "com.sun.management.jmxremote.ssl.enabled.cipher.suites";
        public static final String SSL_ENABLED_PROTOCOLS =
            "com.sun.management.jmxremote.ssl.enabled.protocols";
        public static final String SSL_NEED_CLIENT_AUTH =
            "com.sun.management.jmxremote.ssl.need.client.auth";
    }
    private static class ConfigFilenameFilter implements FilenameFilter {
        final String suffix;
        final String prefix;
        ConfigFilenameFilter(String prefix, String suffix) {
            this.suffix=suffix;
            this.prefix=prefix;
        }
        public boolean accept(File dir, String name) {
            return (name.startsWith(prefix) && name.endsWith(suffix));
        }
    }
    private static File[] findConfigurationFilesOk() {
        final String testSrc = System.getProperty("test.src");
        final File dir = new File(testSrc);
        final FilenameFilter filter =
            new ConfigFilenameFilter("management_test","ok.properties");
        return dir.listFiles(filter);
    }
    private static File[] findConfigurationFilesKo() {
        final String testSrc = System.getProperty("test.src");
        final File dir = new File(testSrc);
        final FilenameFilter filter =
            new ConfigFilenameFilter("management_test","ko.properties");
        return dir.listFiles(filter);
    }
    public static int listMBeans(MBeanServerConnection server)
        throws IOException {
        return listMBeans(server,null,null);
    }
    public static int listMBeans(MBeanServerConnection server,
                                  ObjectName pattern, QueryExp query)
        throws IOException {
        final Set names = server.queryNames(pattern,query);
        for (final Iterator i=names.iterator(); i.hasNext(); ) {
            ObjectName name = (ObjectName)i.next();
            log.trace("listMBeans","Got MBean: "+name);
            try {
                MBeanInfo info =
                    server.getMBeanInfo((ObjectName)name);
                MBeanAttributeInfo[] attrs = info.getAttributes();
                if (attrs == null) continue;
                for (int j=0; j<attrs.length; j++) {
                    if (attrs[j].isReadable()) {
                        try {
                            Object o =
                                server.getAttribute(name,attrs[j].getName());
                            if (log.isDebugOn())
                                log.debug("listMBeans","\t\t" +
                                          attrs[j].getName() +
                                          " = "+o);
                        } catch (Exception x) {
                            log.trace("listMBeans","JmxClient failed to get " +
                                      attrs[j].getName() + ": " + x);
                            final IOException io =
                                new IOException("JmxClient failed to get " +
                                                attrs[j].getName());
                            io.initCause(x);
                            throw io;
                        }
                    }
                }
            } catch (Exception x) {
               log.trace("listMBeans",
                         "JmxClient failed to get MBeanInfo: "  + x);
                final IOException io =
                    new IOException("JmxClient failed to get MBeanInfo: "+x);
                io.initCause(x);
                throw io;
            }
        }
        return names.size();
    }
    private static String getDefaultFileName(String basename) {
        final String fileSeparator = File.separator;
        final StringBuffer defaultFileName =
            new StringBuffer(System.getProperty("java.home")).
            append(fileSeparator).append("lib").append(fileSeparator).
            append("management").append(fileSeparator).
            append(basename);
        return defaultFileName.toString();
    }
    private static String getDefaultStoreName(String basename) {
        final String fileSeparator = File.separator;
        final StringBuffer defaultFileName =
            new StringBuffer(System.getProperty("test.src")).
            append(fileSeparator).append("ssl").append(fileSeparator).
            append(basename);
        return defaultFileName.toString();
    }
    private ArrayList readCredentials(String passwordFileName)
        throws IOException {
        final Properties pws = new Properties();
        final ArrayList  result = new ArrayList();
        final File f = new File(passwordFileName);
        if (!f.exists()) return result;
        FileInputStream fin = new FileInputStream(passwordFileName);
        try {pws.load(fin);}finally{fin.close();}
        for (Enumeration en=pws.propertyNames();en.hasMoreElements();) {
            final String[] cred = new String[2];
            cred[0]=(String)en.nextElement();
            cred[1]=pws.getProperty(cred[0]);
            result.add(cred);
        }
        return result;
    }
    public int connectAndRead(JMXServiceURL url,
                              Object[] useCredentials,
                              boolean  expectConnectOk,
                              boolean  expectReadOk)
        throws IOException {
        int errorCount = 0;
        for (int i=0 ; i<useCredentials.length ; i++) {
            final Map m = new HashMap();
            final String[] credentials = (String[])useCredentials[i];
            final String   crinfo;
            if (credentials != null) {
                crinfo = "{"+credentials[0] + ", " + credentials[1] + "}";
                m.put(PropertyNames.CREDENTIALS,credentials);
            } else {
                crinfo="no credentials";
            }
            log.trace("testCommunication","using credentials: " + crinfo);
            final JMXConnector c;
            try {
                c = JMXConnectorFactory.connect(url,m);
            } catch (IOException x ) {
                if (expectConnectOk) {
                    final String err = "Connection failed for " + crinfo +
                        ": " + x;
                    System.out.println(err);
                    log.trace("testCommunication",err);
                    log.debug("testCommunication",x);
                    errorCount++;
                    continue;
                } else {
                    System.out.println("Connection failed as expected for " +
                                       crinfo + ": " + x);
                    continue;
                }
            } catch (RuntimeException x ) {
                if (expectConnectOk) {
                    final String err = "Connection failed for " + crinfo +
                        ": " + x;
                    System.out.println(err);
                    log.trace("testCommunication",err);
                    log.debug("testCommunication",x);
                    errorCount++;
                    continue;
                } else {
                    System.out.println("Connection failed as expected for " +
                                       crinfo + ": " + x);
                    continue;
                }
            }
            try {
                MBeanServerConnection conn =
                    c.getMBeanServerConnection();
                if (log.isDebugOn()) {
                    log.debug("testCommunication","Connection is:" + conn);
                    log.debug("testCommunication","Server domain is: " +
                              conn.getDefaultDomain());
                }
                final ObjectName pattern =
                    new ObjectName("java.lang:type=Memory,*");
                final int count = listMBeans(conn,pattern,null);
                if (count == 0)
                    throw new Exception("Expected at least one matching "+
                                        "MBean for "+pattern);
                if (expectReadOk) {
                    System.out.println("Communication succeeded " +
                                       "as expected for "+
                                       crinfo + ": found " + count
                                       + ((count<2)?"MBean":"MBeans"));
                } else {
                    final String err = "Expected failure didn't occur for " +
                        crinfo;
                    System.out.println(err);
                    errorCount++;
                }
            } catch (IOException x ) {
                if (expectReadOk) {
                    final String err = "Communication failed with " + crinfo +
                        ": " + x;
                    System.out.println(err);
                    log.trace("testCommunication",err);
                    log.debug("testCommunication",x);
                    errorCount++;
                    continue;
                } else {
                    System.out.println("Communication failed as expected for "+
                                       crinfo + ": " + x);
                    continue;
                }
            } catch (RuntimeException x ) {
                if (expectReadOk) {
                    final String err = "Communication failed with " + crinfo +
                        ": " + x;
                    System.out.println(err);
                    log.trace("testCommunication",err);
                    log.debug("testCommunication",x);
                    errorCount++;
                    continue;
                } else {
                    System.out.println("Communication failed as expected for "+
                                       crinfo + ": " + x);
                }
            } catch (Exception x) {
                final String err = "Failed to read MBeans with " + crinfo +
                    ": " + x;
                System.out.println(err);
                log.trace("testCommunication",err);
                log.debug("testCommunication",x);
                errorCount++;
                continue;
            } finally {
                c.close();
            }
        }
        return errorCount;
    }
    private void setSslProperties() {
        final String defaultKeyStore =
            getDefaultStoreName(DefaultValues.KEYSTORE);
        final String defaultTrustStore =
            getDefaultStoreName(DefaultValues.TRUSTSTORE);
        final String keyStore =
            System.getProperty(PropertyNames.KEYSTORE, defaultKeyStore);
        System.setProperty(PropertyNames.KEYSTORE,keyStore);
        log.trace("setSslProperties",PropertyNames.KEYSTORE+"="+keyStore);
        final String password =
            System.getProperty(PropertyNames.KEYSTORE_PASSWD,
                               DefaultValues.KEYSTORE_PASSWD);
        System.setProperty(PropertyNames.KEYSTORE_PASSWD,password);
        log.trace("setSslProperties",
                  PropertyNames.KEYSTORE_PASSWD+"="+password);
        final String trustStore =
            System.getProperty(PropertyNames.TRUSTSTORE,
                               defaultTrustStore);
        System.setProperty(PropertyNames.TRUSTSTORE,trustStore);
        log.trace("setSslProperties",
                  PropertyNames.TRUSTSTORE+"="+trustStore);
        final String trustword =
            System.getProperty(PropertyNames.TRUSTSTORE_PASSWD,
                               DefaultValues.TRUSTSTORE_PASSWD);
        System.setProperty(PropertyNames.TRUSTSTORE_PASSWD,trustword);
        log.trace("setSslProperties",
                  PropertyNames.TRUSTSTORE_PASSWD+"="+trustword);
    }
    private void checkSslConfiguration() {
        try {
            final String defaultConf =
                getDefaultFileName(DefaultValues.CONFIG_FILE_NAME);
            final String confname =
                System.getProperty(PropertyNames.CONFIG_FILE_NAME,defaultConf);
            final Properties props = new Properties();
            final File conf = new File(confname);
            if (conf.exists()) {
                FileInputStream fin = new FileInputStream(conf);
                try {props.load(fin);} finally {fin.close();}
            }
            final String  useSslStr =
                props.getProperty(PropertyNames.USE_SSL,
                                  DefaultValues.USE_SSL);
            final boolean useSsl =
                Boolean.valueOf(useSslStr).booleanValue();
            log.debug("checkSslConfiguration",
                      PropertyNames.USE_SSL+"="+useSsl+
                      ": setting SSL");
            final String  useSslClientAuthStr =
                props.getProperty(PropertyNames.SSL_NEED_CLIENT_AUTH,
                                  DefaultValues.SSL_NEED_CLIENT_AUTH);
            final boolean useSslClientAuth =
                Boolean.valueOf(useSslClientAuthStr).booleanValue();
            log.debug("checkSslConfiguration",
                      PropertyNames.SSL_NEED_CLIENT_AUTH+"="+useSslClientAuth);
            final String sslCipherSuites =
                props.getProperty(PropertyNames.SSL_ENABLED_CIPHER_SUITES);
            log.debug("checkSslConfiguration",
                      PropertyNames.SSL_ENABLED_CIPHER_SUITES + "=" +
                      sslCipherSuites);
            final String sslProtocols =
                props.getProperty(PropertyNames.SSL_ENABLED_PROTOCOLS);
            log.debug("checkSslConfiguration",
                      PropertyNames.SSL_ENABLED_PROTOCOLS + "=" +
                      sslProtocols);
            if (useSsl) setSslProperties();
        } catch (Exception x) {
            System.out.println("Failed to setup SSL configuration: " + x);
            log.debug("checkSslConfiguration",x);
        }
    }
    public void testCommunication(JMXServiceURL url)
        throws IOException {
        final String defaultConf =
            getDefaultFileName(DefaultValues.CONFIG_FILE_NAME);
        final String confname =
            System.getProperty(PropertyNames.CONFIG_FILE_NAME,defaultConf);
        final Properties props = new Properties();
        final File conf = new File(confname);
        if (conf.exists()) {
            FileInputStream fin = new FileInputStream(conf);
            try {props.load(fin);} finally {fin.close();}
        }
        final String  useAuthenticationStr =
            props.getProperty(PropertyNames.USE_AUTHENTICATION,
                              DefaultValues.USE_AUTHENTICATION);
        final boolean useAuthentication =
            Boolean.valueOf(useAuthenticationStr).booleanValue();
        final String defaultPasswordFileName = Utils.convertPath(
            getDefaultFileName(DefaultValues.PASSWORD_FILE_NAME));
        final String passwordFileName = Utils.convertPath(
            props.getProperty(PropertyNames.PASSWORD_FILE_NAME,
                              defaultPasswordFileName));
        final String defaultAccessFileName = Utils.convertPath(
            getDefaultFileName(DefaultValues.ACCESS_FILE_NAME));
        final String accessFileName = Utils.convertPath(
            props.getProperty(PropertyNames.ACCESS_FILE_NAME,
                              defaultAccessFileName));
        if (useAuthentication) {
            System.out.println("PasswordFileName: " + passwordFileName);
            System.out.println("accessFileName: " + accessFileName);
        }
        final Object[] allCredentials;
        final Object[] noCredentials = { null };
        if (useAuthentication) {
            final ArrayList l = readCredentials(passwordFileName);
            if (l.size() == 0) allCredentials = null;
            else allCredentials = l.toArray();
        } else allCredentials = noCredentials;
        int errorCount = 0;
        if (allCredentials!=null) {
            errorCount += connectAndRead(url,allCredentials,true,true);
        } else {
            final String[][] someCredentials = {
                null,
                { "modify", "R&D" },
                { "measure", "QED" }
            };
            errorCount += connectAndRead(url,someCredentials,false,false);
        }
        if (useAuthentication && allCredentials != noCredentials) {
            final String[][] badCredentials = {
                { "bad.user", "R&D" },
                { "measure", "bad.password" }
            };
            errorCount += connectAndRead(url,badCredentials,false,false);
        }
        if (errorCount > 0) {
            final String err = "Test " + confname + " failed with " +
                errorCount + " error(s)";
            log.debug("testCommunication",err);
            throw new RuntimeException(err);
        }
    }
    private String testConfiguration(File file,int port) {
        final String path;
        try {
            path=(file==null)?null:file.getCanonicalPath();
        } catch(IOException x) {
            final String err = "Failed to test configuration " + file +
                ": " + x;
            log.trace("testConfiguration",err);
            log.debug("testConfiguration",x);
            return err;
        }
        final String config = (path==null)?"Default config file":path;
        System.out.println("***");
        System.out.println("*** Testing configuration (port=" + port + "): "
                           + path);
        System.out.println("***");
        System.setProperty("com.sun.management.jmxremote.port",
                           Integer.toString(port));
        if (path != null)
            System.setProperty("com.sun.management.config.file", path);
        else
            System.getProperties().remove("com.sun.management.config.file");
        log.trace("testConfiguration","com.sun.management.jmxremote.port="+port);
        if (path != null && log.isDebugOn())
            log.trace("testConfiguration",
                      "com.sun.management.config.file="+path);
        checkSslConfiguration();
        final JMXConnectorServer cs;
        try {
            cs = ConnectorBootstrap.initialize();
        } catch (AgentConfigurationError x) {
            final String err = "Failed to initialize connector:" +
                "\n\tcom.sun.management.jmxremote.port=" + port +
                ((path!=null)?"\n\tcom.sun.management.config.file="+path:
                 "\n\t"+config) +
                "\n\tError is: " + x;
            log.trace("testConfiguration",err);
            log.debug("testConfiguration",x);
            return err;
        } catch (Exception x) {
            log.debug("testConfiguration",x);
            return x.toString();
        }
        try {
            JMXServiceURL url =
                new JMXServiceURL("rmi",null,0,"/jndi/rmi:
                                  port+"/jmxrmi");
            try {
                testCommunication(url);
            } catch (Exception x) {
                final String err = "Failed to connect to agent {url="+url+
                    "}: " + x;
                log.trace("testConfiguration",err);
                log.debug("testConfiguration",x);
                return err;
            }
        } catch (Exception x) {
            final String err = "Failed to test configuration "+config+
                ": "+x;
            log.trace("testConfiguration",err);
            log.debug("testConfiguration",x);
            return err;
        } finally {
            try {
                cs.stop();
            } catch (Exception x) {
                final String err = "Failed to terminate: "+x;
                log.trace("testConfiguration",err);
                log.debug("testConfiguration",x);
            }
        }
        System.out.println("Configuration " + config + " successfully tested");
        return null;
    }
    private String testConfigurationKo(File conf,int port) {
        final String errStr = testConfiguration(conf,port+testPort++);
        if (errStr == null) {
            return "Configuration " +
                conf + " should have failed!";
        }
        System.out.println("Configuration " +
                           conf + " failed as expected");
        log.debug("runko","Error was: " + errStr);
        return null;
    }
    private String testConfigurationFile(String fileName) {
        File file = new File(fileName);
        final String portStr = System.getProperty("rmi.port","12424");
        final int port       = Integer.parseInt(portStr);
        if (fileName.endsWith("ok.properties")) {
            return testConfiguration(file,port+testPort++);
        }
        if (fileName.endsWith("ko.properties")) {
            return testConfigurationKo(file,port+testPort++);
        }
        return fileName +
            ": test file suffix must be one of [ko|ok].properties";
    }
    public void runko() {
        final String portStr = System.getProperty("rmi.port","12424");
        final int port       = Integer.parseInt(portStr);
        final File[] conf = findConfigurationFilesKo();
        if ((conf == null)||(conf.length == 0))
            throw new RuntimeException("No configuration found");
        String errStr;
        for (int i=0;i<conf.length;i++) {
            errStr = testConfigurationKo(conf[i],port+testPort++);
            if (errStr != null) {
                throw new RuntimeException(errStr);
            }
        }
    }
    public void runok() {
        final String portStr = System.getProperty("rmi.port","12424");
        final int port       = Integer.parseInt(portStr);
        final File[] conf = findConfigurationFilesOk();
        if ((conf == null)||(conf.length == 0))
            throw new RuntimeException("No configuration found");
        String errStr;
        for (int i=0;i<conf.length;i++) {
            errStr = testConfiguration(conf[i],port+testPort++);
            if (errStr != null) {
                throw new RuntimeException(errStr);
            }
        }
    }
    public void run() {
        runok();
        runko();
    }
    public void run(String args[]) {
        if (args.length == 0) {
            run() ; return;
        }
        for (int i=0; i<args.length; i++) {
            final String errStr =testConfigurationFile(args[i]);
            if (errStr != null) {
                throw new RuntimeException(errStr);
            }
        }
    }
    public static void main(String args[]) {
        RmiBootstrapTest manager = new RmiBootstrapTest();
        try {
            manager.run(args);
        } catch (RuntimeException r) {
            System.out.println("Test Failed: "+ r.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            System.out.println("Test Failed: "+ t);
            t.printStackTrace();
            System.exit(2);
        }
        System.out.println("**** Test  RmiBootstrap Passed ****");
    }
}
