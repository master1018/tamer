public class SyncFactory {
    private SyncFactory() {
    }
    public static final String ROWSET_SYNC_PROVIDER =
            "rowset.provider.classname";
    public static final String ROWSET_SYNC_VENDOR =
            "rowset.provider.vendor";
    public static final String ROWSET_SYNC_PROVIDER_VERSION =
            "rowset.provider.version";
    private static String ROWSET_PROPERTIES = "rowset.properties";
    private static String default_provider =
            "com.sun.rowset.providers.RIOptimisticProvider";
    private static final SQLPermission SET_SYNCFACTORY_PERMISSION =
            new SQLPermission("setSyncFactory");
    private static Context ic;
    private static volatile Logger rsLogger;
    private static Level rsLevel;
    private static Hashtable implementations;
    private static Object logSync = new Object();
    private static java.io.PrintWriter logWriter = null;
    public static synchronized void registerProvider(String providerID)
            throws SyncFactoryException {
        ProviderImpl impl = new ProviderImpl();
        impl.setClassname(providerID);
        initMapIfNecessary();
        implementations.put(providerID, impl);
    }
    public static SyncFactory getSyncFactory() {
        return SyncFactoryHolder.factory;
    }
    public static synchronized void unregisterProvider(String providerID)
            throws SyncFactoryException {
        initMapIfNecessary();
        if (implementations.containsKey(providerID)) {
            implementations.remove(providerID);
        }
    }
    private static String colon = ":";
    private static String strFileSep = "/";
    private static synchronized void initMapIfNecessary() throws SyncFactoryException {
        Properties properties = new Properties();
        if (implementations == null) {
            implementations = new Hashtable();
            try {
                String strRowsetProperties = System.getProperty("rowset.properties");
                if (strRowsetProperties != null) {
                    ROWSET_PROPERTIES = strRowsetProperties;
                    try (FileInputStream fis = new FileInputStream(ROWSET_PROPERTIES)) {
                        properties.load(fis);
                    }
                    parseProperties(properties);
                }
                ROWSET_PROPERTIES = "javax" + strFileSep + "sql" +
                        strFileSep + "rowset" + strFileSep +
                        "rowset.properties";
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                try (InputStream stream =
                         (cl == null) ? ClassLoader.getSystemResourceAsStream(ROWSET_PROPERTIES)
                                      : cl.getResourceAsStream(ROWSET_PROPERTIES)) {
                    if (stream == null) {
                        throw new SyncFactoryException(
                            "Resource " + ROWSET_PROPERTIES + " not found");
                    }
                    properties.load(stream);
                }
                parseProperties(properties);
            } catch (FileNotFoundException e) {
                throw new SyncFactoryException("Cannot locate properties file: " + e);
            } catch (IOException e) {
                throw new SyncFactoryException("IOException: " + e);
            }
            properties.clear();
            String providerImpls = System.getProperty(ROWSET_SYNC_PROVIDER);
            if (providerImpls != null) {
                int i = 0;
                if (providerImpls.indexOf(colon) > 0) {
                    StringTokenizer tokenizer = new StringTokenizer(providerImpls, colon);
                    while (tokenizer.hasMoreElements()) {
                        properties.put(ROWSET_SYNC_PROVIDER + "." + i, tokenizer.nextToken());
                        i++;
                    }
                } else {
                    properties.put(ROWSET_SYNC_PROVIDER, providerImpls);
                }
                parseProperties(properties);
            }
        }
    }
    private static boolean debug = false;
    private static int providerImplIndex = 0;
    private static void parseProperties(Properties p) {
        ProviderImpl impl = null;
        String key = null;
        String[] propertyNames = null;
        for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
            String str = (String) e.nextElement();
            int w = str.length();
            if (str.startsWith(SyncFactory.ROWSET_SYNC_PROVIDER)) {
                impl = new ProviderImpl();
                impl.setIndex(providerImplIndex++);
                if (w == (SyncFactory.ROWSET_SYNC_PROVIDER).length()) {
                    propertyNames = getPropertyNames(false);
                } else {
                    propertyNames = getPropertyNames(true, str.substring(w - 1));
                }
                key = p.getProperty(propertyNames[0]);
                impl.setClassname(key);
                impl.setVendor(p.getProperty(propertyNames[1]));
                impl.setVersion(p.getProperty(propertyNames[2]));
                implementations.put(key, impl);
            }
        }
    }
    private static String[] getPropertyNames(boolean append) {
        return getPropertyNames(append, null);
    }
    private static String[] getPropertyNames(boolean append,
            String propertyIndex) {
        String dot = ".";
        String[] propertyNames =
                new String[]{SyncFactory.ROWSET_SYNC_PROVIDER,
            SyncFactory.ROWSET_SYNC_VENDOR,
            SyncFactory.ROWSET_SYNC_PROVIDER_VERSION};
        if (append) {
            for (int i = 0; i < propertyNames.length; i++) {
                propertyNames[i] = propertyNames[i] +
                        dot +
                        propertyIndex;
            }
            return propertyNames;
        } else {
            return propertyNames;
        }
    }
    private static void showImpl(ProviderImpl impl) {
        System.out.println("Provider implementation:");
        System.out.println("Classname: " + impl.getClassname());
        System.out.println("Vendor: " + impl.getVendor());
        System.out.println("Version: " + impl.getVersion());
        System.out.println("Impl index: " + impl.getIndex());
    }
    public static SyncProvider getInstance(String providerID)
            throws SyncFactoryException {
        if(providerID == null) {
            throw new SyncFactoryException("The providerID cannot be null");
        }
        initMapIfNecessary(); 
        initJNDIContext();    
        ProviderImpl impl = (ProviderImpl) implementations.get(providerID);
        if (impl == null) {
            return new com.sun.rowset.providers.RIOptimisticProvider();
        }
        Class c = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            c = Class.forName(providerID, true, cl);
            if (c != null) {
                return (SyncProvider) c.newInstance();
            } else {
                return new com.sun.rowset.providers.RIOptimisticProvider();
            }
        } catch (IllegalAccessException e) {
            throw new SyncFactoryException("IllegalAccessException: " + e.getMessage());
        } catch (InstantiationException e) {
            throw new SyncFactoryException("InstantiationException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new SyncFactoryException("ClassNotFoundException: " + e.getMessage());
        }
    }
    public static Enumeration<SyncProvider> getRegisteredProviders()
            throws SyncFactoryException {
        initMapIfNecessary();
        return implementations.elements();
    }
    public static void setLogger(Logger logger) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_SYNCFACTORY_PERMISSION);
        }
        if(logger == null){
            throw new NullPointerException("You must provide a Logger");
        }
        rsLogger = logger;
    }
    public static void setLogger(Logger logger, Level level) {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_SYNCFACTORY_PERMISSION);
        }
        if(logger == null){
            throw new NullPointerException("You must provide a Logger");
        }
        logger.setLevel(level);
        rsLogger = logger;
    }
    public static Logger getLogger() throws SyncFactoryException {
        Logger result = rsLogger;
        if (result == null) {
            throw new SyncFactoryException("(SyncFactory) : No logger has been set");
        }
        return result;
    }
    public static synchronized void setJNDIContext(javax.naming.Context ctx)
            throws SyncFactoryException {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(SET_SYNCFACTORY_PERMISSION);
        }
        if (ctx == null) {
            throw new SyncFactoryException("Invalid JNDI context supplied");
        }
        ic = ctx;
    }
    private static synchronized void initJNDIContext() throws SyncFactoryException {
        if ((ic != null) && (lazyJNDICtxRefresh == false)) {
            try {
                parseProperties(parseJNDIContext());
                lazyJNDICtxRefresh = true; 
            } catch (NamingException e) {
                e.printStackTrace();
                throw new SyncFactoryException("SPI: NamingException: " + e.getExplanation());
            } catch (Exception e) {
                e.printStackTrace();
                throw new SyncFactoryException("SPI: Exception: " + e.getMessage());
            }
        }
    }
    private static boolean lazyJNDICtxRefresh = false;
    private static Properties parseJNDIContext() throws NamingException {
        NamingEnumeration bindings = ic.listBindings("");
        Properties properties = new Properties();
        enumerateBindings(bindings, properties);
        return properties;
    }
    private static void enumerateBindings(NamingEnumeration bindings,
            Properties properties) throws NamingException {
        boolean syncProviderObj = false; 
        try {
            Binding bd = null;
            Object elementObj = null;
            String element = null;
            while (bindings.hasMore()) {
                bd = (Binding) bindings.next();
                element = bd.getName();
                elementObj = bd.getObject();
                if (!(ic.lookup(element) instanceof Context)) {
                    if (ic.lookup(element) instanceof SyncProvider) {
                        syncProviderObj = true;
                    }
                }
                if (syncProviderObj) {
                    SyncProvider sync = (SyncProvider) elementObj;
                    properties.put(SyncFactory.ROWSET_SYNC_PROVIDER,
                            sync.getProviderID());
                    syncProviderObj = false; 
                }
            }
        } catch (javax.naming.NotContextException e) {
            bindings.next();
            enumerateBindings(bindings, properties);
        }
    }
    private static class SyncFactoryHolder {
        static final SyncFactory factory = new SyncFactory();
    }
}
class ProviderImpl extends SyncProvider {
    private String className = null;
    private String vendorName = null;
    private String ver = null;
    private int index;
    public void setClassname(String classname) {
        className = classname;
    }
    public String getClassname() {
        return className;
    }
    public void setVendor(String vendor) {
        vendorName = vendor;
    }
    public String getVendor() {
        return vendorName;
    }
    public void setVersion(String providerVer) {
        ver = providerVer;
    }
    public String getVersion() {
        return ver;
    }
    public void setIndex(int i) {
        index = i;
    }
    public int getIndex() {
        return index;
    }
    public int getDataSourceLock() throws SyncProviderException {
        int dsLock = 0;
        try {
            dsLock = SyncFactory.getInstance(className).getDataSourceLock();
        } catch (SyncFactoryException sfEx) {
            throw new SyncProviderException(sfEx.getMessage());
        }
        return dsLock;
    }
    public int getProviderGrade() {
        int grade = 0;
        try {
            grade = SyncFactory.getInstance(className).getProviderGrade();
        } catch (SyncFactoryException sfEx) {
        }
        return grade;
    }
    public String getProviderID() {
        return className;
    }
    public javax.sql.RowSetReader getRowSetReader() {
        RowSetReader rsReader = null;
        try {
            rsReader = SyncFactory.getInstance(className).getRowSetReader();
        } catch (SyncFactoryException sfEx) {
        }
        return rsReader;
    }
    public javax.sql.RowSetWriter getRowSetWriter() {
        RowSetWriter rsWriter = null;
        try {
            rsWriter = SyncFactory.getInstance(className).getRowSetWriter();
        } catch (SyncFactoryException sfEx) {
        }
        return rsWriter;
    }
    public void setDataSourceLock(int param)
            throws SyncProviderException {
        try {
            SyncFactory.getInstance(className).setDataSourceLock(param);
        } catch (SyncFactoryException sfEx) {
            throw new SyncProviderException(sfEx.getMessage());
        }
    }
    public int supportsUpdatableView() {
        int view = 0;
        try {
            view = SyncFactory.getInstance(className).supportsUpdatableView();
        } catch (SyncFactoryException sfEx) {
        }
        return view;
    }
}
