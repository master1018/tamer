public class RmiSslNoKeyStoreTest {
    static TestLogger log =
        new TestLogger("RmiSslNoKeyStoreTest");
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
    }
    public static interface PropertyNames {
        public static final String PORT="com.sun.management.jmxremote.port";
        public static final String CONFIG_FILE_NAME=
            "com.sun.management.config.file";
        public static final String USE_SSL="com.sun.management.jmxremote.ssl";
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
        public static final String KEYSTORE="javax.net.ssl.keyStore";
        public static final String KEYSTORE_PASSWD=
            "javax.net.ssl.keyStorePassword";
        public static final String KEYSTORE_TYPE="javax.net.ssl.keyStoreType";
        public static final String TRUSTSTORE="javax.net.ssl.trustStore";
        public static final String TRUSTSTORE_PASSWD=
            "javax.net.ssl.trustStorePassword";
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
    private static void checkKeystore(Properties props)
        throws IOException, GeneralSecurityException {
        if (log.isDebugOn())
            log.debug("checkKeystore","Checking Keystore configuration");
        final String keyStore =
            System.getProperty(PropertyNames.KEYSTORE);
        if (keyStore == null)
            throw new IllegalArgumentException("System property " +
                                               PropertyNames.KEYSTORE +
                                               " not specified");
        final String keyStorePass =
            System.getProperty(PropertyNames.KEYSTORE_PASSWD);
        if (keyStorePass == null) {
            final File ksf = new File(keyStore);
            if (! ksf.canRead())
                throw new IOException(keyStore + ": not readable");
            if (log.isDebugOn())
                log.debug("checkSSL", "No password.");
            throw new IllegalArgumentException("System property " +
                                               PropertyNames.KEYSTORE_PASSWD +
                                               " not specified");
        }
        final String keyStoreType =
            System.getProperty(PropertyNames.KEYSTORE_TYPE,
                               KeyStore.getDefaultType());
        final KeyStore ks         = KeyStore.getInstance(keyStoreType);
        final FileInputStream fin = new FileInputStream(keyStore);
        final char keypassword[]  = keyStorePass.toCharArray();
        try {
            ks.load(fin,keypassword);
        } finally {
            Arrays.fill(keypassword,' ');
            fin.close();
        }
        if (log.isDebugOn())
            log.debug("checkSSL","SSL configuration successfully checked");
    }
    private void checkSslConfiguration() throws Exception {
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
        log.debug("checkSslConfiguration",PropertyNames.USE_SSL+"="+useSsl);
        if (useSsl == false) {
            final String msg =
                PropertyNames.USE_SSL+"="+useSsl+", can't run test";
            throw new IllegalArgumentException(msg);
        }
        try {
            checkKeystore(props);
        } catch (Exception x) {
            log.debug("checkSslConfiguration","Test configuration OK: " + x);
            return;
        }
        final String msg = "KeyStore properly configured, can't run test";
        throw new IllegalArgumentException(msg);
    }
    private String testConfiguration(File file,int port) {
        final String path = (file==null)?null:file.getAbsolutePath();
        final String config = (path==null)?"Default config file":path;
        try {
            System.out.println("***");
            System.out.println("*** Testing configuration (port="+
                               port + "): "+ path);
            System.out.println("***");
            System.setProperty("com.sun.management.jmxremote.port",
                               Integer.toString(port));
            if (path != null)
                System.setProperty("com.sun.management.config.file", path);
            else
                System.getProperties().
                    remove("com.sun.management.config.file");
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
                log.trace("testConfiguration","Expected failure: " + err);
                log.debug("testConfiguration",x);
                System.out.println("Got expected failure: " + x);
                return null;
            } catch (Exception x) {
                log.debug("testConfiguration",x);
                return x.toString();
            }
            try {
                JMXConnector cc =
                    JMXConnectorFactory.connect(cs.getAddress(), null);
                cc.close();
            } catch (IOException x) {
                final String err = "Failed to initialize connector:" +
                    "\n\tcom.sun.management.jmxremote.port=" + port +
                    ((path!=null)?"\n\tcom.sun.management.config.file="+path:
                     "\n\t"+config) +
                    "\n\tError is: " + x;
                log.trace("testConfiguration","Expected failure: " + err);
                log.debug("testConfiguration",x);
                System.out.println("Got expected failure: " + x);
                return null;
            } catch (Exception x) {
                log.debug("testConfiguration",x);
                return x.toString();
            }
            try {
                cs.stop();
            } catch (Exception x) {
                final String err = "Failed to terminate: "+x;
                log.trace("testConfiguration",err);
                log.debug("testConfiguration",x);
            }
            final String err = "Bootstrap should have failed:" +
                "\n\tcom.sun.management.jmxremote.port=" + port +
                ((path!=null)?"\n\tcom.sun.management.config.file="+path:
                 "\n\t"+config);
            log.trace("testConfiguration",err);
            return err;
        } catch (Exception x) {
            final String err = "Failed to test bootstrap for:" +
                "\n\tcom.sun.management.jmxremote.port=" + port +
                ((path!=null)?"\n\tcom.sun.management.config.file="+path:
                 "\n\t"+config)+
                "\n\tError is: " + x;
            log.trace("testConfiguration",err);
            log.debug("testConfiguration",x);
            return err;
        }
    }
    private String testConfigurationFile(String fileName) {
        File file = new File(fileName);
        final String portStr = System.getProperty("rmi.port","12424");
        final int port       = Integer.parseInt(portStr);
        return testConfiguration(file,port+testPort++);
    }
    public void run(String args[]) {
        final String defaultKeyStore =
            getDefaultStoreName(DefaultValues.KEYSTORE);
        final String keyStore =
            System.getProperty(PropertyNames.KEYSTORE, defaultKeyStore);
        for (int i=0; i<args.length; i++) {
            String errStr =testConfigurationFile(args[i]);
            if (errStr != null) {
                throw new RuntimeException(errStr);
            }
            if ((System.getProperty(PropertyNames.KEYSTORE) == null) &&
                (System.getProperty(PropertyNames.KEYSTORE_PASSWD) == null)) {
                try {
                    System.setProperty(PropertyNames.KEYSTORE,keyStore);
                    log.trace("run",PropertyNames.KEYSTORE+"="+keyStore);
                    errStr =testConfigurationFile(args[i]);
                    if (errStr != null) {
                        throw new RuntimeException(errStr);
                    }
                } finally {
                    System.getProperties().remove(PropertyNames.KEYSTORE);
                }
            }
        }
    }
    public static void main(String args[]) {
        RmiSslNoKeyStoreTest manager = new RmiSslNoKeyStoreTest();
        try {
            manager.run(args);
        } catch (RuntimeException r) {
            System.err.println("Test Failed: "+ r.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            System.err.println("Test Failed: "+ t);
            t.printStackTrace();
            System.exit(2);
        }
        System.out.println("**** Test RmiSslNoKeyStoreTest Passed ****");
    }
}
