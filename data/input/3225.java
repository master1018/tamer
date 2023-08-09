public class ScanDirConfig extends NotificationBroadcasterSupport
        implements ScanDirConfigMXBean, MBeanRegistration {
    private static final Logger LOG =
            Logger.getLogger(ScanDirConfig.class.getName());
    private final static String NOTIFICATION_PREFIX =
            ScanManagerConfig.class.getPackage().getName();
    public final static String NOTIFICATION_SAVED =
            NOTIFICATION_PREFIX+".saved";
    public final static String NOTIFICATION_LOADED =
            NOTIFICATION_PREFIX+".loaded";
    public final static String NOTIFICATION_MODIFIED =
            NOTIFICATION_PREFIX+".modified";
    private static MBeanNotificationInfo[] NOTIFICATION_INFO = {
        new MBeanNotificationInfo(
                new String[] {NOTIFICATION_SAVED},
                Notification.class.getName(),
                "Emitted when the configuration is saved"),
        new MBeanNotificationInfo(
                new String[] {NOTIFICATION_LOADED},
                Notification.class.getName(),
                "Emitted when the configuration is loaded"),
        new MBeanNotificationInfo(
                new String[] {NOTIFICATION_MODIFIED},
                Notification.class.getName(),
                "Emitted when the configuration is modified"),
    };
    private volatile ScanManagerConfig config;
    private String filename = null;
    private volatile String configname = null;
    private volatile SaveState status = CREATED;
    public ScanDirConfig(String filename) {
        this(filename,null);
    }
    public ScanDirConfig(String filename, ScanManagerConfig initialConfig) {
        super(NOTIFICATION_INFO);
        this.filename = filename;
        this.config = initialConfig;
    }
    public void load() throws IOException {
        if (filename == null)
            throw new UnsupportedOperationException("load");
        synchronized(this) {
            config = new XmlConfigUtils(filename).readFromFile();
            if (configname != null) config = config.copy(configname);
            else configname = config.getName();
            status=LOADED;
        }
        sendNotification(NOTIFICATION_LOADED);
    }
    public void save() throws IOException {
        if (filename == null)
            throw new UnsupportedOperationException("load");
        synchronized (this) {
            new XmlConfigUtils(filename).writeToFile(config);
            status = SAVED;
        }
        sendNotification(NOTIFICATION_SAVED);
    }
    public ScanManagerConfig getConfiguration() {
        synchronized (this) {
            return XmlConfigUtils.xmlClone(config);
        }
    }
    private void sendNotification(String type) {
        final Object source = (objectName==null)?this:objectName;
        final Notification n = new Notification(type,source,
                getNextSeqNumber(),
                "The configuration is "+
                type.substring(type.lastIndexOf('.')+1));
        sendNotification(n);
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        if (name == null) {
            if (config == null) return null;
            if (config.getName() == null) return null;
            name = ScanManager.
                    makeMBeanName(ScanDirConfigMXBean.class,config.getName());
        }
        objectName = name;
        mbeanServer = server;
        synchronized (this) {
            configname = name.getKeyProperty("name");
            if (config == null) config = new ScanManagerConfig(configname);
            else config = config.copy(configname);
        }
        return name;
    }
    public void postRegister(Boolean registrationDone) {
    }
    public void preDeregister() throws Exception {
    }
    public void postDeregister() {
    }
    public String getConfigFilename() {
        return filename;
    }
    public void setConfiguration(ScanManagerConfig config) {
        synchronized (this) {
            if (config == null) {
                this.config = null;
                return;
            }
            if (configname == null)
                configname = config.getName();
            this.config = config.copy(configname);
            status = MODIFIED;
        }
        sendNotification(NOTIFICATION_MODIFIED);
    }
    public DirectoryScannerConfig
            addDirectoryScanner(String name, String dir, String filePattern,
                                long sizeExceedsMaxBytes, long sinceLastModified) {
         final DirectoryScannerConfig scanner =
                 new DirectoryScannerConfig(name);
         scanner.setRootDirectory(dir);
         if (filePattern!=null||sizeExceedsMaxBytes>0||sinceLastModified>0) {
            final FileMatch filter = new FileMatch();
            filter.setFilePattern(filePattern);
            filter.setSizeExceedsMaxBytes(sizeExceedsMaxBytes);
            if (sinceLastModified > 0)
                filter.setLastModifiedBefore(new Date(new Date().getTime()
                                                -sinceLastModified));
            scanner.addIncludeFiles(filter);
         }
         synchronized (this) {
            config.putScan(scanner);
            status = MODIFIED;
         }
         LOG.fine("config: "+config);
         sendNotification(NOTIFICATION_MODIFIED);
         return scanner;
    }
    public DirectoryScannerConfig removeDirectoryScanner(String name)
        throws IOException, InstanceNotFoundException {
        final DirectoryScannerConfig scanner;
        synchronized (this) {
            scanner = config.removeScan(name);
            if (scanner == null)
                throw new IllegalArgumentException(name+": scanner not found");
            status = MODIFIED;
        }
        sendNotification(NOTIFICATION_MODIFIED);
        return scanner;
    }
    public SaveState getSaveState() {
        return status;
    }
    static String DEFAULT = "DEFAULT";
    private static String getBasename(String name) {
        final int dot = name.indexOf('.');
        if (dot<0)  return name;
        if (dot==0) return getBasename(name.substring(1));
        return name.substring(0,dot);
    }
    static String guessConfigName(String configFileName,String defaultFile) {
        try {
            if (configFileName == null) return DEFAULT;
            final File f = new File(configFileName);
            if (f.canRead()) {
                final String confname = XmlConfigUtils.read(f).getName();
                if (confname != null && confname.length()>0) return confname;
            }
            final File f2 = new File(defaultFile);
            if (f.equals(f2)) return DEFAULT;
            final String guess = getBasename(f.getName());
            if (guess == null) return DEFAULT;
            if (guess.length()==0) return DEFAULT;
            return guess;
        } catch (Exception x) {
            return DEFAULT;
        }
    }
    private volatile MBeanServer mbeanServer;
    private volatile ObjectName objectName;
}
