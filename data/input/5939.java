public class ResultLogManager extends NotificationBroadcasterSupport
        implements ResultLogManagerMXBean, MBeanRegistration {
    public static final ObjectName RESULT_LOG_MANAGER_NAME =
            ScanManager.makeSingletonName(ResultLogManagerMXBean.class);
    private static final Logger LOG =
            Logger.getLogger(ResultLogManager.class.getName());
    private final List<ResultRecord> memoryLog;
    private volatile boolean memCapacityReached = false;
    private volatile int memCapacity;
    private volatile long fileCapacity;
    private volatile File logFile;
    private volatile OutputStream logStream = null;
    private volatile long logCount = 0;
    private volatile ResultLogConfig config;
    ResultLogManager() {
        memoryLog =
                Collections.synchronizedList(new LinkedList<ResultRecord>() {
            public synchronized boolean add(ResultRecord e) {
                final int max = getMemoryLogCapacity();
                while (max > 0 && size() >= max) {
                    memCapacityReached = true;
                    removeFirst();
                }
                return super.add(e);
            }
        });
        memCapacity = 2048;
        fileCapacity = 0;
        logFile = null;
        config = new ResultLogConfig();
        config.setMemoryMaxRecords(memCapacity);
        config.setLogFileName(getLogFileName(false));
        config.setLogFileMaxRecords(fileCapacity);
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
    throws Exception {
        if (name == null)
            name = RESULT_LOG_MANAGER_NAME;
        objectName = name;
        mbeanServer = server;
        return name;
    }
    public void postRegister(Boolean registrationDone) {
    }
    public void preDeregister() throws Exception {
    }
    public void postDeregister() {
        try {
            if (logStream != null) {
                synchronized(this)  {
                    logStream.flush();
                    logStream.close();
                    logFile = null;
                    logStream = null;
                }
            }
        } catch (Exception x) {
            LOG.finest("Failed to close log properly: "+x);
        }
    }
    private File createNewLogFile(String basename) throws IOException {
        return XmlConfigUtils.createNewXmlFile(basename);
    }
    private OutputStream checkLogFile(String basename, long maxRecords,
                                      boolean force)
    throws IOException {
        final OutputStream newStream;
        synchronized(this) {
            if ((force==false) && (logCount < maxRecords))
                return logStream;
            final OutputStream oldStream = logStream;
            if (oldStream != null) {
                oldStream.flush();
                oldStream.close();
            }
            final File newFile = (basename==null)?null:createNewLogFile(basename);
            newStream = (newFile==null)?null:new FileOutputStream(newFile,true);
            logStream = newStream;
            logFile = newFile;
            fileCapacity = maxRecords;
            logCount = 0;
        }
        sendNotification(new Notification(LOG_FILE_CHANGED,objectName,
                getNextSeqNumber(),
                basename));
        return newStream;
    }
    public void log(ResultRecord record)
    throws IOException {
        if (memCapacity > 0) logToMemory(record);
        if (logFile != null) logToFile(record);
    }
    public ResultRecord[] getMemoryLog() {
        return memoryLog.toArray(new ResultRecord[0]);
    }
    public int getMemoryLogCapacity() {
        return memCapacity;
    }
    public void setMemoryLogCapacity(int maxRecords)  {
        synchronized(this) {
            memCapacity = maxRecords;
            if (memoryLog.size() < memCapacity)
                memCapacityReached = false;
            config.setMemoryMaxRecords(maxRecords);
        }
    }
    public void setLogFileCapacity(long maxRecords)
    throws IOException {
        synchronized (this) {
            fileCapacity = maxRecords;
            config.setLogFileMaxRecords(maxRecords);
        }
        checkLogFile(getLogFileName(),fileCapacity,false);
    }
    public long getLogFileCapacity()  {
        return fileCapacity;
    }
    public long getLoggedCount() {
        return logCount;
    }
    public void newLogFile(String logname, long maxRecord)
    throws IOException {
        checkLogFile(logname,maxRecord,true);
        config.setLogFileName(getLogFileName(false));
        config.setLogFileMaxRecords(getLogFileCapacity());
    }
    public String getLogFileName() {
        return getLogFileName(true);
    }
    public void clearLogs() throws IOException {
        clearMemoryLog();
        clearLogFile();
    }
    private void clearMemoryLog()throws IOException {
        synchronized(this) {
            memoryLog.clear();
            memCapacityReached = false;
        }
        sendNotification(new Notification(MEMORY_LOG_CLEARED,
                objectName,
                getNextSeqNumber(),"memory log cleared"));
    }
    private void clearLogFile() throws IOException {
        checkLogFile(getLogFileName(),fileCapacity,true);
    }
    private void logToMemory(ResultRecord record) {
        final boolean before = memCapacityReached;
        final boolean after;
        synchronized(this) {
            memoryLog.add(record);
            after = memCapacityReached;
        }
        if (before==false && after==true)
            sendNotification(new Notification(MEMORY_LOG_MAX_CAPACITY,
                    objectName,
                    getNextSeqNumber(),"memory log capacity reached"));
    }
    private void logToFile(ResultRecord record) throws IOException {
        final String basename;
        final long   maxRecords;
        synchronized (this) {
            if (logFile == null) return;
            basename = getLogFileName(false);
            maxRecords = fileCapacity;
        }
        final OutputStream stream =
                checkLogFile(basename,maxRecords,false);
        if (stream == null) return;
        synchronized (this) {
            try {
                XmlConfigUtils.write(record,stream,true);
                stream.flush();
                if (stream == logStream) logCount++;
            } catch (JAXBException x) {
                final IllegalArgumentException iae =
                        new IllegalArgumentException("bad record",x);
                LOG.finest("Failed to log record: "+x);
                throw iae;
            }
        }
    }
    public final static String LOG_FILE_CHANGED =
            "com.sun.jmx.examples.scandir.log.file.switched";
    public final static String MEMORY_LOG_MAX_CAPACITY =
            "com.sun.jmx.examples.scandir.log.memory.full";
    public final static String MEMORY_LOG_CLEARED =
            "com.sun.jmx.examples.scandir.log.memory.cleared";
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] {
            new MBeanNotificationInfo(new String[] {
                LOG_FILE_CHANGED},
                    Notification.class.getName(),
                    "Emitted when the log file is switched")
                    ,
            new MBeanNotificationInfo(new String[] {
                MEMORY_LOG_MAX_CAPACITY},
                    Notification.class.getName(),
                    "Emitted when the memory log capacity is reached")
                    ,
            new MBeanNotificationInfo(new String[] {
                MEMORY_LOG_CLEARED},
                    Notification.class.getName(),
                    "Emitted when the memory log is cleared")
        };
    }
    private String getLogFileName(boolean absolute) {
        synchronized (this) {
            if (logFile == null) return null;
            if (absolute) return logFile.getAbsolutePath();
            return logFile.getPath();
        }
    }
    void setConfig(ResultLogConfig logConfigBean) throws IOException {
        if (logConfigBean == null)
            throw new IllegalArgumentException("logConfigBean is null");
        synchronized (this) {
            config = logConfigBean;
            setMemoryLogCapacity(config.getMemoryMaxRecords());
        }
        final String filename = config.getLogFileName();
        final String logname  = getLogFileName(false);
        if ((filename != null && !filename.equals(logname))
        || (filename == null && logname != null)) {
            newLogFile(config.getLogFileName(),
                    config.getLogFileMaxRecords());
        } else {
            setLogFileCapacity(config.getLogFileMaxRecords());
        }
    }
    ResultLogConfig getConfig() {
        return config;
    }
    private MBeanServer mbeanServer;
    private ObjectName objectName;
}
