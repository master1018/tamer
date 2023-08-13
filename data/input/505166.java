public class LogManager {
    private static final String lineSeparator = getPrivilegedSystemProperty("line.separator"); 
    private static final LoggingPermission perm = new LoggingPermission(
            "control", null); 
    static LogManager manager;
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging"; 
    public static LoggingMXBean getLoggingMXBean() {
      throw new UnsupportedOperationException();
    }
    private Hashtable<String, Logger> loggers;
    private Properties props;
    private PropertyChangeSupport listeners;
    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                String className = System
                        .getProperty("java.util.logging.manager"); 
                if (null != className) {
                    manager = (LogManager) getInstanceByClass(className);
                }
                if (null == manager) {
                    manager = new LogManager();
                }
                try {
                    manager.readConfiguration();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Logger root = new Logger("", null); 
                root.setLevel(Level.INFO);
                Logger.global.setParent(root);
                manager.addLogger(root);
                manager.addLogger(Logger.global);
                return null;
            }
        });
    }
    protected LogManager() {
        loggers = new Hashtable<String, Logger>();
        props = new Properties();
        listeners = new PropertyChangeSupport(this);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        reset();
                    }
                });
                return null;
            }
        });
    }
    static String getSystemLineSeparator() {
        return lineSeparator;
    }
    public void checkAccess() {
        if (null != System.getSecurityManager()) {
            System.getSecurityManager().checkPermission(perm);
        }
    }
    public synchronized boolean addLogger(Logger logger) {
        String name = logger.getName();
        if (null != loggers.get(name)) {
            return false;
        }
        addToFamilyTree(logger, name);
        loggers.put(name, logger);
        logger.setManager(this);
        return true;
    }
    private void addToFamilyTree(Logger logger, String name) {
        Logger parent = null;
        int lastSeparator;
        String parentName = name;
        while ((lastSeparator = parentName.lastIndexOf('.')) != -1) {
            parentName = parentName.substring(0, lastSeparator);
            parent = loggers.get(parentName);
            if (parent != null) {
                setParent(logger, parent);
                break;
            } else if (getProperty(parentName + ".level") != null || 
                    getProperty(parentName + ".handlers") != null) { 
                parent = Logger.getLogger(parentName);
                setParent(logger, parent);
                break;
            }
        }
        if (parent == null && null != (parent = loggers.get(""))) { 
            setParent(logger, parent);
        }
        String nameDot = name + '.';
        Collection<Logger> allLoggers = loggers.values();
        for (final Logger child : allLoggers) {
            Logger oldParent = child.getParent();
            if (parent == oldParent
                    && (name.length() == 0 || child.getName().startsWith(
                            nameDot))) {
                final Logger thisLogger = logger;
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        child.setParent(thisLogger);
                        return null;
                    }
                });
                if (null != oldParent) {
                    oldParent.children.remove(child);
                }
            }
        }
    }
    public synchronized Logger getLogger(String name) {
        return loggers.get(name);
    }
    public synchronized Enumeration<String> getLoggerNames() {
        return loggers.keys();
    }
    public static LogManager getLogManager() {
        return manager;
    }
    public String getProperty(String name) {
        return props.getProperty(name);
    }
    public void readConfiguration() throws IOException {
        String configClassName = System
                .getProperty("java.util.logging.config.class"); 
        if (null == configClassName
                || null == getInstanceByClass(configClassName)) {
            String configFile = System
                    .getProperty("java.util.logging.config.file"); 
            if (null == configFile) {
                configFile = new StringBuilder().append(
                        System.getProperty("java.home")).append(File.separator) 
                        .append("lib").append(File.separator).append( 
                                "logging.properties").toString(); 
            }
            InputStream input = null;
            try {
                try {
                    input = new BufferedInputStream(
                            new FileInputStream(configFile), 8192);
                } catch (Exception ex) {
                    input = new BufferedInputStream(
                            getClass().getResourceAsStream(
                                    "logging.properties"), 8192);
                }
                readConfiguration(input);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
    static String getPrivilegedSystemProperty(final String key) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key);
            }
        });
    }
    static Object getInstanceByClass(final String className) {
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(
                    className);
            return clazz.newInstance();
        } catch (Exception e) {
            try {
                Class<?> clazz = Thread.currentThread().getContextClassLoader()
                        .loadClass(className);
                return clazz.newInstance();
            } catch (Exception innerE) {
                System.err.println(Messages.getString("logging.20", className)); 
                System.err.println(innerE);
                return null;
            }
        }
    }
    private synchronized void readConfigurationImpl(InputStream ins)
            throws IOException {
        reset();
        props.load(ins);
        Logger root = loggers.get("");
        if (root != null) {
            root.setManager(this);
        }
        String configs = props.getProperty("config"); 
        if (null != configs) {
            StringTokenizer st = new StringTokenizer(configs, " "); 
            while (st.hasMoreTokens()) {
                String configerName = st.nextToken();
                getInstanceByClass(configerName);
            }
        }
        Collection<Logger> allLoggers = loggers.values();
        for (Logger logger : allLoggers) {
            String property = props.getProperty(logger.getName() + ".level"); 
            if (null != property) {
                logger.setLevel(Level.parse(property));
            }
        }
        listeners.firePropertyChange(null, null, null);
    }
    public void readConfiguration(InputStream ins) throws IOException {
        checkAccess();
        readConfigurationImpl(ins);
    }
    public synchronized void reset() {
        checkAccess();
        props = new Properties();
        Enumeration<String> names = getLoggerNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Logger logger = getLogger(name);
            if (logger != null) {
                logger.reset();
            }
        }
        Logger root = loggers.get(""); 
        if (null != root) {
            root.setLevel(Level.INFO);
        }
    }
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (l == null) {
            throw new NullPointerException();
        }
        checkAccess();
        listeners.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        checkAccess();
        listeners.removePropertyChangeListener(l);
    }
    synchronized Logger getOrCreate(String name, String resourceBundleName) {
        Logger result = getLogger(name);
        if (result == null) {
            result = new Logger(name, resourceBundleName);
            addLogger(result);
        }
        return result;
    }
    synchronized void setParent(Logger logger, Logger newParent) {
        logger.parent = newParent;
        if (logger.levelObjVal == null) {
            setLevelRecursively(logger, null);
        }
        newParent.children.add(logger);
        logger.updateDalvikLogHandler(); 
    }
    synchronized void setLevelRecursively(Logger logger, Level newLevel) {
        int previous = logger.levelIntVal;
        logger.levelObjVal = newLevel;
        if (newLevel == null) {
            logger.levelIntVal = logger.parent != null
                    ? logger.parent.levelIntVal
                    : Level.INFO.intValue();
        } else {
            logger.levelIntVal = newLevel.intValue();
        }
        if (previous != logger.levelIntVal) {
            for (Logger child : logger.children) {
                if (child.levelObjVal == null) {
                    setLevelRecursively(child, null);
                }
            }
        }
    }
}
