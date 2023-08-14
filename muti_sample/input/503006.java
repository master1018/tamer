public class LogRecord implements Serializable {
    private static final long serialVersionUID = 5372048053134512534L;
    private static final int MAJOR = 1;
    private static final int MINOR = 4;
    private static long currentSequenceNumber = 0;
    private static ThreadLocal<Integer> currentThreadId = new ThreadLocal<Integer>();
    private static int initThreadId = 0;
    private Level level;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String message;
    private int threadID;
    private long millis;
    private Throwable thrown;
    private String loggerName;
    private String resourceBundleName;
    private transient ResourceBundle resourceBundle;
    private transient Object[] parameters;
    private transient boolean sourceInited;
    public LogRecord(Level level, String msg) {
        if (null == level) {
            throw new NullPointerException(Messages.getString("logging.4")); 
        }
        this.level = level;
        this.message = msg;
        this.millis = System.currentTimeMillis();
        synchronized (LogRecord.class) {
            this.sequenceNumber = currentSequenceNumber++;
            Integer id = currentThreadId.get();
            if (null == id) {
                this.threadID = initThreadId;
                currentThreadId.set(Integer.valueOf(initThreadId++));
            } else {
                this.threadID = id.intValue();
            }
        }
        this.sourceClassName = null;
        this.sourceMethodName = null;
        this.loggerName = null;
        this.parameters = null;
        this.resourceBundle = null;
        this.resourceBundleName = null;
        this.thrown = null;
    }
    public Level getLevel() {
        return level;
    }
    public void setLevel(Level level) {
        if (null == level) {
            throw new NullPointerException(Messages.getString("logging.4")); 
        }
        this.level = level;
    }
    public String getLoggerName() {
        return loggerName;
    }
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getMillis() {
        return millis;
    }
    public void setMillis(long millis) {
        this.millis = millis;
    }
    public Object[] getParameters() {
        return parameters;
    }
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }
    public String getResourceBundleName() {
        return resourceBundleName;
    }
    public void setResourceBundleName(String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
    }
    public long getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    public String getSourceClassName() {
        initSource();
        return sourceClassName;
    }
    private void initSource() {
        if (sourceInited) {
            return;
        }
        boolean sawLogger = false;
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            String current = element.getClassName();
            if (current.startsWith(Logger.class.getName())) {
                sawLogger = true;
            } else if (sawLogger) {
                this.sourceClassName = element.getClassName();
                this.sourceMethodName = element.getMethodName();
                break;
            }
        }
        sourceInited = true;
    }
    public void setSourceClassName(String sourceClassName) {
        sourceInited = true;
        this.sourceClassName = sourceClassName;
    }
    public String getSourceMethodName() {
        initSource();
        return sourceMethodName;
    }
    public void setSourceMethodName(String sourceMethodName) {
        sourceInited = true;
        this.sourceMethodName = sourceMethodName;
    }
    public int getThreadID() {
        return threadID;
    }
    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }
    public Throwable getThrown() {
        return thrown;
    }
    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeByte(MAJOR);
        out.writeByte(MINOR);
        if (null == parameters) {
            out.writeInt(-1);
        } else {
            out.writeInt(parameters.length);
            for (Object element : parameters) {
                out.writeObject(null == element ? null : element.toString());
            }
        }
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        byte major = in.readByte();
        byte minor = in.readByte();
        if (major != MAJOR) {
            throw new IOException(Messages.getString("logging.5", 
                    Byte.valueOf(major), Byte.valueOf(minor)));
        }
        int length = in.readInt();
        if (length >= 0) {
            parameters = new Object[length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = in.readObject();
            }
        }
        if (null != resourceBundleName) {
            try {
                resourceBundle = Logger.loadResourceBundle(resourceBundleName);
            } catch (MissingResourceException e) {
                resourceBundle = null;
            }
        }
    }
}
